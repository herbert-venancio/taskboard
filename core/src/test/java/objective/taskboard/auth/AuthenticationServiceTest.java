package objective.taskboard.auth;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.testUtils.AssertUtils.collectionToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
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

    private final String AUTHENTICATION_ERROR_MESSAGE = "Failed to login";
    private AuthenticationResult authenticateResult;

    private AuthenticationService subject;

    @Test
    public void authenticate_validCredentials_shouldAuthenticate() {
        given()
            .user("jose")
                .withPassword("123")
                .existsOnJiraWithRoles(
                        role().id(1).name("dev").projectKey("PX"),
                        role().id(2).name("adm").projectKey("SP"))
                .existsOnTaskboardWithAdminPermissionEquals(true)

        .whenExecute(() -> authenticateResult = subject.authenticate("jose", "123"))

        .thenAssert()
            .taskboardUserLastLoginWasUpdatedToNow()
            .successEquals(true)
            .messageEquals(null)
            .principalEquals()
                .username("jose")
                .admin(true)
                .roles("1 | dev | PX",
                       "2 | adm | SP");
    }

    @Test
    public void authenticate_firstLogin_shouldCreateTaskboardUser() {
        given()
            .usernamePropertyToConnectWithJiraEquals("mary")
            .user("jose")
                .withPassword("123")
                .existsOnJiraWithRoles(
                        role().id(1).name("dev").projectKey("PX"))
                .doenstExistsOnTaskboard()

        .whenExecute(() -> authenticateResult = subject.authenticate("jose", "123"))

        .thenAssert()
            .taskboardUserWasCreated()
            .taskboardUserLastLoginWasUpdatedToNow()
            .successEquals(true)
            .messageEquals(null)
            .principalEquals()
                .username("jose")
                .admin(false)
                .roles("1 | dev | PX");
    }

    @Test
    public void authenticate_jiraUserFirstLogin_shouldCreateTaskboardUserAsAdmin() {
        given()
            .usernamePropertyToConnectWithJiraEquals("mary")
            .user("mary")
                .withPassword("123")
                .existsOnJiraWithRoles(
                        role().id(1).name("dev").projectKey("PX"))
                .doenstExistsOnTaskboard()

        .whenExecute(() -> authenticateResult = subject.authenticate("mary", "123"))

        .thenAssert()
            .taskboardUserWasCreated()
            .taskboardUserLastLoginWasUpdatedToNow()
            .successEquals(true)
            .messageEquals(null)
            .principalEquals()
                .username("mary")
                .admin(true)
                .roles("1 | dev | PX");
    }

    @Test
    public void authenticate_invalidCredentials_shouldFail() {
        given()
            .user("jose")
                .withPassword("123")
                .doenstExistsOnJira()

        .whenExecute(() -> authenticateResult = subject.authenticate("jose", "123"))

        .thenAssert()
            .successEquals(false)
            .messageEquals(AUTHENTICATION_ERROR_MESSAGE)
            .principalEquals()
                .nullValue();
    }

    private AuthenticationServiceTestDSL given() {
        return new AuthenticationServiceTestDSL();
    }

    private RoleBuilder role() {
        return new RoleBuilder();
    }

    private class AuthenticationServiceTestDSL {

        private JiraProperties jiraProperties = new JiraProperties();
        private JiraService jiraService = mock(JiraService.class);
        private TaskboardUserRepository taskboardUserRepository = mock(TaskboardUserRepository.class);
        private FixedClock clock = new FixedClock();

        private final Instant NOW = Instant.parse("2018-01-20T10:15:30.00Z");

        private String username;
        private String password;
        private TaskboardUser taskboardUser;

        public AuthenticationServiceTestDSL() {
            subject = new AuthenticationService(jiraProperties, jiraService, taskboardUserRepository, clock);

            clock.setNow(NOW);
            jiraProperties.setLousa(new Lousa());
            doThrow(new IllegalStateException("This method must be mocked")).when(jiraService).authenticate(any(), any());
        }

        public AuthenticationServiceTestDSL usernamePropertyToConnectWithJiraEquals(String propertyUsername) {
            jiraProperties.getLousa().setUsername(propertyUsername);
            return this;
        }

        public AuthenticationServiceTestDSLUser user(String username) {
            return new AuthenticationServiceTestDSLUser(username);
        }

        public AuthenticationServiceTestDSL whenExecute(Runnable method) {
            method.run();
            return this;
        }

        public AuthenticationServiceTestDSLAsserter thenAssert() {
            return new AuthenticationServiceTestDSLAsserter();
        }

        private class AuthenticationServiceTestDSLUser {

            public AuthenticationServiceTestDSLUser(String user) {
                username = user;
                taskboardUser = new TaskboardUser(username);
            }

            public AuthenticationServiceTestDSLUser withPassword(String pass) {
                password = pass;
                return this;
            }

            public AuthenticationServiceTestDSLUser existsOnJiraWithRoles(RoleBuilder... roles) {
                doNothing().when(jiraService).authenticate(username, password);
                when(jiraService.getUserRoles(username)).thenReturn(stream(roles).map(r -> r.build()).collect(toList()));
                return this;
            }

            public AuthenticationServiceTestDSLUser doenstExistsOnJira() {
                doThrow(new RuntimeException(AUTHENTICATION_ERROR_MESSAGE)).when(jiraService).authenticate(username, password);
                return this;
            }

            public AuthenticationServiceTestDSLUser existsOnTaskboardWithAdminPermissionEquals(boolean isAdmin) {
                taskboardUser.setAdmin(isAdmin);
                when(taskboardUserRepository.getByUsername(username)).thenReturn(Optional.of(taskboardUser));
                return this;
            }

            public AuthenticationServiceTestDSLUser doenstExistsOnTaskboard() {
                when(taskboardUserRepository.getByUsername(username)).thenReturn(Optional.empty());
                return this;
            }

            public AuthenticationServiceTestDSL whenExecute(Runnable method) {
                return AuthenticationServiceTestDSL.this.whenExecute(method);
            }

        }

        private class AuthenticationServiceTestDSLAsserter {

            private ArgumentCaptor<TaskboardUser> newTaskboardUser;

            public AuthenticationServiceTestDSLAsserter taskboardUserWasCreated() {
                newTaskboardUser = ArgumentCaptor.forClass(TaskboardUser.class);
                verify(taskboardUserRepository).add(newTaskboardUser.capture());
                assertEquals(username, newTaskboardUser.getValue().getUsername());
                return this;
            }

            public AuthenticationServiceTestDSLAsserter taskboardUserLastLoginWasUpdatedToNow() {
                if (newTaskboardUser == null)
                    assertEquals(Optional.of(NOW), taskboardUser.getLastLogin());
                else
                    assertEquals(Optional.of(NOW), newTaskboardUser.getValue().getLastLogin());
                return this;
            }

            public AuthenticationServiceTestDSLAsserter successEquals(boolean success) {
                assertEquals(success, authenticateResult.isSuccess());
                return this;
            }

            public AuthenticationServiceTestDSLAsserter messageEquals(String message) {
                assertEquals(message, authenticateResult.getMessage());
                return this;
            }

            public AuthenticationServiceTestDSLAsserterPrincipal principalEquals() {
                return new AuthenticationServiceTestDSLAsserterPrincipal();
            }

            private class AuthenticationServiceTestDSLAsserterPrincipal {

                public AuthenticationServiceTestDSLAsserterPrincipal nullValue() {
                    assertNull(authenticateResult.getPrincipal());
                    return this;
                }

                public AuthenticationServiceTestDSLAsserterPrincipal username(String user) {
                    assertEquals(user, authenticateResult.getPrincipal().getUsername());
                    return this;
                }

                public AuthenticationServiceTestDSLAsserterPrincipal admin(boolean isAdmin) {
                    assertEquals(isAdmin, authenticateResult.getPrincipal().isAdmin());
                    return this;
                }

                public AuthenticationServiceTestDSLAsserterPrincipal roles(String... expectedRoles) {
                    Function<JiraRole, String> roleToString = r -> r.id + " | " + r.name + " | " + r.projectKey;
                    assertEquals(StringUtils.join(expectedRoles, "\n"), collectionToString(authenticateResult.getPrincipal().getJiraRoles(), roleToString, "\n"));
                    return this;
                }
            }

        }

    }

    private static class RoleBuilder {
        private long id;
        private String name;
        private String projectKey;

        public RoleBuilder id(long id) {
            this.id = id;
            return this;
        }

        public RoleBuilder name(String name) {
            this.name = name;
            return this;
        }

        public RoleBuilder projectKey(String projectKey) {
            this.projectKey = projectKey;
            return this;
        }

        public UserDetail.Role build() {
            return new UserDetail.Role(id, name, projectKey);
        }
    }

}
