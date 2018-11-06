package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;

import java.util.List;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;

public class AnyTeamPermissionAnyAcceptableRole extends BasePermission implements TargetlessPermission {

    private final UserTeamCachedRepository userTeamRepository;
    private final List<UserTeamRole> acceptedRoles;

    public AnyTeamPermissionAnyAcceptableRole(String name, UserTeamCachedRepository userTeamRepository, UserTeamRole... acceptedRoles) {
        super(name);
        this.userTeamRepository = userTeamRepository;
        this.acceptedRoles = asList(acceptedRoles);
    }

    @Override
    public boolean accepts(LoggedUserDetails userDetails, PermissionContext permissionContext) {
        validate(permissionContext);

        return userTeamRepository.findByUserName(userDetails.getUsername()).stream()
                .anyMatch(userTeam -> acceptedRoles.contains(userTeam.getRole()));
    }
}
