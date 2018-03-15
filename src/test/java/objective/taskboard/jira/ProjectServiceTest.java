package objective.taskboard.jira;

import static java.util.Arrays.asList;
import static objective.taskboard.config.CacheConfiguration.ALL_PROJECTS;
import static objective.taskboard.jira.AuthorizedJiraEndpointTest.JIRA_MASTER_PASSWORD;
import static objective.taskboard.jira.AuthorizedJiraEndpointTest.JIRA_MASTER_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import objective.taskboard.domain.Project;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.data.Version;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.repository.ProjectFilterConfigurationRepository;
import objective.taskboard.testUtils.JiraMockServer;

@RunWith(SpringRunner.class)
@EnableCaching
@ContextConfiguration(classes = {
        AuthorizedJiraEndpointTest.Configuration.class
        , ProjectServiceTest.Configuration.class
        , CacheConfiguration.class})
public class ProjectServiceTest {

    private final static String PROJECT_WITHOUT_NO_ACCESS = "PROJ4";
    private final static String PROJECT_ARCHIVED = "PROJ1";
    private final static String PROJECT_REGULAR = "PROJ2";

    public static class Configuration {
        @Bean
        public ProjectFilterConfigurationCachedRepository projectFilterConfigurationCachedRepository() {
            return new ProjectFilterConfigurationCachedRepository();
        }
        @Bean
        public ProjectCache projectCache() {
            return new ProjectCache() {
                @Override
                public Map<String, Project> getUserProjects() {
                    return generateUserProjects();
                }
            };
        }
        @Bean
        public ProjectService projectService() {
            return new ProjectService();
        }
    }

    @Autowired
    private JiraMockServer jira;

    @MockBean
    private JiraProperties jiraProperties;

    @MockBean
    private ProjectFilterConfigurationRepository projectFilterRepository;

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectFilterConfigurationCachedRepository;

    @Autowired
    private ProjectService projectService;

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
        proj1.setProjectKey(PROJECT_ARCHIVED);
        ProjectFilterConfiguration proj2 = new ProjectFilterConfiguration();
        proj2.setProjectKey(PROJECT_REGULAR);
        ProjectFilterConfiguration proj3 = new ProjectFilterConfiguration();
        proj3.setProjectKey("PROJ3");
        ProjectFilterConfiguration proj4 = new ProjectFilterConfiguration();
        proj4.setProjectKey(PROJECT_WITHOUT_NO_ACCESS);

        List<ProjectFilterConfiguration> projectList = asList(
                taskb
                , proj1
                , proj2
                , proj3
                , proj4);
        doReturn(projectList).when(projectFilterRepository).findAll();
        projectFilterConfigurationCachedRepository.loadCache();
    }

    @Test
    public void getVersion() {
        Version expected = new Version("12550", "1.0");
        Version actual = projectService.getVersion("12550");

        assertThat(actual).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void cacheGetVersion() {
        Version version1 = projectService.getVersion("12550");
        Version version2 = projectService.getVersion("12550");

        assertTrue(version1 == version2);
    }

    @Test
    public void evict() {
        Version version1 = projectService.getVersion("12550");
        cacheManager.getCache(ALL_PROJECTS).clear();
        Version version2 = projectService.getVersion("12550");

        assertTrue(version1 != version2);
    }

    @Test
    public void whenGetProjectsOnTaskboard_dontShowArchivedProjects() {
        List<Project> visibleProjectsOnTaskboard = projectService.getVisibleProjectsOnTaskboard();
        assertFalse(visibleProjectsOnTaskboard.stream().anyMatch(p -> p.getKey().equals(PROJECT_ARCHIVED)));
        assertFalse(visibleProjectsOnTaskboard.stream().anyMatch(p -> p.isArchived() == true));
    }

    @Test
    public void whenCheckIfAProjectIsVisibleOnTaskboard_ifIsArchived_returnFalse() {
        assertFalse(projectService.isProjectVisibleOnTaskboard(PROJECT_ARCHIVED));
        assertTrue(projectService.isProjectVisibleOnTaskboard(PROJECT_REGULAR));
    }

    @Test
    public void whenProjectExists_ifTheUserHasAccess_returnTrue() {
        assertTrue(projectService.existisAndHasAccess(PROJECT_ARCHIVED));
        assertTrue(projectService.existisAndHasAccess(PROJECT_REGULAR));

        assertTrue(projectFilterRepository.findAll().stream().anyMatch(p -> PROJECT_WITHOUT_NO_ACCESS.equals(p.getProjectKey()) ));
        assertFalse(projectService.existisAndHasAccess(PROJECT_WITHOUT_NO_ACCESS));
    }

    private static Map<String, Project> generateUserProjects() {
        Map<String, Project> projectsMap = new HashMap<String, Project>();

        Project p1 = new Project();
        p1.setKey(PROJECT_ARCHIVED);
        p1.setName("PROJECT 1");
        p1.setArchived(true);

        Project p2 = new Project();
        p2.setKey(PROJECT_REGULAR);
        p2.setName("PROJECT 2");
        p2.setArchived(false);

        Project p3 = new Project();
        p3.setKey("PROJ3");
        p3.setName("PROJECT 3");
        p3.setArchived(false);

        projectsMap.put(p1.getKey(), p1);
        projectsMap.put(p2.getKey(), p2);
        projectsMap.put(p3.getKey(), p3);

        return projectsMap;
    }
}