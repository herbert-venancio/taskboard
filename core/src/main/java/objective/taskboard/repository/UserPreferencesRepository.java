package objective.taskboard.repository;

import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.domain.UserPreferences;

@Repository
@Transactional(readOnly = true)
@CacheConfig(cacheNames = CacheConfiguration.USER_PREFERENCES)
public class UserPreferencesRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Cacheable(key = "#jiraUser")
    public Optional<UserPreferences> findOneByJiraUser(String jiraUser) {
        return entityManager.createNamedQuery("UserPreferences.findByJiraUser", UserPreferences.class)
                .setParameter("jiraUser", jiraUser)
                .setMaxResults(1)
                .getResultList().stream().findFirst();
    }

    @CacheEvict(key = "#newUserPreferences.getJiraUser()")
    public void add(UserPreferences newUserPreferences) {
        entityManager.persist(newUserPreferences);
    }

    @CacheEvict(key = "#updatedUserPreferences.getJiraUser()")
    public void updatePreferences(UserPreferences updatedUserPreferences) {
        entityManager.merge(updatedUserPreferences);
    }

}
