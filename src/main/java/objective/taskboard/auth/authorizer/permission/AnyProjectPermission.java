package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;

import java.util.List;

import objective.taskboard.auth.LoggedUserDetails;

public class AnyProjectPermission extends BaseTargetlessPermission {

    private final List<String> acceptedRoles;

    public AnyProjectPermission(String name, LoggedUserDetails loggedUserDetails, String... acceptedRoles) {
        super(name, loggedUserDetails);
        this.acceptedRoles = asList(acceptedRoles);
    }

    @Override
    public boolean isAuthorized(LoggedUserDetails loggedUserDetails) {
        return loggedUserDetails.getJiraRoles().stream()
                .anyMatch(role -> acceptedRoles.contains(role.name));
    }

}
