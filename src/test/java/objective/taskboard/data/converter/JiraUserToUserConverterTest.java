package objective.taskboard.data.converter;

import objective.taskboard.data.User;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.ProjectCache;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.data.JiraUser;
import objective.taskboard.jira.endpoint.JiraEndpoint;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.testUtils.JiraMockServer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.URI;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static objective.taskboard.jira.AuthorizedJiraEndpointTest.JIRA_MASTER_PASSWORD;
import static objective.taskboard.jira.AuthorizedJiraEndpointTest.JIRA_MASTER_USERNAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = JiraUserToUserConverterTest.Configuration.class)
public class JiraUserToUserConverterTest {

    public static class Configuration {
        @Bean
        public JiraMockServer jira() {
            return new JiraMockServer().port(0).startAndWait();
        }

        @Bean
        public JiraEndpoint jiraEndpoint() {
            return new JiraEndpoint();
        }

        @Bean
        public JiraEndpointAsMaster jiraEndpointAsMaster() {
            return new JiraEndpointAsMaster();
        }

        @Bean
        public JiraEndpointAsLoggedInUser jiraEndpointAsUser() {
            return new JiraEndpointAsLoggedInUser();
        }

        @Bean
        public JiraUserToUserConverter jiraUserToUserConverter() {
            return new JiraUserToUserConverter();
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
    private ProjectFilterConfigurationCachedRepository projectFilterConfigurationCachedRepository;

    @SpyBean
    private ProjectCache projectCache;

    @Autowired
    private JiraUserToUserConverter subject;

    @Before
    public void setupProperties() {
        doReturn("http://localhost:" + jira.port()).when(jiraProperties).getUrl();
        JiraProperties.CustomField.CustomFieldDetails coAssignees = new JiraProperties.CustomField.CustomFieldDetails();
        coAssignees.setId("customfield_11456");
        JiraProperties.CustomField customField = new JiraProperties.CustomField();
        customField.setCoAssignees(coAssignees);
        doReturn(customField).when(jiraProperties).getCustomfield();
    }

    @Before
    public void setupMocks() {
        // master user
        JiraProperties.Lousa lousa = new JiraProperties.Lousa();
        lousa.setUsername(JIRA_MASTER_USERNAME);
        lousa.setPassword(JIRA_MASTER_PASSWORD);
        doReturn(lousa).when(jiraProperties).getLousa();

        JiraProject jiraProject = new JiraProject(null, "TASKB", null, null);
        doReturn(singletonList(jiraProject)).when(projectCache).getAllProjects();
    }

    public JiraUser setupLoggedUser(String username) {
        String password = "123";
        String email = username + "@mail.com";

        // setup logged user
        Authentication authentication = mock(Authentication.class);
        doReturn(username).when(authentication).getName();
        doReturn(password).when(authentication).getCredentials();
        doReturn(true).when(authentication).isAuthenticated();
        SecurityContext securityContext = mock(SecurityContext.class);
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);

        // getLoggedUser
        Map<String, URI> avatarUrls = singletonMap("48x48",
                URI.create("http://www.gravatar.com/avatar/c2b78b1ds52b346ff4528044ee123cc74?d=mm&s=48"));
        return new JiraUser(username, password, email, avatarUrls);
    }

    @Test
    public void givenUserHasCustomerRoleInJira_thenCustomer() {
        JiraUser jiraUser = setupLoggedUser("albert.customer");

        User user = subject.convert(jiraUser);

        assertThat(user.isCustomer).isTrue();
    }

    @Test
    public void givenUserHasDeveloperRoleInJira_thenNotCustomer() {
        JiraUser jiraUser = setupLoggedUser("thomas.developer");

        User user = subject.convert(jiraUser);

        assertThat(user.isCustomer).isFalse();
    }

    @Test
    public void givenUserHasReviewerRoleInJira_thenNotCustomer() {
        JiraUser jiraUser = setupLoggedUser("graham.reviewer");

        User user = subject.convert(jiraUser);

        assertThat(user.isCustomer).isFalse();
    }
}
