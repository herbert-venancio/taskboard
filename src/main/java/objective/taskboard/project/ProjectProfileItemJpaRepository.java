package objective.taskboard.project;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import objective.taskboard.domain.ProjectFilterConfiguration;

@Repository
@Transactional(readOnly = true)
class ProjectProfileItemJpaRepository implements ProjectProfileItemRepository {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public List<ProjectProfileItem> listByProject(ProjectFilterConfiguration project) {
        return entityManager.createNamedQuery("ProjectProfileItem.listByProject", ProjectProfileItem.class)
                .setParameter("project", project)
                .getResultList();
    }
    
    @Override
    public void add(ProjectProfileItem item) {
        entityManager.persist(item);
    }

    @Override
    public void remove(ProjectProfileItem item) {
        entityManager.remove(item);
    }
}
