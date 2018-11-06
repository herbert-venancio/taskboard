package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.authorizer.Permissions.TEAMS_EDIT_VIEW;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;

@Service
public class TeamsEditViewPermission extends ComposedPermissionAnyMatch {

    @Autowired
    public TeamsEditViewPermission(TaskboardAdministrationPermission taskboardAdministrationPermission, UserTeamCachedRepository userTeamCachedRepository) {
        super(TEAMS_EDIT_VIEW, taskboardAdministrationPermission, new AnyTeamPermissionAnyAcceptableRole(null, userTeamCachedRepository, UserTeamRole.MANAGER));
    }

}
