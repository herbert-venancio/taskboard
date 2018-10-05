package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;

import java.util.List;

import objective.taskboard.auth.LoggedUserDetails;

public class AnyProjectPermission implements TargetlessPermission {

    private final String name;
    private final List<String> acceptedRoles;

    public AnyProjectPermission(String name, String... acceptedRoles) {
        this.name = name;
        this.acceptedRoles = asList(acceptedRoles);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean accepts(LoggedUserDetails userDetails, PermissionContext permissionContext) {
        validate(permissionContext);

        return userDetails.getJiraRoles().stream()
                .anyMatch(role -> acceptedRoles.contains(role.name));
    }
}
