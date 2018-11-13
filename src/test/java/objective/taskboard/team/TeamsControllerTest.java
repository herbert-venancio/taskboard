package objective.taskboard.team;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.auth.authorizer.permission.TeamsEditViewPermission;
import objective.taskboard.data.Team;
import objective.taskboard.team.TeamsController.TeamDto;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;

@RunWith(MockitoJUnitRunner.class)
public class TeamsControllerTest {

    private UserTeamService userTeamService = mock(UserTeamService.class);

    @Test
    public void getTeams_ifLoggedUserDoesNotHaveRequiredPermission_returnNotFound() {
        TeamsController subject = teamsControllerBuilder()
            .withoutTeamsEditViewPermission()
            .build();

        AssertResponse.of(subject.getTeams())
            .httpStatus(NOT_FOUND)
            .emptyBody();
    }

    @Test
    public void getTeams_ifLoggedUserHasTaskboardAdministrationPermission_returnOkWithSortedValues() {
        TeamsController subject = teamsControllerBuilder()
            .withTeamsEditViewPermission()
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
    public void getTeams_ifLoggedUserHasTeamsEditViewPermission_returnOkWithPermittedTeams() {
        TeamsController subject = teamsControllerBuilder()
            .withTeamsEditViewPermission()
            .withTeamsThatUserCanAdmin(
                    new Team("TASKBOARD", "john", "", asList("liam", "emma", "mia")),
                    new Team("SDLC", "john", "", asList("james", "mary"))
            )
            .build();

        AssertResponse.of(subject.getTeams())
            .httpStatus(OK)
            .bodyClassWhenList(0, TeamDto.class)
            .bodyAsJson(
                    "[" +
                        "{" +
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
    public void getTeam_ifLoggedUserDoesNotHaveTeamEditViewPermission_returnNotFound() {
        TeamsController subject = teamsControllerBuilder()
                .withoutTeamsEditViewPermission()
                .build();
        
        AssertResponse.of(subject.getTeam("ANY"))
                .httpStatus(NOT_FOUND)
                .emptyBody();
    }

    @Test
    public void getTeam_ifLoggedUserCantAdminRequestedTeam_returnNotFound() {
        TeamsController subject = teamsControllerBuilder()
            .withTeamsEditViewPermission()
            .withTeamsThatUserCanAdmin(
                    new Team("ONE", "john", "", asList("james", "mary"))
            )
            .build();

        AssertResponse.of(subject.getTeam("ANOTHER"))
            .httpStatus(NOT_FOUND)
            .bodyAsString("Team \"ANOTHER\" not found.");
    }

    @Test
    public void getTeam_ifLoggedUserHasValidPermissions_returnOkWithValue() {
        TeamsController subject = teamsControllerBuilder()
                .withTeamsEditViewPermission()
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
    public void updateTeam_ifLoggedUserDoesNotHaveTeamEditViewPermission_returnNotFound() {
        TeamsController subject = teamsControllerBuilder()
            .withoutTeamsEditViewPermission()
            .build();

        AssertResponse.of(subject.updateTeam("ANY", new TeamDto()))
            .httpStatus(NOT_FOUND)
            .emptyBody();
    }

    @Test
    public void updateTeam_ifLoggedUserHasTeamEditViewPermissionButDataIsInvalid_returnBadRequestWithMessage() throws Exception {
        TeamsController subject = teamsControllerBuilder()
            .withTeamsEditViewPermission()
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
    public void updateTeam_ifLoggedUserCantAdminRequestedTeam_returnNotFoundWithMessage() {
        TeamsController subject = teamsControllerBuilder()
            .withTeamsEditViewPermission()
            .withTeamsThatUserCanAdmin(new Team("ONE", "", "", asList()))
            .build();

        TeamDto TeamDto = TeamDtoBuilder.init()
                .withTeamName("ANY")
                .withManager("ANY")
                .build();

        AssertResponse.of(subject.updateTeam("ANOTHER", TeamDto))
            .httpStatus(NOT_FOUND)
            .bodyAsString("Team \"ANOTHER\" not found.");
    }

    @Test
    public void updateTeam_ifLoggedUserHasValidPermissionsAndDataIsValid_saveValuesAndReturnOk() {
        DLSBuilder builder = teamsControllerBuilder()
                .withTeamsEditViewPermission()
                .withTeamsThatUserCanAdmin(
                        new Team("SDLC", "", "", asList())
                );

        TeamsController subject = builder
                .build();

        TeamDto TeamDto = TeamDtoBuilder.init()
                .withManager("new-manager")
                .withTeamName("new-name")
                .withManager("new-manager")
                .withMembers(
                        "new-member-1",
                        "new-member-2")
                .build();

        AssertResponse.of(subject.updateTeam("SDLC", TeamDto))
            .httpStatus(OK);

        teamSavedWithCorrectValues(TeamDto);
    }

    public DLSBuilder teamsControllerBuilder() {
        return new DLSBuilder();
    }

    public void teamSavedWithCorrectValues(TeamDto TeamDto) {
        ArgumentCaptor<Team> teamParam = ArgumentCaptor.forClass(Team.class);
        verify(userTeamService).saveTeam(teamParam.capture());
        Team team = teamParam.getValue();

        assertEquals(TeamDto.name, team.getName());
        assertEquals(TeamDto.manager, team.getManager());
        assertEquals(TeamDto.members.stream().collect(joining(",")), team.getMembers().stream().map(member -> member.getUserName()).collect(joining(",")));
    }

    private class DLSBuilder {

        private TeamsEditViewPermission teamsEditViewPermission = mock(TeamsEditViewPermission.class);
        private TeamsController subject = new TeamsController(userTeamService, teamsEditViewPermission);

        public DLSBuilder withTeamsEditViewPermission() {
            when(teamsEditViewPermission.isAuthorized()).thenReturn(true);
            return this;
        }

        public DLSBuilder withoutTeamsEditViewPermission() {
            when(teamsEditViewPermission.isAuthorized()).thenReturn(false);
            return this;
        }

        public DLSBuilder withTeamsThatUserCanAdmin(Team... teams) {
            Set<Team> teamsList = stream(teams).collect(toSet());
            when(userTeamService.getTeamsThatUserCanAdmin()).thenReturn(teamsList);
            return this;
        }

        public TeamsController build() {
            return subject;
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
