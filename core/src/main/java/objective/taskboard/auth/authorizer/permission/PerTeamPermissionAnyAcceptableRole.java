package objective.taskboard.auth.authorizer.permission;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.data.UserTeam;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;

public class PerTeamPermissionAnyAcceptableRole extends BaseTargettedPermission {

    private final UserTeamCachedRepository userTeamRepository;
    private final UserTeamRole[] acceptedRoles;

    public PerTeamPermissionAnyAcceptableRole(
            String name,
            LoggedUserDetails loggedUserDetails,
            UserTeamCachedRepository userTeamRepository,
            UserTeamRole... acceptedRoles) {
        super(name, loggedUserDetails);
        this.userTeamRepository = userTeamRepository;
        this.acceptedRoles = acceptedRoles;
    }

    @Override
    protected boolean isAuthorized(LoggedUserDetails loggedUserDetails, String target) {
        Optional<UserTeam> userTeam = userTeamRepository.findByUsernameTeamAndRoles(
                loggedUserDetails.getUsername(),
                target,
                acceptedRoles);
        return userTeam.isPresent();
    }

    @Override
    public List<String> applicableTargets() {
        List<String> applicableTargets = userTeamRepository.findByUsernameAndRoles(getLoggedUser().getUsername(), acceptedRoles).stream()
                .map(UserTeam::getTeam)
                .collect(toList());
        return applicableTargets;
    }

}
