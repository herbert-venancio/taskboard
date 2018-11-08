package objective.taskboard.auth.authorizer.permission;

import java.util.List;
import java.util.Optional;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.team.UserTeamPermissionService;

public class PerUserVisibilityOfUserPermission implements TargettedPermission {

    private final String name;
    private final UserTeamPermissionService userTeamPermissionService;
    private final TaskboardAdministrationPermission taskboardAdministrationPermission;

    public PerUserVisibilityOfUserPermission(
            String name,
            TaskboardAdministrationPermission taskboardAdministrationPermission,
            UserTeamPermissionService userTeamPermissionService) {
        this.name = name;
        this.taskboardAdministrationPermission = taskboardAdministrationPermission;
        this.userTeamPermissionService = userTeamPermissionService;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean accepts(LoggedUserDetails userDetails, PermissionContext permissionContext) {
        validate(permissionContext);

        boolean hasPermissionToSeeAllUsers = taskboardAdministrationPermission.accepts(userDetails, PermissionContext.empty());

        return hasPermissionToSeeAllUsers || isThereSomeTeamInCommon(permissionContext);
    }

    private boolean isThereSomeTeamInCommon(PermissionContext permissionContext) {
        return userTeamPermissionService.getTeamsVisibleToLoggedInUser().stream()
                .flatMap(team -> team.getMembers().stream())
                .anyMatch(userTeam -> userTeam.getUserName().equals(permissionContext.target));
    }

    @Override
    public Optional<List<String>> applicableTargets(LoggedUserDetails userDetails) {
        return Optional.empty();
    }

}
