package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.authorizer.Permissions.TEAM_EDIT;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;

@Service
public class TeamEditPermission extends ComposedPermissionAnyMatch {

    @Autowired
    public TeamEditPermission(TaskboardAdministrationPermission taskboardAdministrationPermission, UserTeamCachedRepository userTeamCachedRepository) {
        super(TEAM_EDIT, taskboardAdministrationPermission, new PerTeamPermissionAnyAcceptableRole(null, userTeamCachedRepository, UserTeamRole.MANAGER));
    }

}
