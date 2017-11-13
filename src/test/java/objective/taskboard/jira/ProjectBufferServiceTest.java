package objective.taskboard.jira;

import static java.util.Arrays.asList;
import static objective.taskboard.config.CacheConfiguration.ALL_PROJECTS;
import static objective.taskboard.jira.AuthorizedJiraEndpointTest.JIRA_MASTER_PASSWORD;
import static objective.taskboard.jira.AuthorizedJiraEndpointTest.JIRA_MASTER_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.data.Version;
import objective.taskboard.repository.ProjectFilterConfigurationRepository;
import objective.taskboard.testUtils.JiraMockServer;

@RunWith(SpringRunner.class)
@EnableCaching
@ContextConfiguration(classes = {
        AuthorizedJiraEndpointTest.Configuration.class
        , ProjectBufferServiceTest.Configuration.class
        , CacheConfiguration.class})
public class ProjectBufferServiceTest {

    public static class Configuration {
        @Bean
        public ProjectBufferService projectBufferService() {
            return new ProjectBufferService();
        }
    }

    @Autowired
    private JiraMockServer jira;

    @MockBean
    private JiraProperties jiraProperties;

    @MockBean
    private ProjectFilterConfigurationRepository repository;

    @Autowired
    private ProjectBufferService projectBufferService;

    @Autowired
    private CacheManager cacheManager;

    @Before
    public void setupSecurity() {
        // master user
        JiraProperties.Lousa lousa = new JiraProperties.Lousa();
        lousa.setUsername(JIRA_MASTER_USERNAME);
        lousa.setPassword(JIRA_MASTER_PASSWORD);
        doReturn(lousa).when(jiraProperties).getLousa();
    }

    @Before
    public void setupProperties() {
        doReturn("http://localhost:" + jira.port()).when(jiraProperties).getUrl();
    }

    @Before
    public void mockRepository() {
        ProjectFilterConfiguration taskb = new ProjectFilterConfiguration();
        taskb.setProjectKey("TASKB");
        ProjectFilterConfiguration proj1 = new ProjectFilterConfiguration();
        proj1.setProjectKey("PROJ1");
        ProjectFilterConfiguration proj2 = new ProjectFilterConfiguration();
        proj2.setProjectKey("PROJ2");

        List<ProjectFilterConfiguration> projectList = asList(
                taskb
                , proj1
                , proj2);
        doReturn(projectList).when(repository).findAll();
    }

    @Test
    public void cacheGetProjects() {
        List<JiraProject> all1 = projectBufferService.getAllProjects();
        List<JiraProject> all2 = projectBufferService.getAllProjects();

        assertTrue(all1 == all2);
    }

    @Test
    public void evict() {
        List<JiraProject> all1 = projectBufferService.getAllProjects();
        cacheManager.getCache(ALL_PROJECTS).clear();
        List<JiraProject> all2 = projectBufferService.getAllProjects();

        assertTrue(all1 != all2);
    }

    @Test
    public void getVersion() {
        Version expected = new Version("12550", "1.0");
        Version actual = projectBufferService.getVersion("12550");

        assertThat(actual).isEqualToComparingFieldByField(expected);
    }
}
