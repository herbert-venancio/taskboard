package objective.taskboard.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.project.ProjectDefaultTeamByIssueType;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = {
        "flyway.enabled=false"
})
@ContextConfiguration(classes = DashboardConfigurationRepositoryIntegrationTest.Configuration.class)
public class DashboardConfigurationRepositoryIntegrationTest {
    
    @EntityScan(basePackageClasses = {DashboardConfiguration.class, ProjectFilterConfiguration.class, ProjectDefaultTeamByIssueType.class})
    public static class Configuration {
        @Bean
        public JpaRepositoryFactoryBean<DashboardConfigurationRepository, DashboardConfiguration, Long> dashboardConfigurationRepository() {
            return new JpaRepositoryFactoryBean<>(DashboardConfigurationRepository.class);
        }
    }
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private DashboardConfigurationRepository repository;
    
    @Test
    public void givenProjectExistsAndItWasConfigured_whenFindByProject_thenReturnConfiguredProject() {
        ProjectFilterConfiguration testProject = new ProjectFilterConfiguration("TEST", 0L);
        testProject = entityManager.persist(testProject);
        DashboardConfiguration testConfiguration = new DashboardConfiguration();
        testConfiguration.setProject(testProject);
        testConfiguration.setTimelineDaysToDisplay(10);
        testConfiguration = entityManager.persist(testConfiguration);
        
        Optional<DashboardConfiguration> actualConfiguration = repository.findByProject(testProject);
        assertThat(actualConfiguration).isPresent();
        assertThat(actualConfiguration).contains(testConfiguration);
    }
    
    @Test
    public void givenProjectExistsAndItWasNotConfigured_whenFindByProject_thenReturnEmptyConfiguration() {
        ProjectFilterConfiguration testProject = new ProjectFilterConfiguration("TEST", 0L);
        testProject = entityManager.persist(testProject);

        Optional<DashboardConfiguration> actualConfiguration = repository.findByProject(testProject);
        assertThat(actualConfiguration).isNotPresent();
    }

    @Test
    public void givenProjectIsNull_whenFindByProject_thenReturnEmptyConfiguration() {
        Optional<DashboardConfiguration> actualConfiguration = repository.findByProject(null);
        assertThat(actualConfiguration).isNotPresent();
    }
}
