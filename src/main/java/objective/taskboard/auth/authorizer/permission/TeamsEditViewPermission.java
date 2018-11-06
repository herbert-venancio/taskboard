package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.authorizer.Permissions.TEAMS_EDIT_VIEW;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;

@Service
public class TeamsEditViewPermission extends ComposedPermissionAnyMatch {

    @Autowired
    public TeamsEditViewPermission(
            TaskboardAdministrationPermission taskboardAdministrationPermission,
            LoggedUserDetails loggedUserDetails,
            UserTeamCachedRepository userTeamCachedRepository) {
        super(TEAMS_EDIT_VIEW,
                loggedUserDetails, taskboardAdministrationPermission,
                new AnyTeamPermissionAnyAcceptableRole(null, loggedUserDetails, userTeamCachedRepository, UserTeamRole.MANAGER));
    }

}
