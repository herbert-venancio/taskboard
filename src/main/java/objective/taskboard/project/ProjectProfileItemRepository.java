package objective.taskboard.project;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import objective.taskboard.domain.ProjectFilterConfiguration;

@Repository
@Transactional(readOnly = true)
public class ProjectProfileItemRepository {

    @PersistenceContext
    private EntityManager entityManager;
    
    public List<ProjectProfileItem> listByProject(ProjectFilterConfiguration project) {
        return entityManager.createNamedQuery("ProjectProfileItem.listByProject", ProjectProfileItem.class)
                .setParameter("project", project)
                .getResultList();
    }
    
    public void add(ProjectProfileItem projectProfile) {
        entityManager.persist(projectProfile);
    }
}
