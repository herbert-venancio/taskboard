package objective.taskboard.team;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;

import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.filterConfiguration.TeamFilterConfigurationService;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.UserTeamCachedRepository;

public class UserTeamServiceTest {
    
    private UserTeamCachedRepository userTeamRepo = mock(UserTeamCachedRepository.class);
    private TeamCachedRepository teamRepo = mock(TeamCachedRepository.class);
    private TeamFilterConfigurationService teamFilterConfigurationService = mock(TeamFilterConfigurationService.class);
    private LoggedUserDetails loggedInUser = mock(LoggedUserDetails.class);
    private UserTeamService subject = new UserTeamService(userTeamRepo, teamRepo, teamFilterConfigurationService, loggedInUser);
    
    @Test
    public void getTeamsVisibleToLoggedInUser_regularUser_shouldReturnTeamsThatUserIsMemberOfAndDefaultTeamsOfVisibleProjects() {        
        Team rocketTeam = new Team("Rocket", "sue", "sue", emptyList());
        Team superTeam  = new Team("Super",  "sue", "sue", emptyList());
        Team extraTeam  = new Team("Extra",  "sue", "sue", emptyList());

        when(loggedInUser.getUsername()).thenReturn("mary");
        when(loggedInUser.isAdmin()).thenReturn(false);
        when(userTeamRepo.findByUserName("mary")).thenReturn(asList(
                new UserTeam("mary", "Super"), 
                new UserTeam("mary", "Extra")));
 
        when(teamFilterConfigurationService.getDefaultTeamsInProjectsVisibleToUser()).thenReturn(asList(rocketTeam));
        
        when(teamRepo.findByName("Rocket")).thenReturn(rocketTeam);
        when(teamRepo.findByName("Super")).thenReturn(superTeam);
        when(teamRepo.findByName("Extra")).thenReturn(extraTeam);
        
        assertTeams("Extra, Rocket, Super", subject.getTeamsVisibleToLoggedInUser());
    }

    @Test
    public void getTeamsVisibleToLoggedInUser_adminUser_shouldReturnAllTeams() {
        when(loggedInUser.isAdmin()).thenReturn(true);
        when(teamRepo.getCache()).thenReturn(asList(
                new Team("Extra" , "sue", "sue", emptyList()),
                new Team("Super", "joe", "joe", emptyList())));
        
        assertTeams("Extra, Super", subject.getTeamsVisibleToLoggedInUser());
    }

    private static void assertTeams(String expectedNames, Collection<Team> actual) {
        assertEquals(expectedNames, actual.stream().map(t -> t.getName()).sorted().collect(joining(", ")));
    }
}
