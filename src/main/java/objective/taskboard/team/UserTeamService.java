package objective.taskboard.team;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.filterConfiguration.TeamFilterConfigurationService;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.UserTeamCachedRepository;

@Service
public class UserTeamService {

    private final UserTeamCachedRepository userTeamRepo;
    private final TeamCachedRepository teamRepo;
    private final TeamFilterConfigurationService teamFilterConfigurationService;
    private final LoggedUserDetails loggedInUser;

    @Autowired
    public UserTeamService(
            UserTeamCachedRepository userTeamRepo, 
            TeamCachedRepository teamRepo,
            TeamFilterConfigurationService teamFilterConfigurationService, 
            LoggedUserDetails loggedInUser) {
        this.userTeamRepo = userTeamRepo;
        this.teamRepo = teamRepo;
        this.teamFilterConfigurationService = teamFilterConfigurationService;
        this.loggedInUser = loggedInUser;
    }

    public List<Long> getIdsOfTeamsVisibleToUser() {
        return getTeamsVisibleToLoggedInUser().stream().map(ut->ut.getId()).collect(Collectors.toList());
    }

    public boolean isUserVisibleToLoggedUser(String username) {
        Set<Team> teams = getTeamsVisibleToLoggedInUser();
        for (Team team : teams) {
            List<UserTeam> members = team.getMembers();
            for (UserTeam userTeam : members) {
                if (userTeam.getUserName().equals(username))
                    return true;
            }
        }
        return false;
    }

    public Set<Team> getTeamsVisibleToLoggedInUser() {
        if (loggedInUser.isAdmin())
            return new HashSet<>(teamRepo.getCache());

        List<UserTeam> userTeam = userTeamRepo.findByUserName(loggedInUser.getUsername());
        Set<Team> userTeams = userTeam.stream().map(ut->teamRepo.findByName(ut.getTeam())).filter(t->t!=null).distinct().collect(Collectors.toSet());

        List<Team> teamsInProjectsVisibleToUser = teamFilterConfigurationService.getDefaultTeamsInProjectsVisibleToUser();
        userTeams.addAll(teamsInProjectsVisibleToUser);

        return userTeams;
    }

    public Optional<Team> getTeamVisibleToLoggedInUserById(Long id) {
        return getTeamsVisibleToLoggedInUser().stream()
            .filter(t -> t.getId().equals(id))
            .findFirst();
    }

    public Team getTeamVisibleToLoggedInUserByIdOrCry(Long id) {
        return getTeamVisibleToLoggedInUserById(id)
                .orElseThrow(() -> new IllegalStateException("Team \""+ id +"\" not found to logged in user."));
    }

}
