package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.authorizer.Permissions.USER_VISIBILITY;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.team.UserTeamPermissionService;

@Service
public class UserVisibilityPermission extends BasePermission implements TargettedPermission {

    private final UserTeamPermissionService userTeamPermissionService;
    private final TaskboardAdministrationPermission taskboardAdministrationPermission;

    @Autowired
    public UserVisibilityPermission(
            TaskboardAdministrationPermission taskboardAdministrationPermission,
            LoggedUserDetails loggedUserDetails,
            UserTeamPermissionService userTeamPermissionService) {
        super(USER_VISIBILITY, loggedUserDetails);
        this.taskboardAdministrationPermission = taskboardAdministrationPermission;
        this.userTeamPermissionService = userTeamPermissionService;
    }

    @Override
    public boolean accepts(PermissionContext permissionContext) {
        validate(permissionContext);

        boolean hasPermissionToSeeAllUsers = taskboardAdministrationPermission.accepts(PermissionContext.empty());

        return hasPermissionToSeeAllUsers || isThereSomeTeamInCommon(permissionContext);
    }

    private boolean isThereSomeTeamInCommon(PermissionContext permissionContext) {
        return userTeamPermissionService.getTeamsVisibleToLoggedInUser().stream()
                .flatMap(team -> team.getMembers().stream())
                .anyMatch(userTeam -> userTeam.getUserName().equals(permissionContext.target));
    }

    @Override
    public Optional<List<String>> applicableTargets() {
        return Optional.empty();
    }

}
