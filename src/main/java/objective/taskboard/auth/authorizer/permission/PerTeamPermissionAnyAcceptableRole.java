package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;

public class PerTeamPermissionAnyAcceptableRole extends BaseTargettedPermission {

    private final UserTeamCachedRepository userTeamRepository;
    private final List<UserTeamRole> acceptedRoles;

    public PerTeamPermissionAnyAcceptableRole(
            String name,
            LoggedUserDetails loggedUserDetails,
            UserTeamCachedRepository userTeamRepository,
            UserTeamRole... acceptedRoles) {
        super(name, loggedUserDetails);
        this.userTeamRepository = userTeamRepository;
        this.acceptedRoles = asList(acceptedRoles);
    }

    @Override
    protected boolean isAuthorized(LoggedUserDetails loggedUserDetails, String target) {
        return userTeamRepository.findByUserName(loggedUserDetails.getUsername()).stream()
                .filter(userTeam -> userTeam.getTeam().equals(target))
                .anyMatch(userTeam -> acceptedRoles.contains(userTeam.getRole()));
    }

    @Override
    public List<String> applicableTargets() {
        List<String> applicableTargets = userTeamRepository.findByUserName(getLoggedUser().getUsername()).stream()
                .filter(userTeam -> acceptedRoles.contains(userTeam.getRole()))
                .map(userTeam -> userTeam.getTeam())
                .collect(toList());
        return applicableTargets;
    }

}
