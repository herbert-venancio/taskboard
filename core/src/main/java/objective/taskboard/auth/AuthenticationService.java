package objective.taskboard.auth;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.authorizer.permission.ImpersonatePermission.IMPERSONATE_HEADER;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import objective.taskboard.auth.LoggedUserDetails.JiraRole;
import objective.taskboard.auth.authorizer.permission.ImpersonatePermission;
import objective.taskboard.jira.JiraService;
import objective.taskboard.user.TaskboardUser;
import objective.taskboard.user.TaskboardUserService;

@Component
public class AuthenticationService {

    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);

    private final HttpServletRequest request;
    private final JiraService jiraService;
    private final ImpersonatePermission impersonatePermission;
    private final TaskboardUserService taskboardUserService;

    @Autowired
    public AuthenticationService(
            HttpServletRequest request,
            JiraService jiraService,
            ImpersonatePermission impersonatePermission,
            TaskboardUserService taskboardUserService) {
        this.request = request;
        this.jiraService = jiraService;
        this.impersonatePermission = impersonatePermission;
        this.taskboardUserService = taskboardUserService;
    }

    public AuthenticationResult authenticate(String username, String password) throws BadCredentialsException {
        try {
            jiraService.authenticate(username, password);

            TaskboardUser taskboardUser = taskboardUserService.getTaskboardUser(username);
            LoggedUserDetails principal = getPrincipal(taskboardUser);

            updateLastLogin(username);

            return AuthenticationResult.success(principal);
        } catch (Exception e) { //NOSONAR
            return AuthenticationResult.fail(e.getMessage());
        }
    }

    private LoggedUserDetails getPrincipal(TaskboardUser taskboardUser) throws ImpersonateException {
        Optional<String> impersonateUsername = Optional.ofNullable(request.getHeader(IMPERSONATE_HEADER));
        LoggedUserDetails loggedInUser = getUserDetails(taskboardUser);

        if (impersonateUsername.isPresent() && impersonatePermission.isAuthorized(loggedInUser, impersonateUsername.get()))
            impersonate(loggedInUser, impersonateUsername.get());

        return loggedInUser;
    }

    private LoggedUserDetails getUserDetails(TaskboardUser taskboardUser) {
        List<JiraRole> roles = jiraService.getUserRoles(taskboardUser.getUsername()).stream()
                .map(r -> new LoggedUserDetails.JiraRole(r.id, r.name, r.projectKey))
                .collect(toList());

        return new LoggedUserDetails(taskboardUser.getUsername(), roles, taskboardUser.isAdmin());
    }

    private void impersonate(LoggedUserDetails loggedInUser, String impersonateUsername) throws ImpersonateException {
        if (isBlank(impersonateUsername))
            throw ImpersonateException.blankHeaderValue();

        if (!jiraService.getJiraUserAsMaster(impersonateUsername).isPresent())
            throw ImpersonateException.userToImpersonateNotFound(impersonateUsername);

        loggedInUser.setImpersonateUser(getUserDetails(taskboardUserService.getTaskboardUser(impersonateUsername)));
    }

    private void updateLastLogin(String username) {
        try {
            taskboardUserService.updateLastLoginToNow(username);
        } catch (ObjectOptimisticLockingFailureException e) { //NOSONAR
            log.info(e.getMessage());
        }
    }

    public static class AuthenticationResult {
        private final boolean success;
        private final String message;
        private final LoggedUserDetails principal;

        private AuthenticationResult(boolean success, String message, LoggedUserDetails principal) {
            this.success = success;
            this.message = message;
            this.principal = principal;
        }

        private static AuthenticationResult success(LoggedUserDetails principal) {
            return new AuthenticationResult(true, null, principal);
        }

        private static AuthenticationResult fail(String message) {
            return new AuthenticationResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public LoggedUserDetails getPrincipal() {
            return principal;
        }
    }

}
