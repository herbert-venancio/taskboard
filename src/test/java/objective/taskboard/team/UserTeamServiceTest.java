package objective.taskboard.team;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.utils.StreamUtils.streamOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
    public void getTeamsVisibleToLoggedInUser_regularUser_shouldReturnTeamsWhereUserIsMemberOf() {
        withTeams()
            .team("Rocket")
            .team("Super")
            .team("Extra")
        .userIsMemberOf("Super", "Extra")
        .userIsNotAdmin();

        assertTeams("Extra, Super", subject.getTeamsVisibleToLoggedInUser());
    }

    @Test
    public void getTeamsVisibleToLoggedInUser_regularUser_shouldReturnDefaultTeamsOfVisibleProjects() {
        withTeams()
            .team("Rocket").isDefaultInAVisibleProject()
            .team("Super")
            .team("Extra")
        .userIsMemberOf()
        .userIsNotAdmin();
        
        assertTeams("Rocket", subject.getTeamsVisibleToLoggedInUser());
    }

    @Test
    public void getTeamsVisibleToLoggedInUser_adminUser_shouldReturnAllTeams() {
        withTeams()
            .team("Super")
            .team("Extra")
        .userIsMemberOf()
        .userIsAdmin();
        
        assertTeams("Extra, Super", subject.getTeamsVisibleToLoggedInUser());
    }

    @Test
    public void getTeamsVisibleToLoggedInUser_shouldReturnGloballyVisibleTeams() {
        withTeams()
            .team("Global").isGloballyVisible()
            .team("Extra")
        .userIsMemberOf()
        .userIsNotAdmin();

        assertTeams("Global", subject.getTeamsVisibleToLoggedInUser());
    }

    private static void assertTeams(String expectedNames, Collection<Team> actual) {
        assertEquals(expectedNames, actual.stream().map(t -> t.getName()).sorted().collect(joining(", ")));
    }

    private DSLBuilder withTeams() {
        return new DSLBuilder();
    }

    private class DSLBuilder {
        List<Team> defaulTeamsInProjects = new LinkedList<Team>();
        List<Team> globallyVisibleTeams = new LinkedList<Team>();
        Map<String, Team> teamByName = new HashMap<>();

        public DSLBuilder() {
            when(loggedInUser.getUsername()).thenReturn("mary");
            when(teamFilterConfigurationService.getDefaultTeamsInProjectsVisibleToUser()).thenReturn(defaulTeamsInProjects);
            when(teamRepo.getCache()).thenAnswer(i->teamByName.values().stream().collect(toList()));
            when(teamFilterConfigurationService.getGloballyVisibleTeams()).thenReturn(globallyVisibleTeams);
        }

        public DSLTeam team(String teamName) {
            Team team = new Team(teamName, "sue", "sue", emptyList());
            teamByName.put(teamName, team);

            when(teamRepo.findByName(teamName)).thenReturn(team);
            return new DSLTeam(teamName);
        }

        public void userIsNotAdmin() {
            when(loggedInUser.isAdmin()).thenReturn(false);
        }

        public void userIsAdmin() {
            when(loggedInUser.isAdmin()).thenReturn(true);
        }

        public DSLBuilder userIsMemberOf(String ...teams) {
            when(userTeamRepo.findByUserName("mary")).thenReturn(
                    streamOf(Arrays.asList(teams))
                        .map(teamName -> new UserTeam("mary", teamName))
                        .collect(toList()));

            return this;
        }

        class DSLTeam {
            private String teamName;
            public DSLTeam(String teamName) {
                this.teamName = teamName;
            }

            public DSLBuilder isGloballyVisible() {
                globallyVisibleTeams.add(teamByName.get(teamName));
                return DSLBuilder.this;
            }

            public DSLBuilder isDefaultInAVisibleProject() {
                defaulTeamsInProjects.add(teamByName.get(teamName));
                return DSLBuilder.this;
            }

            public DSLBuilder userIsMemberOf(String ...teams) {
                return DSLBuilder.this.userIsMemberOf(teams);
            }

            public DSLTeam team(String anotherTeam) {
                return DSLBuilder.this.team(anotherTeam);
            }
        }
    }
}
