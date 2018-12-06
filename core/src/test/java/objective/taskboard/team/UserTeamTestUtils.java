package objective.taskboard.team;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
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

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.TeamEditPermission;
import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.filterConfiguration.TeamFilterConfigurationService;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.UserTeamCachedRepository;

public class UserTeamTestUtils {

    private final TeamFilterConfigurationService teamFilterConfigurationService;
    private final TeamCachedRepository teamRepo;
    private final UserTeamCachedRepository userTeamRepo;
    private final TeamEditPermission teamEditPermission;
    private final LoggedUserDetails loggedUserDetails;

    public UserTeamTestUtils(
            TeamFilterConfigurationService teamFilterConfigurationService,
            TeamCachedRepository teamRepo,
            UserTeamCachedRepository userTeamRepo,
            TeamEditPermission teamEditPermission,
            LoggedUserDetails loggedUserDetails) {
        this.teamFilterConfigurationService = teamFilterConfigurationService;
        this.teamRepo = teamRepo;
        this.userTeamRepo = userTeamRepo;
        this.teamEditPermission = teamEditPermission;
        this.loggedUserDetails = loggedUserDetails;
    }

    public static void assertTeams(String expectedNames, Collection<Team> actual) {
        assertEquals(expectedNames, actual.stream().map(t -> t.getName()).sorted().collect(joining(", ")));
    }

    public DSLBuilder withTeams() {
        return new DSLBuilder();
    }

    public class DSLBuilder {
        List<Team> defaulTeamsInProjects = new LinkedList<Team>();
        List<Team> globallyVisibleTeams = new LinkedList<Team>();
        Map<String, Team> teamByName = new HashMap<>();

        public DSLBuilder() {
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

            public UserTeamTestUtils.DSLBuilder isGloballyVisible() {
                globallyVisibleTeams.add(teamByName.get(teamName));
                return UserTeamTestUtils.DSLBuilder.this;
            }

            public UserTeamTestUtils.DSLBuilder isDefaultInAVisibleProject() {
                defaulTeamsInProjects.add(teamByName.get(teamName));
                return UserTeamTestUtils.DSLBuilder.this;
            }

            public DSLTeam team(String anotherTeam) {
                return UserTeamTestUtils.DSLBuilder.this.team(anotherTeam);
            }

            public UserTeamTestUtils.DSLBuilder and() {
                return UserTeamTestUtils.DSLBuilder.this;
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

            public DSLLoggedUser hasTeamEditPermissionFor(String... teamsName) {
                stream(teamsName).forEach(name -> when(teamEditPermission.isAuthorizedFor(name)).thenReturn(true));
                return this;
            }

            public DSLLoggedUser isNotAdmin() {
                when(loggedUserDetails.isAdmin()).thenReturn(false);
                return this;
            }

            public DSLLoggedUser isAdmin() {
                when(loggedUserDetails.isAdmin()).thenReturn(true);
                return this;
            }

        }

    }
}
