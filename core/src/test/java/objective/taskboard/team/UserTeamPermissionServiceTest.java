package objective.taskboard.team;

import static objective.taskboard.team.UserTeamTestUtils.assertTeams;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.filter.TeamFilterConfigurationService;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.UserTeamCachedRepository;
import objective.taskboard.team.UserTeamTestUtils.DSLBuilder;

public class UserTeamPermissionServiceTest {

    private LoggedUserDetails loggedInUser = mock(LoggedUserDetails.class);
    private UserTeamCachedRepository userTeamRepo = mock(UserTeamCachedRepository.class);
    private TeamCachedRepository teamRepo = mock(TeamCachedRepository.class);
    private TeamFilterConfigurationService teamFilterConfigurationService = mock(TeamFilterConfigurationService.class);
    private UserTeamPermissionService subject = new UserTeamPermissionService(userTeamRepo, teamRepo, teamFilterConfigurationService, loggedInUser);

    private UserTeamTestUtils testUtils = new UserTeamTestUtils(teamFilterConfigurationService, teamRepo, userTeamRepo, null, loggedInUser);

    @Before
    public void setup() {
        when(loggedInUser.getUsername()).thenReturn("mary");
    }

    @Test
    public void getTeamsVisibleToLoggedInUser_regularUser_shouldReturnDefaultTeamsOfVisibleProjects() {
        withTeams()
            .team("Rocket").isDefaultInAVisibleProject()
            .team("Super")
            .team("Extra")
            .and()
        .loggedUser()
            .isNotAdmin();

        assertTeams("Rocket", subject.getTeamsVisibleToLoggedInUser());
    }

    @Test
    public void getTeamsVisibleToLoggedInUser_adminUser_shouldReturnAllTeams() {
        withTeams()
            .team("Super")
            .team("Extra")
            .and()
        .loggedUser()
            .isAdmin();

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
            .isNotAdmin();

        assertTeams("Global", subject.getTeamsVisibleToLoggedInUser());
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
            .isNotAdmin();

        assertTeams("Extra, Super", subject.getTeamsVisibleToLoggedInUser());
    }

    private DSLBuilder withTeams() {
        return testUtils.withTeams();
    }

}