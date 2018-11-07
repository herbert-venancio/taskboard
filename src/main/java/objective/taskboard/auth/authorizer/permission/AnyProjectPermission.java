package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;

import java.util.List;

import objective.taskboard.auth.LoggedUserDetails;

public class AnyProjectPermission extends BasePermission implements TargetlessPermission {

    private final List<String> acceptedRoles;

    public AnyProjectPermission(String name, LoggedUserDetails loggedUserDetails, String... acceptedRoles) {
        super(name, loggedUserDetails);
        this.acceptedRoles = asList(acceptedRoles);
    }

    @Override
    public boolean isAuthorized(PermissionContext permissionContext) {
        validate(permissionContext);

        return getLoggedUser().getJiraRoles().stream()
                .anyMatch(role -> acceptedRoles.contains(role.name));
    }

}
