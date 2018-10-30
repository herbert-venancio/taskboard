package objective.taskboard.team;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.authorizer.Permissions.TASKBOARD_ADMINISTRATION;
import static objective.taskboard.auth.authorizer.Permissions.TEAM_EDIT;
import static objective.taskboard.data.UserTeam.UserTeamRole.MANAGER;
import static objective.taskboard.data.UserTeam.UserTeamRole.MEMBER;
import static objective.taskboard.data.UserTeam.UserTeamRole.VIEWER;
import static objective.taskboard.utils.StreamUtils.streamOf;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import objective.taskboard.data.UserTeam.UserTeamRole;
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
       .loggedUser()
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
        .loggedUser()
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
        .loggedUser()
            .hasNotTaskboardAdministrationPermission();

        assertTeams("Rocket", subject.getTeamsVisibleToLoggedInUser());
    }

    @Test
    public void getTeamsVisibleToLoggedInUser_adminUser_shouldReturnAllTeams() {
        withTeams()
            .team("Super")
            .team("Extra")
            .and()
        .loggedUser()
            .hasTaskboardAdministrationPermission();

        assertTeams("Extra, Super", subject.getTeamsVisibleToLoggedInUser());
    }

    @Test
    public void getTeamsVisibleToLoggedInUser_shouldReturnGloballyVisibleTeams() {
        withTeams()
            .team("Global").isGloballyVisible()
            .team("Extra")
            .and()
        .loggedUser()
            .isMemberOf()
            .hasNotTaskboardAdministrationPermission();

        assertTeams("Global", subject.getTeamsVisibleToLoggedInUser());
    }

    @Test
    public void getTeamsThatUserIsAValidAssignee_shouldReturnOnlyTeamsWhereUserIsAManagerOrMember() {
        withTeams()
            .team("Global")
            .team("Rocket")
            .team("Extra")
            .team("Blue")
            .team("Super")
            .and()
        .user("my.user")
            .belongsToTeam("Rocket").withRole(MANAGER)
            .belongsToTeam("Extra").withRole(MEMBER)
            .belongsToTeam("Blue").withRole(MEMBER)
            .belongsToTeam("Super").withRole(VIEWER);

        assertTeams("Blue, Extra, Rocket", subject.getTeamsThatUserIsAValidAssignee("my.user"));
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

        public DSLLoggedUser loggedUser() {
            return new DSLLoggedUser();
        }

        public DSLUser user(String username) {
            return new DSLUser(username);
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

            private String lastUsername;
            private UserTeam lastUserTeam;
            private final Map<String, List<UserTeam>> userTeams = new HashMap<>();

            DSLUser(String username) {
                if (!userTeams.containsKey(username)) {
                    List<UserTeam> userTeamList = new ArrayList<>();
                    userTeams.put(username, userTeamList);
                    when(userTeamRepo.findByUserName(username)).thenReturn(userTeamList);
                }

                lastUsername = username;
            }

            public DSLUser belongsToTeam(String team) {
                UserTeam userTeam = mock(UserTeam.class);
                when(userTeam.getTeam()).thenReturn(team);
                userTeams.get(lastUsername).add(userTeam);
                lastUserTeam = userTeam;
                return this;
            }

            public DSLUser withRole(UserTeamRole userTeamRole) {
                when(lastUserTeam.getRole()).thenReturn(userTeamRole);
                return this;
            }

        }

        class DSLLoggedUser {
            public DSLLoggedUser isMemberOf(String ...teams) {
                when(userTeamRepo.findByUserName("mary")).thenReturn(
                        streamOf(asList(teams))
                            .map(teamName -> new UserTeam("mary", teamName))
                            .collect(toList()));

                return this;
            }

            public DSLLoggedUser hasTaskboardAdministrationPermission() {
                when(authorizer.hasPermission(TASKBOARD_ADMINISTRATION)).thenReturn(true);
                return this;
            }

            public DSLLoggedUser hasNotTaskboardAdministrationPermission() {
                when(authorizer.hasPermission(TASKBOARD_ADMINISTRATION)).thenReturn(false);
                return this;
            }

            public DSLLoggedUser hasTeamEditPermissionFor(String... teamsName) {
                stream(teamsName).forEach(name -> when(authorizer.hasPermission(TEAM_EDIT, name)).thenReturn(true));
                return this;
            }

        }

    }
}
