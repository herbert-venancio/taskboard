package objective.taskboard.auth;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import objective.taskboard.auth.LoggedUserDetails.JiraRole;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.user.TaskboardUser;
import objective.taskboard.user.TaskboardUserRepository;
import objective.taskboard.utils.Clock;

@Component
public class AuthenticationService {

    private final JiraProperties jiraProperties;
    private final JiraService jiraService;
    private final TaskboardUserRepository taskboardUserRepository;
    private final Clock clock;

    @Autowired
    public AuthenticationService(
            JiraProperties jiraProperties,
            JiraService jiraService, 
            TaskboardUserRepository taskboardUserRepository, 
            Clock clock) {
        this.jiraProperties = jiraProperties;
        this.jiraService = jiraService;
        this.taskboardUserRepository = taskboardUserRepository;
        this.clock = clock;
    }

    @Transactional
    public AuthenticationResult authenticate(String username, String password) throws BadCredentialsException {
        try {
            jiraService.authenticate(username, password);
        } catch (Exception e) { //NOSONAR
            return AuthenticationResult.fail(e.getMessage());
        }
        
        TaskboardUser taskboardUser = getOrCreateTaskboardUser(username);
        taskboardUser.setLastLogin(clock.now());

        LoggedUserDetails principal = getPrincipal(taskboardUser);

        return AuthenticationResult.success(principal);
    }

    private LoggedUserDetails getPrincipal(TaskboardUser taskboardUser) {
        List<JiraRole> roles = jiraService.getUserRoles(taskboardUser.getUsername()).stream()
                .map(r -> new LoggedUserDetails.JiraRole(r.id, r.name, r.projectKey))
                .collect(toList());

        return new LoggedUserDetails(taskboardUser.getUsername(), roles, taskboardUser.isAdmin());
    }

    private TaskboardUser getOrCreateTaskboardUser(String username) {
        return taskboardUserRepository.getByUsername(username)
                .orElseGet(() -> createTaskboardUser(username));
    }

    private TaskboardUser createTaskboardUser(String username) {
        TaskboardUser user = new TaskboardUser(username);

        if (username.equals(jiraProperties.getLousa().getUsername()))
            user.setAdmin(true);

        taskboardUserRepository.add(user);
        return user;
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
