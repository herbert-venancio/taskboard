package objective.taskboard.auth;

import java.util.List;

public class LoggedUserDetails {

    private final String username;
    private final List<Role> roles;
    private final boolean isAdmin;

    public LoggedUserDetails(String username, List<Role> roles, boolean isAdmin) {
        this.username = username;
        this.roles = roles;
        this.isAdmin = isAdmin;
    }

    public List<Role> getUserRoles() {
        return roles;
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

    public static class Role {
        public Long id;
        public String name;
        public String projectKey;

        public Role() {}

        public Role(Long id, String name, String projectKey) {
            this.id = id;
            this.name = name;
            this.projectKey = projectKey;
        }
    }
}
