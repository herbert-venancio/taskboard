package objective.taskboard.database;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.core.metamodel.type.EntityType;
import org.javers.repository.jql.QueryBuilder;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.core.support.AbstractRepositoryMetadata;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import com.google.gson.Gson;

@Service
public class AuditService {

    private final Javers javers;
    private Map<Class<?>, JpaRepository> repositoryMap;

    @Autowired
    public AuditService(Javers javers, List<JpaRepository> jpaRepositories) {
        this.javers = javers;
        repositoryMap = mapAuditedRepositories(jpaRepositories);
    }

    private static Map<Class<?>, JpaRepository> mapAuditedRepositories(List<JpaRepository> jpaRepositories) {
        Map<Class<?>, JpaRepository> repositoryMap = new HashMap<>();
        jpaRepositories.forEach(repo -> {
            Optional<Class<?>> repoType = Arrays.stream(ClassUtils.getAllInterfaces(repo))
                    .filter(type -> AnnotationUtils.isAnnotationDeclaredLocally(JaversSpringDataAuditable.class, type))
                    .findFirst();
            if(!repoType.isPresent())
                return;
            Class<?> domainType = AbstractRepositoryMetadata.getMetadata(repoType.get()).getDomainType();
            repositoryMap.put(domainType, repo);
        });
        return repositoryMap;
    }

    public Collection<Class<?>> getAuditedTypes() {
        return repositoryMap.keySet();
    }

    public List<Change> getChanges(Class<?> type) {
        QueryBuilder jqlQuery = QueryBuilder.byClass(type);
        return javers.findChanges(jqlQuery.build());
    }

    public List<CdoSnapshot> getSnapshots(Class<?> type) {
        QueryBuilder jqlQuery = QueryBuilder.byClass(type);
        return javers.findSnapshots(jqlQuery.build());
    }

    public <T, I extends Serializable> Object restore(String id, BigDecimal commitId, Class<T> domainType) {
        Assert.notNull(id, "id cannot be null");
        Assert.notNull(commitId, "commitId cannot be null");
        Assert.notNull(domainType, "domainType cannot be null");

        JpaRepository<T, I> repo = getJpaRepository(domainType);
        Assert.notNull(repo, domainType.getName() + "Not audited type");

        EntityType entityType = javers.getTypeMapping(domainType);
        I entityId = normalize(id, entityType);

        T entity = repo.getOne(entityId);
        Assert.notNull(entity, "Entity not found");

        CdoSnapshot snapshot = getSnapshot(entityId, commitId, domainType).orElse(null);
        Assert.notNull(snapshot, "Snapshot not found");

        BeanWrapper bean = new BeanWrapperImpl(entity);
        snapshot.getState().forEachProperty((propertyName, value) -> {
            if(entityType.getProperty(propertyName).isPrimitiveOrValueType())
                bean.setPropertyValue(propertyName, value);
        });
        repo.save(entity);
        return entity;
    }

    private Optional<CdoSnapshot> getSnapshot(Object id, BigDecimal commitId, Class<?> type) {
        QueryBuilder jqlQuery = QueryBuilder.byInstanceId(id, type).withCommitId(commitId);

        List<CdoSnapshot> snaps = javers.findSnapshots(jqlQuery.build());
        return snaps.stream()
                .findFirst();
    }

    @SuppressWarnings("unchecked")
    private <T, I extends Serializable> JpaRepository<T, I> getJpaRepository(Class<T> domainType) {
        return repositoryMap.get(domainType);
    }

    @SuppressWarnings("unchecked")
    private <I extends Serializable> I normalize(String id, EntityType entityType) {
        Assert.isTrue(!entityType.hasCompositeId(), "Composite keys not implemented yet");
        return (I) new Gson().fromJson(id, entityType.getIdProperty().getRawType());
    }
}
