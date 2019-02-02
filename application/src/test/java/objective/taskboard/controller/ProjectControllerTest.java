package objective.taskboard.controller;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.auth.authorizer.Authorizer;
import objective.taskboard.controller.ProjectCreationData.ProjectCreationDataTeam;
import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.followup.FollowUpFacade;
import objective.taskboard.jira.AuthorizedProjectsService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.repository.TeamCachedRepository;
import objective.taskboard.repository.TeamFilterConfigurationCachedRepository;


@RunWith(MockitoJUnitRunner.class)
public class ProjectControllerTest {

    @Mock
    private TeamCachedRepository teamRepository;

    @Mock
    private TeamFilterConfigurationCachedRepository teamFilterConfigurationRepository;

    @Mock
    private ProjectService projectService;

    @Mock
    private AuthorizedProjectsService authorizedProjectsService;

    @Mock
    private FollowUpFacade followUpFacade;

    @Mock
    private Authorizer authorizer;

    @Captor
    private ArgumentCaptor<Team> teamCaptor;

    private ProjectController controller;

    @Before
    public void setup() {
        controller = new ProjectController(teamRepository, teamFilterConfigurationRepository, projectService, followUpFacade, authorizer);

        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> {
            Team team  = (Team) invocation.getArguments()[0];
            team.setId(123L);
            return team;
        });

        when(teamFilterConfigurationRepository.save(any())).then(invocation -> invocation.getArguments()[0]);
    }

    @Test
    public void create_ifLeaderIsNotATeamMember_shouldAddTeamLeaderAsATeamMemberWithManagerRole() {
        ProjectCreationData data = buildProjectCreationData("my.team.leader", "member.one", "member.two");
        controller.create(data);

        verify(teamRepository).save(teamCaptor.capture());

        Team team = teamCaptor.getValue();
        assertTeamMembers(team, "member.one", "member.two", "my.team.leader");

        UserTeam leader = getUser(team, "my.team.leader");
        assertEquals(UserTeamRole.MANAGER, leader.getRole());
    }

    @Test
    public void create_ifLeaderIsATeamMember_shouldOnlySetLeaderRoleToManager() {
        ProjectCreationData data = buildProjectCreationData("my.team.leader", "member.one", "my.team.leader", "member.two");
        controller.create(data);

        verify(teamRepository).save(teamCaptor.capture());

        Team team = teamCaptor.getValue();
        assertTeamMembers(team, "member.one", "my.team.leader", "member.two");

        UserTeam leader = getUser(team, "my.team.leader");
        assertEquals(UserTeamRole.MANAGER, leader.getRole());
    }

    private ProjectCreationData buildProjectCreationData(String leader, String... members) {
        ProjectCreationData data = new ProjectCreationData();
        data.projectKey = "PROJ";
        data.teamLeader = leader;
        data.defaultTeam = new ProjectCreationDataTeam();
        data.defaultTeam.name = "My default Team";
        data.defaultTeam.members = asList(members);
        return data;
    }

    private UserTeam getUser(Team team, String username) {
        return team.getMembers().stream()
                .filter(m -> m.getUserName().equals(username))
                .findAny()
                .get();
    }

    private void assertTeamMembers(Team team, String... expectedMembers) {
        String expected = join(", ", expectedMembers);
        String actual = team.getMembers().stream()
                .map(m -> m.getUserName())
                .collect(Collectors.joining(", "));
        assertEquals(expected, actual);
    }

}