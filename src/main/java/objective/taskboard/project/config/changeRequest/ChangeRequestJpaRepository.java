package objective.taskboard.project.config.changeRequest;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import objective.taskboard.domain.ProjectFilterConfiguration;

@Repository
@Transactional(readOnly = true)
public class ChangeRequestJpaRepository implements ChangeRequestRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ChangeRequest> listByProject(ProjectFilterConfiguration project) {
        return entityManager.createNamedQuery("ChangeRequest.listByProject", ChangeRequest.class)
                .setParameter("project", project)
                .getResultList();
    }

    @Override
    public void add(ChangeRequest item) {
        entityManager.persist(item);
    }

    @Override
    public void update(ChangeRequest item) {
        entityManager.merge(item);
    }

    @Override
    public void remove(ChangeRequest item) {
        entityManager.remove(item);
    }

}
