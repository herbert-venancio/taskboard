package objective.taskboard.auth;

import java.util.List;
import java.util.Optional;

public class LoggedUserDetails {

    private final String username;
    private final List<JiraRole> jiraRoles;
    private final boolean isAdmin;

    private Optional<LoggedUserDetails> impersonateUser = Optional.empty();

    public LoggedUserDetails(String username, List<JiraRole> jiraRoles, boolean isAdmin) {
        this.username = username;
        this.jiraRoles = jiraRoles;
        this.isAdmin = isAdmin;
    }

    public String getRealUsername() {
        return username;
    }

    public Optional<String> getImpersonateUsername() {
        return impersonateUser
                .map(impersonate -> Optional.of(impersonate.defineUsername()))
                .orElse(Optional.empty());
    }

    public void setImpersonateUser(LoggedUserDetails impersonateUser) {
        this.impersonateUser = Optional.of(impersonateUser);
    }

    public String defineUsername() {
        return getImpersonateUsername()
                .orElse(getRealUsername());
    }

    public List<JiraRole> getJiraRoles() {
        return impersonateUser
                .map(LoggedUserDetails::getJiraRoles)
                .orElse(jiraRoles);
    }

    public boolean isAdmin() {
        return impersonateUser
                .map(LoggedUserDetails::isAdmin)
                .orElse(isAdmin);
    }

    @Override
    public String toString() {
        return username;
    }

    public static class JiraRole {
        public static final String PROJECT_ADMINISTRATORS = "Administrators";
        public static final String PROJECT_DEVELOPERS = "Developers";
        public static final String PROJECT_KPI = "KPI";
        public static final String PROJECT_CUSTOMERS = "Customer";

        public Long id;
        public String name;
        public String projectKey;

        public JiraRole(Long id, String name, String projectKey) {
            this.id = id;
            this.name = name;
            this.projectKey = projectKey;
        }
    }
}
