package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;

import java.util.List;

import objective.taskboard.auth.LoggedUserDetails;

public class AnyProjectPermission extends BasePermission implements TargetlessPermission {

    private final List<String> acceptedRoles;

    public AnyProjectPermission(String name, String... acceptedRoles) {
        super(name);
        this.acceptedRoles = asList(acceptedRoles);
    }

    @Override
    public boolean accepts(LoggedUserDetails userDetails, PermissionContext permissionContext) {
        validate(permissionContext);

        return userDetails.getJiraRoles().stream()
                .anyMatch(role -> acceptedRoles.contains(role.name));
    }

}
