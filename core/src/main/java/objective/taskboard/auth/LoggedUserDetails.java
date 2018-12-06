package objective.taskboard.auth;

import java.util.List;

public class LoggedUserDetails {

    private final String username;
    private final List<JiraRole> jiraRoles;
    private final boolean isAdmin;

    public LoggedUserDetails(String username, List<JiraRole> jiraRoles, boolean isAdmin) {
        this.username = username;
        this.jiraRoles = jiraRoles;
        this.isAdmin = isAdmin;
    }

    public List<JiraRole> getJiraRoles() {
        return jiraRoles;
    }

    public String getUsername() {
        return username;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }

    @Override
    public String toString() {
        return username;
    }

    public static class JiraRole {
        public static final String PROJECT_ADMINISTRATORS = "Administrators";
        public static final String PROJECT_DEVELOPERS = "Developers";
        public static final String PROJECT_KPI = "KPI";

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
