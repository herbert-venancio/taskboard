package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.authorizer.Permissions.TEAMS_EDIT_VIEW;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;

@Service
public class TeamsEditViewPermission extends BaseTargetlessPermission {

    private final TaskboardAdministrationPermission taskboardAdministrationPermission;
    private final AnyTeamPermissionAnyAcceptableRole managerInAnyTeamPermission;

    @Autowired
    public TeamsEditViewPermission(
            TaskboardAdministrationPermission taskboardAdministrationPermission,
            LoggedUserDetails loggedUserDetails,
            UserTeamCachedRepository userTeamCachedRepository) {
        super(TEAMS_EDIT_VIEW, loggedUserDetails);
        this.taskboardAdministrationPermission = taskboardAdministrationPermission;
        this.managerInAnyTeamPermission = new AnyTeamPermissionAnyAcceptableRole(null, loggedUserDetails, userTeamCachedRepository, UserTeamRole.MANAGER);
    }

    @Override
    protected boolean isAuthorized(LoggedUserDetails loggedUserDetails) {
        return PermissionUtils.isAuthorizedForAnyPermission(taskboardAdministrationPermission, managerInAnyTeamPermission);
    }

}
