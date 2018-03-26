package objective.taskboard.jira;

import static objective.taskboard.jira.AuthorizedJiraEndpointTest.JIRA_MASTER_PASSWORD;
import static objective.taskboard.jira.AuthorizedJiraEndpointTest.JIRA_MASTER_USERNAME;
import static objective.taskboard.jira.AuthorizedJiraEndpointTest.JIRA_USER_PASSWORD;
import static objective.taskboard.jira.AuthorizedJiraEndpointTest.JIRA_USER_USERNAME;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import objective.taskboard.config.LoggedInUserLocaleKeyGenerator;
import objective.taskboard.config.SpringContextBridge;
import objective.taskboard.jira.client.JiraFieldDataDto;
import objective.taskboard.testUtils.JiraMockServer;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        AuthorizedJiraEndpointTest.Configuration.class
        , FieldMetadataServiceTest.Configuration.class})
public class FieldMetadataServiceTest {

    public static class Configuration {
        @Bean
        public FieldMetadataService fieldMetadataService() {
            return new FieldMetadataService();
        }

        @Bean
        public LoggedInUserLocaleKeyGenerator loggedInUserLocaleKeyGenerator() {
            return new LoggedInUserLocaleKeyGenerator();
        }
    }

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private JiraMockServer jira;

    @MockBean
    private JiraProperties jiraProperties;

    @Autowired
    private FieldMetadataService fieldMetadataService;

    @Before
    public void setupSpringContextBridge() {
        new SpringContextBridge().setApplicationContext(applicationContext);
    }

    @After
    public void disposeSpringContextBridge() {
        new SpringContextBridge().setApplicationContext(null);
    }

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
    public void getAll() {
        List<JiraFieldDataDto> list1 = fieldMetadataService.getFieldsMetadata();
        List<JiraFieldDataDto> list2 = fieldMetadataService.getFieldsMetadata();

        assertTrue(list1 == list2);
    }

    @Test
    public void getAllAsUser() {
        List<JiraFieldDataDto> list1 = fieldMetadataService.getFieldsMetadataAsUser();
        List<JiraFieldDataDto> list2 = fieldMetadataService.getFieldsMetadataAsUser();

        assertTrue(list1 == list2);
    }
}
