package objective.taskboard.team;

import static objective.taskboard.data.UserTeam.UserTeamRole.MANAGER;
import static objective.taskboard.data.UserTeam.UserTeamRole.MEMBER;
import static objective.taskboard.data.UserTeam.UserTeamRole.VIEWER;
import static objective.taskboard.team.UserTeamTestUtils.assertTeams;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import objective.taskboard.auth.authorizer.permission.TeamEditPermission;
import objective.taskboard.filterConfiguration.TeamFilterConfigurationService;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.UserTeamCachedRepository;

public class UserTeamServiceTest {

    private UserTeamCachedRepository userTeamRepo = mock(UserTeamCachedRepository.class);
    private TeamEditPermission teamEditPermission = mock(TeamEditPermission.class);
    private TeamCachedRepository teamRepo = mock(TeamCachedRepository.class);
    private TeamFilterConfigurationService teamFilterConfigurationService = mock(TeamFilterConfigurationService.class);
    private UserTeamPermissionService userTeamPermissionService = mock(UserTeamPermissionService.class);
    private UserTeamService subject = new UserTeamService(userTeamRepo, teamRepo, userTeamPermissionService, teamEditPermission);


    private UserTeamTestUtils testUtils = new UserTeamTestUtils(teamFilterConfigurationService, teamRepo, userTeamRepo, teamEditPermission, null);

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

        assertTeams("Blue, Extra, Rocket", subject.getTeamsInWhichUserIsValidAsAssignee("my.user"));
    }

    private UserTeamTestUtils.DSLBuilder withTeams() {
        return testUtils.withTeams();
    }

}
