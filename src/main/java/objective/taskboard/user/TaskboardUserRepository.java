package objective.taskboard.user;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class TaskboardUserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<TaskboardUser> getByUsername(String username) {
        try {
            return Optional.of(entityManager.createQuery("select u from TaskboardUser u where u.username = :username", TaskboardUser.class)
                    .setParameter("username", username)
                    .getSingleResult());

        } catch (NoResultException e) { //NOSONAR
            return Optional.empty();
        }
    }

    public void add(TaskboardUser user) {
       entityManager.persist(user); 
    }
}
