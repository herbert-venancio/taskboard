package objective.taskboard.team;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static objective.taskboard.data.UserTeam.UserTeamRole.MANAGER;
import static objective.taskboard.data.UserTeam.UserTeamRole.MEMBER;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.authorizer.permission.TeamEditPermission;
import objective.taskboard.data.Team;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.UserTeamCachedRepository;

@Service
public class UserTeamService {

    private final UserTeamCachedRepository userTeamRepo;
    private final TeamCachedRepository teamRepo;
    private final UserTeamPermissionService userTeamPermissionService;
    private final TeamEditPermission teamEditPermission;

    @Autowired
    public UserTeamService(
            UserTeamCachedRepository userTeamRepo, 
            TeamCachedRepository teamRepo,
            UserTeamPermissionService userTeamPermissionService,
            TeamEditPermission teamEditPermission) {
        this.userTeamRepo = userTeamRepo;
        this.teamRepo = teamRepo;
        this.userTeamPermissionService = userTeamPermissionService;
        this.teamEditPermission = teamEditPermission;
    }

    public List<Long> getIdsOfTeamsVisibleToUser() {
        return userTeamPermissionService.getTeamsVisibleToLoggedInUser().stream()
                .map(ut -> ut.getId())
                .collect(toList());
    }

    public Set<Team> getTeamsThatUserCanAdmin() {
        return teamRepo.getCache().stream()
                .filter(t -> teamEditPermission.isAuthorizedFor(t.getName()))
                .collect(toSet());
    }

    public List<Team> getTeamsThatUserIsAValidAssignee(String username) {
        return userTeamRepo.findByUserName(username).stream()
                .filter(userTeam -> userTeam.getRole() == MANAGER || userTeam.getRole() == MEMBER)
                .map(ut -> teamRepo.findByName(ut.getTeam()))
                .filter(Objects::nonNull)
                .distinct()
                .collect(toList());
    }

    public Optional<Team> getTeamVisibleToLoggedInUserById(Long id) {
        return userTeamPermissionService.getTeamsVisibleToLoggedInUser().stream()
            .filter(t -> t.getId().equals(id))
            .findFirst();
    }

    public Team getTeamVisibleToLoggedInUserByIdOrCry(Long id) {
        return getTeamVisibleToLoggedInUserById(id)
                .orElseThrow(() -> new IllegalStateException("Team \""+ id +"\" not found to logged in user."));
    }

    public Team saveTeam(Team team) {
        return teamRepo.save(team);
    }

}
