package objective.taskboard.auth;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.authorizer.permission.ImpersonatePermission.IMPERSONATE_HEADER;
import static objective.taskboard.testUtils.AssertUtils.collectionToString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.api.client.util.Maps;

import objective.taskboard.auth.AuthenticationService.AuthenticationResult;
import objective.taskboard.auth.LoggedUserDetails.JiraRole;
import objective.taskboard.auth.authorizer.permission.ImpersonatePermission;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.data.JiraUser;
import objective.taskboard.jira.data.plugin.UserDetail;
import objective.taskboard.user.TaskboardUser;
import objective.taskboard.user.TaskboardUserService;

public class AuthenticationServiceTest {

    private final String AUTHENTICATION_ERROR_MESSAGE = "Failed to login";
    private AuthenticationResult authenticateResult;

    private AuthenticationService subject;

    @Test
    public void authenticate_validCredentials_shouldAuthenticate() {
        given()
            .user("jose")
                .existsOnJiraWithRoles(
                        role().id(1).name("dev").projectKey("PX"),
                        role().id(2).name("adm").projectKey("SP"))

        .whenExecute(() -> authenticateResult = subject.authenticate("jose", "some-pass"))

        .then()
            .assertUser("jose")
                .taskboardUserLastLoginWasUpdatedToNow()
                .success(true)
                .message(null)
                .principal()
                    .defineUsername("jose")
                    .roles("1 | dev | PX",
                           "2 | adm | SP");
    }

    @Test
    public void authenticate_invalidCredentials_shouldFail() {
        given()
            .user("jose")
                .doenstExistsOnJira()

        .whenExecute(() -> authenticateResult = subject.authenticate("jose", "some-pass"))

        .then()
            .assertUser("jose")
                .success(false)
                .message(AUTHENTICATION_ERROR_MESSAGE)
                .principal()
                    .nullValue();
    }

    @Test
    public void givenCorrectImpersonateValue_ifUserToBeImpersonatedExists_andLoggedInUserHasPermission_thenAuthenticateWithImpersonateValueSetted() {
        given()
            .requestWithHeaderEquals(IMPERSONATE_HEADER, "john")

            .user("john")
                .existsOnJiraWithRoles(
                        role().id(2).name("adm").projectKey("TZ"))

            .user("mary")
                .existsOnJiraWithRoles(
                        role().id(1).name("dev").projectKey("PX"))
                .withPermissionToImpersonate("john")

        .whenExecute(() -> authenticateResult = subject.authenticate("mary", "some-pass"))

        .then()
            .assertUser("mary")
                .taskboardUserLastLoginWasUpdatedToNow()
                .success(true)
                .message(null)
                .principal()
                    .realUsername("mary")
                    .defineUsername("john")
                    .roles("2 | adm | TZ");
    }

    @Test
    public void givenCorrectImpersonateValue_ifLoggedInUserDoesntHavePermission_thenAuthenticateWithoutImpersonateValueSetted() {
        given()
            .requestWithHeaderEquals(IMPERSONATE_HEADER, "john")

            .user("john")
                .existsOnJiraWithRoles(
                        role().id(2).name("adm").projectKey("TZ"))

            .user("mary")
                .existsOnJiraWithRoles(
                        role().id(1).name("dev").projectKey("PX"))
                .withoutPermissionToImpersonate("john")

        .whenExecute(() -> authenticateResult = subject.authenticate("mary", "123"))

        .then()
            .assertUser("mary")
                .taskboardUserLastLoginWasUpdatedToNow()
                .success(true)
                .message(null)
                .principal()
                    .defineUsername("mary")
                    .roles("1 | dev | PX");
    }

    @Test
    public void givenCorrectImpersonateValue_ifLoggedInUserHasPermission_butUserToBeImpersonatedDoenstExists_thenAuthenticationMustFail() {
        given()
            .requestWithHeaderEquals(IMPERSONATE_HEADER, "jose")

            .user("jose")
                .doenstExistsOnJira()

            .user("john")
                .existsOnJiraWithRoles(
                        role().id(1).name("dev").projectKey("PX"))
                .withPermissionToImpersonate("jose")

        .whenExecute(() -> authenticateResult = subject.authenticate("john", "123"))

        .then()
            .assertUser("john")
                .taskboardUserLastLoginWasNotUpdated()
                .success(false)
                .message("User \"jose\" not found.")
                .principal()
                    .nullValue();
    }

    @Test
    public void givenBlankImpersonateValue_ifUserToBeImpersonatedExists_andLoggedInUserHasPermission_thenAuthenticationMustFail() {
        String blank = "";

        given()
            .requestWithHeaderEquals(IMPERSONATE_HEADER, blank)

            .user("john")
                .existsOnJiraWithRoles(
                        role().id(1).name("dev").projectKey("PX"))
                .withPermissionToImpersonate(blank)

        .whenExecute(() -> authenticateResult = subject.authenticate("john", "123"))

        .then()
            .assertUser("john")
                .taskboardUserLastLoginWasNotUpdated()
                .success(false)
                .message("\""+ IMPERSONATE_HEADER +"\" parameter cannot be blank.")
                .principal()
                    .nullValue();
    }

