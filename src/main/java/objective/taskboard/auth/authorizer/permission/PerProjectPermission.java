package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import objective.taskboard.auth.LoggedUserDetails;

public class PerProjectPermission extends BasePermission implements TargettedPermission {

    private final List<String> acceptedRoles;

    public PerProjectPermission(String name, LoggedUserDetails loggedUserDetails, String... acceptedRoles) {
        super(name, loggedUserDetails);
        this.acceptedRoles = asList(acceptedRoles);
    }

    @Override
    public boolean isAuthorized(PermissionContext permissionContext) {
        validate(permissionContext);

        return getLoggedUser().getJiraRoles().stream()
                .filter(role -> role.projectKey.equals(permissionContext.target))
                .anyMatch(role -> acceptedRoles.contains(role.name));
    }

    @Override
    public Optional<List<String>> applicableTargets() {
        List<String> applicableTargets = getLoggedUser().getJiraRoles().stream()
                .filter(role -> acceptedRoles.contains(role.name))
                .map(role -> role.projectKey)
                .collect(toList());
        return Optional.of(applicableTargets);
    }

}
