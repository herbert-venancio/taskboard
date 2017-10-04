package objective.taskboard.jira;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import objective.taskboard.jira.data.Status;
import objective.taskboard.jira.data.StatusCategory;
import objective.taskboard.jira.endpoint.AuthorizedJiraEndpoint;
import objective.taskboard.jira.endpoint.JiraEndpoint;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;
import objective.taskboard.testUtils.JiraMockServer;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AuthorizedJiraEndpointTest.Configuration.class)
public class AuthorizedJiraEndpointTest {

    private static final String JIRA_MASTER_USERNAME = "master";
    private static final String JIRA_MASTER_PASSWORD = "password";
    private static final String JIRA_USER_USERNAME = "user";
    private static final String JIRA_USER_PASSWORD = "pass";

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
    }

    @Autowired
    private JiraMockServer jira;

    @MockBean
    private JiraProperties jiraProperties;

    @Autowired
    @Qualifier("jiraEndpointAsMaster")
    private AuthorizedJiraEndpoint jiraEndpointAsMaster;

    @Before
    public void setupSecurity() {
        // master user
        JiraProperties.Lousa lousa = new JiraProperties.Lousa();
        lousa.setUsername(JIRA_MASTER_USERNAME);
        lousa.setPassword(JIRA_MASTER_PASSWORD);
        doReturn(lousa).when(jiraProperties).getLousa();

        // logged user
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        doReturn(JIRA_USER_USERNAME).when(authentication).getName();
        doReturn(JIRA_USER_PASSWORD).when(authentication).getCredentials();
        doReturn(true).when(authentication).isAuthenticated();
        doReturn(authentication).when(securityContext).getAuthentication();
        SecurityContextHolder.setContext(securityContext);
    }

    @Before
    public void setupProperties() {
        doReturn("http://localhost:" + jira.port()).when(jiraProperties).getUrl();
    }

    @Test
    public void getStatusCategories() {
        List<StatusCategory> categories = jiraEndpointAsMaster.request(StatusCategory.Service.class).all();
        assertThat(categories.get(0).id, is(1L));
        assertThat(categories.get(0).key, is("undefined"));
        assertThat(categories.get(0).name, is("No Category"));
        assertThat(categories.get(0).colorName, is("medium-gray"));
        assertThat(categories.get(1).id, is(2L));
        assertThat(categories.get(2).id, is(4L));
        assertThat(categories.get(3).id, is(3L));
    }

    @Test
    public void getStatuses() {
        List<Status> statuses = jiraEndpointAsMaster.request(Status.Service.class).all();
        assertThat(statuses.get(0).name, is("To Do"));
        assertThat(statuses.get(0).statusCategory.name, is("To Do"));
    }
}
