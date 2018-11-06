package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;

public class PerTeamPermissionAnyAcceptableRole extends BasePermission implements TargettedPermission {

    private final UserTeamCachedRepository userTeamRepository;
    private final List<UserTeamRole> acceptedRoles;

    public PerTeamPermissionAnyAcceptableRole(String name, UserTeamCachedRepository userTeamRepository, UserTeamRole... acceptedRoles) {
        super(name);
        this.userTeamRepository = userTeamRepository;
        this.acceptedRoles = asList(acceptedRoles);
    }

    @Override
    public boolean accepts(LoggedUserDetails userDetails, PermissionContext permissionContext) {
        validate(permissionContext);

        return userTeamRepository.findByUserName(userDetails.getUsername()).stream()
                .filter(userTeam -> userTeam.getTeam().equals(permissionContext.target))
                .anyMatch(userTeam -> acceptedRoles.contains(userTeam.getRole()));
    }

    @Override
    public Optional<List<String>> applicableTargets(LoggedUserDetails userDetails) {
        List<String> applicableTargets = userTeamRepository.findByUserName(userDetails.getUsername()).stream()
                .filter(userTeam -> acceptedRoles.contains(userTeam.getRole()))
                .map(userTeam -> userTeam.getTeam())
                .collect(toList());
        return Optional.of(applicableTargets);
    }

}
