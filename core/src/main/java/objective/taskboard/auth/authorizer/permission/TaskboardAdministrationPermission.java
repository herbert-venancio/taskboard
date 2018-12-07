package objective.taskboard.auth.authorizer.permission;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.authorizer.Permissions.TASKBOARD_ADMINISTRATION;

import java.util.List;

import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.data.UserTeam;
import objective.taskboard.repository.UserTeamCachedRepository;

@Service
public class TaskboardAdministrationPermission extends BaseTargetlessPermission {

    private final UserTeamCachedRepository userTeamRepository;

    public TaskboardAdministrationPermission(LoggedUserDetails loggedUserDetails, UserTeamCachedRepository userTeamRepository) {
        super(TASKBOARD_ADMINISTRATION, loggedUserDetails);
        this.userTeamRepository = userTeamRepository;
    }

    @Override
    protected boolean isAuthorized(LoggedUserDetails loggedUserDetails) {
        return loggedUserDetails.isAdmin();
    }

    public List<String> getAllTeams() {
        List<String> applicableTargets = userTeamRepository.getCache().stream()
                .map(UserTeam::getTeam)
                .collect(toList());
        return applicableTargets;
    }

}
