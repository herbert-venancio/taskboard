package objective.taskboard.database;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.javers.core.Javers;
import org.javers.core.diff.Change;
import org.javers.core.metamodel.object.CdoSnapshot;
import org.javers.repository.jql.QueryBuilder;
import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.core.support.AbstractRepositoryMetadata;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;

@Service
public class AuditService {

    private final Javers javers;
    private Map<Class<?>, JpaRepository<?, ?>> repositoryMap;

    @Autowired
    public AuditService(Javers javers, List<JpaRepository<?, ?>> jpaRepositories) {
        this.javers = javers;
        repositoryMap = mapAuditedRepositories(jpaRepositories);
    }

    private static Map<Class<?>, JpaRepository<?, ?>> mapAuditedRepositories(List<JpaRepository<?, ?>> jpaRepositories) {
        Map<Class<?>, JpaRepository<?, ?>> repositoryMap = new HashMap<>();
        jpaRepositories.forEach(repo -> {
            Optional<Class<?>> repoType = Arrays.stream(ClassUtils.getAllInterfaces(repo))
                    .filter(type -> AnnotationUtils.isAnnotationDeclaredLocally(JaversSpringDataAuditable.class, type))
                    .findFirst();
            if (!repoType.isPresent())
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

}
