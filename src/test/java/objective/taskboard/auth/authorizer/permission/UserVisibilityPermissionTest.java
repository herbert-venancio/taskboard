package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.PermissionTestUtils.PermissionTest;
import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.team.UserTeamPermissionService;

public class UserVisibilityPermissionTest implements PermissionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    @Test
    public void testName() {
        Permission subject = permission()
                .build();

        assertEquals("user.visibility", subject.name());
    }

    @Test
    public void testIsAuthorizedArguments() {
        UserVisibilityPermission subject = permission()
                .build();

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Empty PermissionContext isn't allowed for permission user.visibility."));

        subject.isAuthorized();
    }

    @Test
    public void testIsAuthorized() {
        assertTrue(givenUserWithTaskboardAdministrationPermission());

        assertTrue(givenUserWithoutTaskboardAdministrationPermission_butWithTeamInCommonUserTargettedUser());

        assertFalse(givenUserWithoutTaskboardAdministrationPermission_andNoTeamInCommonUserTargettedUser());
    }

    private boolean givenUserWithTaskboardAdministrationPermission() {
        UserVisibilityPermission subject = permission()
                .withUserTaskboardAdministrationPermission(true)
                .withVisibleTeams()
                .build();

        return subject.isAuthorizedFor("USER_A");
    }

    private boolean givenUserWithoutTaskboardAdministrationPermission_butWithTeamInCommonUserTargettedUser() {
        UserVisibilityPermission subject = permission()
                .withUserTaskboardAdministrationPermission(false)
                .withVisibleTeams(
                        teamWithMembers("John", "Mary"),
                        teamWithMembers("Peter", "Joseph"))
                .build();

        return subject.isAuthorizedFor("John");
    }

    private boolean givenUserWithoutTaskboardAdministrationPermission_andNoTeamInCommonUserTargettedUser() {
        UserVisibilityPermission subject = permission()
                .withUserTaskboardAdministrationPermission(false)
                .withVisibleTeams(
                        teamWithMembers("John", "Mary"),
                        teamWithMembers("Peter", "Joseph"))
                .build();

        return subject.isAuthorizedFor("Mark");
    }

    private Team teamWithMembers(String... members) {
        Team team = mock(Team.class);
        List<UserTeam> userTeamList = stream(members)
                .map(memberName -> {
                    UserTeam userTeam = mock(UserTeam.class);
                    when(userTeam.getUserName()).thenReturn(memberName);
                    return userTeam;
                })
                .collect(toList());
        when(team.getMembers()).thenReturn(userTeamList);
        return team;
    }

    public DSLBuilder permission() {
        return new DSLBuilder();
    }

    private class DSLBuilder {

        private TaskboardAdministrationPermission tbAdminPermission = mock(TaskboardAdministrationPermission.class);
        private UserTeamPermissionService userTeamPermissionService = mock(UserTeamPermissionService.class);

        public DSLBuilder withUserTaskboardAdministrationPermission(boolean hasPermission) {
            when(tbAdminPermission.isAuthorized()).thenReturn(hasPermission);
            return this;
        }

        public DSLBuilder withVisibleTeams(Team... teams) {
            Set<Team> visibleTeams = stream(teams).collect(toSet());
            when(userTeamPermissionService.getTeamsVisibleToLoggedInUser()).thenReturn(visibleTeams);
            return this;
        }

        public UserVisibilityPermission build() {
            return new UserVisibilityPermission(tbAdminPermission, loggedUserDetails, userTeamPermissionService);
        }

    }

}
