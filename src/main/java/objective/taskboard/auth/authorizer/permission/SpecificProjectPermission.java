package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;

import objective.taskboard.auth.LoggedUserDetails;

public class SpecificProjectPermission implements TargettedPermission {

    private final String name;
    private final List<String> acceptedRoles;

    public SpecificProjectPermission(String name, String... acceptedRoles) {
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
                .filter(role -> role.projectKey.equals(permissionContext.target))
                .anyMatch(role -> acceptedRoles.contains(role.name));
    }

    @Override
    public List<String> applicableTargets(LoggedUserDetails userDetails) {
        return userDetails.getJiraRoles().stream()
                .filter(role -> acceptedRoles.contains(role.name))
                .map(role -> role.projectKey)
                .collect(toList());
    }

}
