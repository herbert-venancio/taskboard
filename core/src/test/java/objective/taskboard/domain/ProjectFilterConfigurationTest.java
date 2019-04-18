package objective.taskboard.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import objective.taskboard.project.ProjectDefaultTeamByIssueType;
import objective.taskboard.repository.ProjectFilterConfigurationRepository;

@RunWith(SpringRunner.class)
@DataJpaTest
@TestPropertySource(properties = {
        "flyway.enabled=false"
})
@ContextConfiguration(classes = ProjectFilterConfigurationTest.Configuration.class)
public class ProjectFilterConfigurationTest {

    private static final String PROJECT_NAME = "PROJECT";
    private static final long DEFAULT_TEAM_ID = 10L;

    @EntityScan(basePackageClasses = {ProjectFilterConfiguration.class, ProjectDefaultTeamByIssueType.class})
    public static class Configuration {
        @Bean
        public JpaRepositoryFactoryBean<ProjectFilterConfigurationRepository, ProjectFilterConfiguration, Long> projectFilterConfigurationRepository() {
            return new JpaRepositoryFactoryBean<>(ProjectFilterConfigurationRepository.class);
        }
    }

    @Autowired
    private ProjectFilterConfigurationRepository projectFilterConfigurationRepository;

    private ProjectFilterConfiguration subject;

    @Before
    public void setup() {
        subject = new ProjectFilterConfiguration(PROJECT_NAME, DEFAULT_TEAM_ID);
    }

    @Test
    public void getTeamByIssueTypeId_givenEmptyTeamsByIssueTypes_returnEmpty() {
        assertTrue(subject.getTeamsByIssueTypes().isEmpty());
        Optional<Long> teamByIssueTypeId = subject.getTeamByIssueTypeId(5L);
        assertEquals(Optional.empty(), teamByIssueTypeId);
    }

    @Test
    public void getTeamByIssueTypeId_givenExistentTeamsByIssueTypes_returnDefaultTeamByIssueTypeId() {
        assertTrue(subject.getTeamsByIssueTypes().isEmpty());

        subject.addProjectTeamForIssueType(20L, 2L);
        subject.addProjectTeamForIssueType(40L, 4L);
        subject.addProjectTeamForIssueType(60L, 6L);

        assertEquals(Optional.of(20L), subject.getTeamByIssueTypeId(2L));
        assertEquals(Optional.of(40L), subject.getTeamByIssueTypeId(4L));
        assertEquals(Optional.of(60L), subject.getTeamByIssueTypeId(6L));
    }

    @Test
    public void addProjectTeamForIssueType() {
        assertTrue(subject.getTeamsByIssueTypes().isEmpty());

        subject.addProjectTeamForIssueType(20L, 2L);
        assertEquals(1, subject.getTeamsByIssueTypes().size());
        assertEquals(Long.valueOf(20), subject.getTeamsByIssueTypes().get(0).getTeamId());
        assertEquals(Long.valueOf(2), subject.getTeamsByIssueTypes().get(0).getIssueTypeId());

        subject.addProjectTeamForIssueType(40L, 4L);
        assertEquals(2, subject.getTeamsByIssueTypes().size());
        assertEquals(Long.valueOf(40), subject.getTeamsByIssueTypes().get(1).getTeamId());
        assertEquals(Long.valueOf(4), subject.getTeamsByIssueTypes().get(1).getIssueTypeId());
    }

    @Test
    public void removeProjectTeamForIssueType() {
        assertTrue(subject.getTeamsByIssueTypes().isEmpty());

        subject.addProjectTeamForIssueType(20L, 2L);
        subject.addProjectTeamForIssueType(40L, 4L);
        subject.addProjectTeamForIssueType(60L, 6L);

        projectFilterConfigurationRepository.save(subject);

        assertEquals(3, subject.getTeamsByIssueTypes().size());

        subject.removeDefaultTeamForIssueType(subject.getTeamsByIssueTypes().get(0));
        subject.removeDefaultTeamForIssueType(subject.getTeamsByIssueTypes().get(0));

        assertEquals(1, subject.getTeamsByIssueTypes().size());
        assertEquals(Long.valueOf(60), subject.getTeamsByIssueTypes().get(0).getTeamId());
        assertEquals(Long.valueOf(6), subject.getTeamsByIssueTypes().get(0).getIssueTypeId());
    }

}
