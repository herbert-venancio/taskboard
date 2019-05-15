package objective.taskboard.team;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.filter.TeamFilterConfigurationService;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.UserTeamCachedRepository;

@Service
public class UserTeamPermissionService {

    private final UserTeamCachedRepository userTeamRepo;
    private final TeamCachedRepository teamRepo;
    private final TeamFilterConfigurationService teamFilterConfigurationService;
    private final LoggedUserDetails loggedInUser;

    @Autowired
    public UserTeamPermissionService(
            UserTeamCachedRepository userTeamRepo, 
            TeamCachedRepository teamRepo,
            TeamFilterConfigurationService teamFilterConfigurationService, 
            LoggedUserDetails loggedInUser) {
        this.userTeamRepo = userTeamRepo;
        this.teamRepo = teamRepo;
        this.teamFilterConfigurationService = teamFilterConfigurationService;
        this.loggedInUser = loggedInUser;
    }

    public Set<Team> getTeamsVisibleToLoggedInUser() {
        if (loggedInUser.isAdmin())
            return new HashSet<>(teamRepo.getCache());

        return getTeamsVisibleToUser(loggedInUser.getUsername());
    }

    public Set<Team> getTeamsVisibleToUser(String username) {
        List<UserTeam> userTeam = userTeamRepo.findByUserName(username);
        Set<Team> userTeams = userTeam.stream()
                .map(ut -> teamRepo.findByName(ut.getTeam()))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<Team> teamsInProjectsVisibleToUser = teamFilterConfigurationService.getDefaultTeamsInProjectsVisibleToUser();
        userTeams.addAll(teamsInProjectsVisibleToUser);
        userTeams.addAll(teamFilterConfigurationService.getGloballyVisibleTeams());

        return userTeams;
    }

}
