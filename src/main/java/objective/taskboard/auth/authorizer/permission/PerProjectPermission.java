package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;

import objective.taskboard.auth.LoggedUserDetails;

public class PerProjectPermission extends BaseTargettedPermission {

    private final List<String> acceptedRoles;

    public PerProjectPermission(String name, LoggedUserDetails loggedUserDetails, String... acceptedRoles) {
        super(name, loggedUserDetails);
        this.acceptedRoles = asList(acceptedRoles);
    }

    @Override
    protected boolean isAuthorized(LoggedUserDetails loggedUserDetails, String target) {
        return loggedUserDetails.getJiraRoles().stream()
                .filter(role -> role.projectKey.equals(target))
                .anyMatch(role -> acceptedRoles.contains(role.name));
    }

    @Override
    public List<String> applicableTargets() {
        List<String> applicableTargets = getLoggedUser().getJiraRoles().stream()
                .filter(role -> acceptedRoles.contains(role.name))
                .map(role -> role.projectKey)
                .collect(toList());
        return applicableTargets;
    }

}
