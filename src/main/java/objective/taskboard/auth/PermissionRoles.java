package objective.taskboard.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PermissionRoles {
    private String name;
    private List<String> requiredRoles;
    
    public PermissionRoles() {
        this.requiredRoles = new ArrayList<>();
    }
    
    public PermissionRoles(String name, String... requiredRoles) {
        this.name = name;
        this.requiredRoles = Arrays.asList(requiredRoles);
    }

    public String getName() {
        return name;
    }

    public boolean accepts(String role) {
        return requiredRoles.contains(role);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PermissionRoles that = (PermissionRoles) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        return requiredRoles != null ? requiredRoles.equals(that.requiredRoles) : that.requiredRoles == null;
    }
    
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (requiredRoles != null ? requiredRoles.hashCode() : 0);
        return result;
    }
}
