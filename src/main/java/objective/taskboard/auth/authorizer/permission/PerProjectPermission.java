package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import objective.taskboard.auth.LoggedUserDetails;

public class PerProjectPermission extends BasePermission implements TargettedPermission {

    private final List<String> acceptedRoles;

    public PerProjectPermission(String name, String... acceptedRoles) {
        super(name);
        this.acceptedRoles = asList(acceptedRoles);
    }

    @Override
    public boolean accepts(LoggedUserDetails userDetails, PermissionContext permissionContext) {
        validate(permissionContext);

        return userDetails.getJiraRoles().stream()
                .filter(role -> role.projectKey.equals(permissionContext.target))
                .anyMatch(role -> acceptedRoles.contains(role.name));
    }

    @Override
    public Optional<List<String>> applicableTargets(LoggedUserDetails userDetails) {
        List<String> applicableTargets = userDetails.getJiraRoles().stream()
                .filter(role -> acceptedRoles.contains(role.name))
                .map(role -> role.projectKey)
                .collect(toList());
        return Optional.of(applicableTargets);
    }

}
