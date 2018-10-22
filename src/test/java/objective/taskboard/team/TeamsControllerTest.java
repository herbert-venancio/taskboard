package objective.taskboard.team;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static objective.taskboard.auth.authorizer.Permissions.TASKBOARD_ADMINISTRATION;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.util.Arrays;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.auth.authorizer.Authorizer;
import objective.taskboard.data.Team;
import objective.taskboard.team.TeamsController.TeamDto;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;

@RunWith(MockitoJUnitRunner.class)
public class TeamsControllerTest {

    @Test
    public void getTeams_ifLoggedUserIsntAdmin_returnNotFound() {
        TeamsController subject = TeamsControllerMockBuilder.init()
            .withTaskboardAdministrationPermission(false)
            .build();

        AssertResponse.of(subject.getTeams())
            .httpStatus(NOT_FOUND)
            .emptyBody();
    }

    @Test
    public void getTeams_ifLoggedUserIsAdmin_returnOkWithSortedValues() {
        TeamsController subject = TeamsControllerMockBuilder.init()
            .withTaskboardAdministrationPermission(true)
            .withTeamsThatUserCanAdmin(
                    new Team("TASKBOARD", "john", "", asList("liam", "emma", "mia")),
                    new Team("A", "john", "", asList("liam", "mary")),
                    new Team("SDLC", "john", "", asList("james", "mary"))
            )
            .build();

        AssertResponse.of(subject.getTeams())
            .httpStatus(OK)
            .bodyClassWhenList(0, TeamDto.class)
            .bodyAsJson(
                    "[" +
                        "{" +
                            "\"name\" : \"A\"," +
                            "\"manager\" : \"john\"," +
                            "\"members\" : [ \"liam\", \"mary\" ]" +
                        "},{" +
                            "\"name\" : \"SDLC\"," +
                            "\"manager\" : \"john\"," +
                            "\"members\" : [ \"james\", \"mary\" ]" +
                        "},{" +
                            "\"name\" : \"TASKBOARD\"," +
                            "\"manager\" : \"john\"," +
                            "\"members\" : [ \"emma\", \"liam\", \"mia\" ]" +
                        "}" +
                    "]");
    }

    @Test
    public void getTeam_ifLoggedUserIsntAdmin_returnNotFound() {
        TeamsController subject = TeamsControllerMockBuilder.init()
            .withTaskboardAdministrationPermission(false)
            .build();

        AssertResponse.of(subject.getTeam("ANY"))
            .httpStatus(NOT_FOUND)
            .emptyBody();
    }

    @Test
    public void getTeam_ifLoggedUserIsAdminButHeCantAdminOrTeamDoesntExist_returnBadRequestWithMessage() {
        TeamsController subject = TeamsControllerMockBuilder.init()
            .withTaskboardAdministrationPermission(true)
            .build();

        AssertResponse.of(subject.getTeam("ANY"))
            .httpStatus(BAD_REQUEST)
            .bodyAsString("Team \"ANY\" not found.");
    }

    @Test
    public void getTeam_ifLoggedUserIsAdmin_returnOkWithValue() {
        TeamsController subject = TeamsControllerMockBuilder.init()
            .withTaskboardAdministrationPermission(true)
            .withTeamsThatUserCanAdmin(
                    new Team("SDLC", "john", "", asList("james", "mary"))
            )
            .build();

        AssertResponse.of(subject.getTeam("SDLC"))
            .httpStatus(OK)
            .bodyClass(TeamDto.class)
            .bodyAsJson(
                    "{" +
                        "\"name\" : \"SDLC\"," +
                        "\"manager\" : \"john\"," +
                        "\"members\" : [ \"james\", \"mary\" ]" +
                    "}");
    }

    @Test
    public void updateTeam_ifLoggedUserIsntAdmin_returnNotFound() {
        TeamsController subject = TeamsControllerMockBuilder.init()
            .withTaskboardAdministrationPermission(false)
            .build();

        AssertResponse.of(subject.updateTeam("ANY", new TeamDto()))
            .httpStatus(NOT_FOUND)
            .emptyBody();
    }

