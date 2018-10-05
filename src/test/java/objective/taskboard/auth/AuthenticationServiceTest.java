package objective.taskboard.auth;

import static java.util.Arrays.asList;
import static objective.taskboard.testUtils.AssertUtils.collectionToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import objective.taskboard.auth.AuthenticationService.AuthenticationResult;
import objective.taskboard.auth.LoggedUserDetails.JiraRole;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.data.plugin.UserDetail;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.Lousa;
import objective.taskboard.testUtils.FixedClock;
import objective.taskboard.user.TaskboardUser;
import objective.taskboard.user.TaskboardUserRepository;

public class AuthenticationServiceTest {
    
    private JiraService jiraService = mock(JiraService.class);
    private TaskboardUserRepository taskboardUserRepository = mock(TaskboardUserRepository.class);
    private FixedClock clock = new FixedClock();
    private JiraProperties jiraProperties = new JiraProperties();
    private AuthenticationService subject = new AuthenticationService(jiraProperties, jiraService, taskboardUserRepository, clock);
    
    private static final Instant NOW = Instant.parse("2018-01-20T10:15:30.00Z"); 
    
    @Before
    public void setup() {
        clock.setNow(NOW);
        jiraProperties.setLousa(new Lousa());

        doThrow(new RuntimeException("Failed to login")).when(jiraService).authenticate(any(), any());
    }

    @Test
    public void authenticate_validCredentials_shouldAuthenticate() {
        TaskboardUser taskboardUser = new TaskboardUser("jose");
        taskboardUser.setAdmin(true);
        
        doNothing().when(jiraService).authenticate("jose", "123");
        when(taskboardUserRepository.getByUsername("jose")).thenReturn(Optional.of(taskboardUser));
        when(jiraService.getUserRoles("jose")).thenReturn(asList(
                new UserDetail.Role(1L, "dev", "PX"), 
                new UserDetail.Role(2L, "adm", "SP")));

        AuthenticationResult result = subject.authenticate("jose", "123");

        assertTrue(result.isSuccess());
        assertNotNull(result.getPrincipal());

        assertEquals("jose", result.getPrincipal().getUsername());
        assertTrue(result.getPrincipal().isAdmin());
        assertRoles(result.getPrincipal().getJiraRoles(),
                "1 | dev | PX",
                "2 | adm | SP");
        
        assertEquals(Optional.of(NOW), taskboardUser.getLastLogin());
    }
    
    @Test
    public void authenticate_firstLogin_shouldCreateTaskboardUser() {
        doNothing().when(jiraService).authenticate("jose", "123");
        when(taskboardUserRepository.getByUsername("jose")).thenReturn(Optional.empty());
        
        AuthenticationResult result = subject.authenticate("jose", "123");
        
        assertTrue(result.isSuccess());
        assertNotNull(result.getPrincipal());
        assertEquals("jose", result.getPrincipal().getUsername());
        assertFalse(result.getPrincipal().isAdmin());

        ArgumentCaptor<TaskboardUser> newTaskboardUser = ArgumentCaptor.forClass(TaskboardUser.class);
        verify(taskboardUserRepository).add(newTaskboardUser.capture());
        assertEquals("jose", newTaskboardUser.getValue().getUsername());
        assertEquals(Optional.of(NOW), newTaskboardUser.getValue().getLastLogin());
    }
    
    @Test
    public void authenticate_jiraUserFirstLogin_shouldCreateTaskboardUserAsAdmin() {
        jiraProperties.getLousa().setUsername("mary");
        doNothing().when(jiraService).authenticate("mary", "123");
        when(taskboardUserRepository.getByUsername("mary")).thenReturn(Optional.empty());
        
        AuthenticationResult result = subject.authenticate("mary", "123");
        
        assertTrue(result.isSuccess());
        assertTrue(result.getPrincipal().isAdmin());
        
        ArgumentCaptor<TaskboardUser> newTaskboardUser = ArgumentCaptor.forClass(TaskboardUser.class);
        verify(taskboardUserRepository).add(newTaskboardUser.capture());
        assertEquals("mary", newTaskboardUser.getValue().getUsername());
        assertTrue(newTaskboardUser.getValue().isAdmin());
    }
    
    @Test
    public void authenticate_invalidCredentials_shouldFail() {
        AuthenticationResult result = subject.authenticate("hugo", "123");
        
        assertFalse(result.isSuccess());
        assertNull(result.getPrincipal());
        assertEquals("Failed to login", result.getMessage());
    }

    private static void assertRoles(List<JiraRole> actualRoles, String... expectedRoles) {
        Function<JiraRole, String> roleToString = r -> r.id + " | " + r.name + " | " + r.projectKey;
        assertEquals(StringUtils.join(expectedRoles, "\n"), collectionToString(actualRoles, roleToString, "\n"));
    }

}
