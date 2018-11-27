package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.authorizer.Permissions.TEAM_EDIT;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;

@Service
public class TeamEditPermission extends BaseTargettedPermission {

    private final TaskboardAdministrationPermission taskboardAdministrationPermission;
    private final PerTeamPermissionAnyAcceptableRole perTeamPermissionAnyAcceptableRole;

    @Autowired
    public TeamEditPermission(
            TaskboardAdministrationPermission taskboardAdministrationPermission,
            LoggedUserDetails loggedUserDetails,
            UserTeamCachedRepository userTeamCachedRepository) {
        super(TEAM_EDIT, loggedUserDetails);
        this.taskboardAdministrationPermission = taskboardAdministrationPermission;
        this.perTeamPermissionAnyAcceptableRole = new PerTeamPermissionAnyAcceptableRole(null, loggedUserDetails, userTeamCachedRepository, UserTeamRole.MANAGER);
    }

    @Override
    public List<String> applicableTargets() {
        return PermissionUtils.applicableTargetsInAnyPermission(perTeamPermissionAnyAcceptableRole);
    }

    @Override
    protected boolean isAuthorized(LoggedUserDetails loggedUserDetails, String target) {
        return PermissionUtils.isAuthorizedForAnyPermission(target, taskboardAdministrationPermission);
    }

}