    @Test
    public void updateTeam_ifLoggedUserIsAdminButDataIsInvalid_returnBadRequestWithMessage() throws Exception {
        TeamsController subject = TeamsControllerMockBuilder.init()
            .withTaskboardAdministrationPermission(true)
            .build();

        TeamDto TeamDto = TeamDtoBuilder.init()
                .withTeamName("")
                .withManager("")
                .withMembers(
                        "",
                        "",
                        "repeated-member1",
                        "repeated-member1",
                        "repeated-member1",
                        "repeated-member2",
                        "repeated-member2")
                .build();

        AssertResponse.of(subject.updateTeam("ANY", TeamDto))
            .httpStatus(BAD_REQUEST)
            .bodyAsString(
                    "[" +
                        "\"manager\" is required., " +
                        "\"teamName\" is required., " +
                        "Empty member isn't allowed., " +
                        "Member \"repeated-member1\" repeated., " +
                        "Member \"repeated-member2\" repeated." +
                    "]");
    }

    @Test
    public void updateTeam_ifLoggedUserIsAdminButHeCantAdminOrTeamDoesntExist_returnBadRequestWithMessage() {
        TeamsController subject = TeamsControllerMockBuilder.init()
            .withTaskboardAdministrationPermission(true)
            .build();

        TeamDto TeamDto = TeamDtoBuilder.init()
                .withTeamName("ANY")
                .withManager("ANY")
                .build();

        AssertResponse.of(subject.updateTeam("ANY", TeamDto))
            .httpStatus(BAD_REQUEST)
            .bodyAsString("Team \"ANY\" not found.");
    }

    @Test
    public void updateTeam_ifLoggedUserIsAdminAndDataIsValid_saveValuesAndReturnOk() {
        TeamsController subject = TeamsControllerMockBuilder.init()
                .withTaskboardAdministrationPermission(true)
                .withTeamsThatUserCanAdmin(
                        new Team("SDLC", "", "", asList())
                )
                .build();

        TeamDto TeamDto = TeamDtoBuilder.init()
                .withTeamName("new-name")
                .withManager("new-manager")
                .withMembers(
                        "new-member-1",
                        "new-member-2")
                .build();

        AssertResponse.of(subject.updateTeam("SDLC", TeamDto))
            .httpStatus(OK);

        TeamsControllerMockBuilder.teamSavedWithCorrectValues(TeamDto);
    }

    private static class TeamsControllerMockBuilder {

        private static Authorizer authorizer = mock(Authorizer.class);
        private static UserTeamService userTeamService = mock(UserTeamService.class);

        private TeamsController subject = new TeamsController(authorizer, userTeamService);

        public static TeamsControllerMockBuilder init() {
            return new TeamsControllerMockBuilder();
        }

        public TeamsControllerMockBuilder withTaskboardAdministrationPermission(boolean withPermission) {
            when(authorizer.hasPermission(TASKBOARD_ADMINISTRATION)).thenReturn(withPermission);
            return this;
        }

        public TeamsControllerMockBuilder withTeamsThatUserCanAdmin(Team... teams) {
            Set<Team> teamsList = Arrays.stream(teams).collect(toSet());
            when(userTeamService.getTeamsThatUserCanAdmin()).thenReturn(teamsList);
            return this;
        }

        public TeamsController build() {
            return subject;
        }

        public static void teamSavedWithCorrectValues(TeamDto TeamDto) {
            ArgumentCaptor<Team> teamParam = ArgumentCaptor.forClass(Team.class);
            verify(userTeamService).saveTeam(teamParam.capture());
            Team team = teamParam.getValue();

            assertEquals(TeamDto.name, team.getName());
            assertEquals(TeamDto.manager, team.getManager());
            assertEquals(TeamDto.members.stream().collect(joining(",")), team.getMembers().stream().map(member -> member.getUserName()).collect(joining(",")));
        }

    }

    private static class TeamDtoBuilder {

        private TeamDto teamDto  = new TeamDto();

        public static TeamDtoBuilder init() {
            return new TeamDtoBuilder();
        }

        public TeamDtoBuilder withTeamName(String teamName) {
            teamDto.name = teamName;
            return this;
        }

        public TeamDtoBuilder withManager(String manager) {
            teamDto.manager = manager;
            return this;
        }

        public TeamDtoBuilder withMembers(String... members) {
            teamDto.members = asList(members);
            return this;
        }

        public TeamDto build() {
            return teamDto;
        }

    }


}
