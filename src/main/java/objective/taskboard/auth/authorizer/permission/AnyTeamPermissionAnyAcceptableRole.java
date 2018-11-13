package objective.taskboard.auth.authorizer.permission;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;

public class AnyTeamPermissionAnyAcceptableRole extends BaseTargetlessPermission {

    private final UserTeamCachedRepository userTeamRepository;
    private final UserTeamRole[] acceptedRoles;

    public AnyTeamPermissionAnyAcceptableRole(
            String name,
            LoggedUserDetails loggedUserDetails,
            UserTeamCachedRepository userTeamRepository,
            UserTeamRole... acceptedRoles) {
        super(name, loggedUserDetails);
        this.userTeamRepository = userTeamRepository;
        this.acceptedRoles = acceptedRoles;
    }

    @Override
    protected boolean isAuthorized(LoggedUserDetails loggedUserDetails) {
        return !userTeamRepository.findByUsernameAndRoles(loggedUserDetails.getUsername(), acceptedRoles).isEmpty();
    }

}
