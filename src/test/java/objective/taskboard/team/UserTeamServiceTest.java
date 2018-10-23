package objective.taskboard.team;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.authorizer.Permissions.TASKBOARD_ADMINISTRATION;
import static objective.taskboard.auth.authorizer.Permissions.TEAM_EDIT;
import static objective.taskboard.utils.StreamUtils.streamOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.Authorizer;
import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.filterConfiguration.TeamFilterConfigurationService;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.UserTeamCachedRepository;

public class UserTeamServiceTest {

    private UserTeamCachedRepository userTeamRepo = mock(UserTeamCachedRepository.class);
    private Authorizer authorizer = mock(Authorizer.class);
    private TeamCachedRepository teamRepo = mock(TeamCachedRepository.class);
    private TeamFilterConfigurationService teamFilterConfigurationService = mock(TeamFilterConfigurationService.class);
    private LoggedUserDetails loggedInUser = mock(LoggedUserDetails.class);
    private UserTeamService subject = new UserTeamService(userTeamRepo, teamRepo, teamFilterConfigurationService, loggedInUser, authorizer);

    @Test
    public void getTeamsThatUserCanAdmin_shouldReturnOnlyPermittedTeams() {
        withTeams()
            .team("Rocket")
            .team("Super")
            .team("Extra")
            .and()
       .user()
           .hasTeamEditPermissionFor("Super", "Extra");

        assertTeams("Extra, Super", subject.getTeamsThatUserCanAdmin());
    }

    @Test
    public void getTeamsVisibleToLoggedInUser_regularUser_shouldReturnTeamsWhereUserIsMemberOf() {
        withTeams()
            .team("Rocket")
            .team("Super")
            .team("Extra")
            .and()
        .user()
            .isMemberOf("Super", "Extra")
            .hasNotTaskboardAdministrationPermission();

        assertTeams("Extra, Super", subject.getTeamsVisibleToLoggedInUser());
    }

    @Test
    public void getTeamsVisibleToLoggedInUser_regularUser_shouldReturnDefaultTeamsOfVisibleProjects() {
        withTeams()
            .team("Rocket").isDefaultInAVisibleProject()
            .team("Super")
            .team("Extra")
            .and()
        .user()
            .hasNotTaskboardAdministrationPermission();

        assertTeams("Rocket", subject.getTeamsVisibleToLoggedInUser());
    }

    @Test
    public void getTeamsVisibleToLoggedInUser_adminUser_shouldReturnAllTeams() {
        withTeams()
            .team("Super")
            .team("Extra")
            .and()
        .user()
            .hasTaskboardAdministrationPermission();

        assertTeams("Extra, Super", subject.getTeamsVisibleToLoggedInUser());
    }

    @Test
    public void getTeamsVisibleToLoggedInUser_shouldReturnGloballyVisibleTeams() {
        withTeams()
            .team("Global").isGloballyVisible()
            .team("Extra")
            .and()
        .user()
            .isMemberOf()
            .hasNotTaskboardAdministrationPermission();

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

        public DSLUser user() {
            return new DSLUser();
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

            public DSLTeam team(String anotherTeam) {
                return DSLBuilder.this.team(anotherTeam);
            }

            public DSLBuilder and() {
                return DSLBuilder.this;
            }
        }

        class DSLUser {

            public DSLUser isMemberOf(String ...teams) {
                when(userTeamRepo.findByUserName("mary")).thenReturn(
                        streamOf(asList(teams))
                            .map(teamName -> new UserTeam("mary", teamName))
                            .collect(toList()));

                return this;
            }

            public DSLUser hasTaskboardAdministrationPermission() {
                when(authorizer.hasPermission(TASKBOARD_ADMINISTRATION)).thenReturn(true);
                return this;
            }

            public DSLUser hasNotTaskboardAdministrationPermission() {
                when(authorizer.hasPermission(TASKBOARD_ADMINISTRATION)).thenReturn(false);
                return this;
            }

            public DSLUser hasTeamEditPermissionFor(String... teamsName) {
                stream(teamsName).forEach(name -> when(authorizer.hasPermission(TEAM_EDIT, name)).thenReturn(true));
                return this;
            }

        }

    }
}