    private AuthenticationServiceTestDSL given() {
        return new AuthenticationServiceTestDSL();
    }

    private RoleBuilder role() {
        return new RoleBuilder();
    }

    private class AuthenticationServiceTestDSL {

        private HttpServletRequest request = mock(HttpServletRequest.class);
        private JiraService jiraService = mock(JiraService.class);
        private ImpersonatePermission impersonatePermission = mock(ImpersonatePermission.class);
        private TaskboardUserService taskboardUserService = mock(TaskboardUserService.class);

        private Map<String, TaskboardUser> registredUsers = Maps.newHashMap();

        public AuthenticationServiceTestDSL() {
            doThrow(new IllegalStateException("This method must be mocked")).when(jiraService).authenticate(any(), any());
            when(jiraService.getJiraUserAsMaster(anyString())).thenReturn(Optional.empty());

            subject = new AuthenticationService(request, jiraService, impersonatePermission, taskboardUserService);
        }

        public AuthenticationServiceTestDSLUser user(String username) {
            return new AuthenticationServiceTestDSLUser(username);
        }

        public AuthenticationServiceTestDSL requestWithHeaderEquals(String headerKey, String headerValue) {
            when(request.getHeader(headerKey)).thenReturn(headerValue);
            return this;
        }

        public AuthenticationServiceTestDSL whenExecute(Runnable method) {
            method.run();
            return this;
        }

        public AuthenticationServiceTestDSL then() {
            return this;
        }

        public AuthenticationServiceTestDSLAsserter assertUser(String username) {
            return new AuthenticationServiceTestDSLAsserter(username);
        }

        private class AuthenticationServiceTestDSLUser {

            private TaskboardUser taskboardUser;

            public AuthenticationServiceTestDSLUser(String username) {
                taskboardUser = new TaskboardUser(username);
                registredUsers.put(username, taskboardUser);
            }

            public AuthenticationServiceTestDSLUser existsOnJiraWithRoles(RoleBuilder... roles) {
                doNothing().when(jiraService).authenticate(eq(taskboardUser.getUsername()), anyString());
                when(jiraService.getJiraUserAsMaster(eq(taskboardUser.getUsername()))).thenReturn(Optional.of(mock(JiraUser.class)));
                when(jiraService.getUserRoles(taskboardUser.getUsername())).thenReturn(stream(roles).map(r -> r.build()).collect(toList()));
                when(taskboardUserService.getTaskboardUser(taskboardUser.getUsername())).thenReturn(taskboardUser);
                return this;
            }

            public AuthenticationServiceTestDSLUser doenstExistsOnJira() {
                doThrow(new RuntimeException(AUTHENTICATION_ERROR_MESSAGE)).when(jiraService).authenticate(eq(taskboardUser.getUsername()), anyString());
                return this;
            }

            public AuthenticationServiceTestDSLUser withPermissionToImpersonate(String username) {
                when(impersonatePermission.isAuthorized(any(), eq(username))).thenReturn(true);
                return this;
            }

            public AuthenticationServiceTestDSLUser withoutPermissionToImpersonate(String username) {
                when(impersonatePermission.isAuthorized(any(), eq(username))).thenReturn(false);
                return this;
            }

            public AuthenticationServiceTestDSLUser user(String username) {
                return AuthenticationServiceTestDSL.this.user(username);
            }

            public AuthenticationServiceTestDSL whenExecute(Runnable method) {
                return AuthenticationServiceTestDSL.this.whenExecute(method);
            }

        }

        private class AuthenticationServiceTestDSLAsserter {

            private TaskboardUser user;

            public AuthenticationServiceTestDSLAsserter(String username) {
                user = registredUsers.get(username);
            }

            public AuthenticationServiceTestDSLAsserter taskboardUserLastLoginWasUpdatedToNow() {
                verify(taskboardUserService, times(1)).updateLastLoginToNow(eq(user.getUsername()));
                return this;
            }

            public AuthenticationServiceTestDSLAsserter taskboardUserLastLoginWasNotUpdated() {
                verify(taskboardUserService, times(0)).updateLastLoginToNow(eq(user.getUsername()));
                return this;
            }

            public AuthenticationServiceTestDSLAsserter success(boolean success) {
                assertEquals(success, authenticateResult.isSuccess());
                return this;
            }

            public AuthenticationServiceTestDSLAsserter message(String message) {
                assertEquals(message, authenticateResult.getMessage());
                return this;
            }

            public AuthenticationServiceTestDSLAsserterPrincipal principal() {
                return new AuthenticationServiceTestDSLAsserterPrincipal();
            }

            private class AuthenticationServiceTestDSLAsserterPrincipal {

                public AuthenticationServiceTestDSLAsserterPrincipal nullValue() {
                    assertNull(authenticateResult.getPrincipal());
                    return this;
                }

                public AuthenticationServiceTestDSLAsserterPrincipal realUsername(String user) {
                    assertEquals(user, authenticateResult.getPrincipal().getRealUsername());
                    return this;
                }

                public AuthenticationServiceTestDSLAsserterPrincipal defineUsername(String user) {
                    assertEquals(user, authenticateResult.getPrincipal().defineUsername());
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
