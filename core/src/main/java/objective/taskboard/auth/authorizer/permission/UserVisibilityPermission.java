package objective.taskboard.auth.authorizer.permission;

import static java.util.Collections.emptyList;
import static objective.taskboard.auth.authorizer.Permissions.USER_VISIBILITY;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.team.UserTeamPermissionService;

@Service
public class UserVisibilityPermission extends BaseTargettedPermission {

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
    protected boolean isAuthorized(LoggedUserDetails loggedUserDetails, String target) {
        boolean hasPermissionToSeeAllUsers = taskboardAdministrationPermission.isAuthorized(loggedUserDetails);

        return hasPermissionToSeeAllUsers || isSameUser(loggedUserDetails, target) || isThereSomeTeamInCommon(target);
    }

    private boolean isSameUser(LoggedUserDetails loggedUserDetails, String target) {
        return target.equals(loggedUserDetails.defineUsername());
    }

    private boolean isThereSomeTeamInCommon(String target) {
        return userTeamPermissionService.getTeamsVisibleToLoggedInUser().stream()
                .flatMap(team -> team.getMembers().stream())
                .anyMatch(userTeam -> userTeam.getUserName().equals(target));
    }

    @Override
    public List<String> applicableTargets() {
        return emptyList();
    }

}
