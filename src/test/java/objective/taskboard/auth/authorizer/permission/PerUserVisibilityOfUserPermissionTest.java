package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
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
import objective.taskboard.team.UserTeamService;

public class PerUserVisibilityOfUserPermissionTest implements PermissionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testName() {
        Permission subject = perUserVisibilityOfUserPermission("PERMISSION_NAME")
                .build();

        assertEquals("PERMISSION_NAME", subject.name());
    }

    @Test
    public void testAcceptsArguments() {
        Permission subject = perUserVisibilityOfUserPermission("PERMISSION_NAME")
                .build();

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Empty PermissionContext isn't allowed for permission PERMISSION_NAME."));

        LoggedUserDetails userDetails = mock(LoggedUserDetails.class);

        subject.accepts(userDetails, PermissionContext.empty());
    }

    @Test
    public void testAccepts() {
        assertTrue(givenUserWithTaskboardAdministrationPermission());

        assertTrue(givenUserWithoutTaskboardAdministrationPermission_butWithTeamInCommonUserTargettedUser());

        assertFalse(givenUserWithoutTaskboardAdministrationPermission_andNoTeamInCommonUserTargettedUser());
    }

    private boolean givenUserWithTaskboardAdministrationPermission() {
        Permission subject = perUserVisibilityOfUserPermission("PERMISSION_NAME")
                .withUserTaskboardAdministrationPermission(true)
                .withVisibleTeams()
                .build();

        LoggedUserDetails userDetails = mock(LoggedUserDetails.class);

        return subject.accepts(userDetails, new PermissionContext("USER_A"));
    }

    private boolean givenUserWithoutTaskboardAdministrationPermission_butWithTeamInCommonUserTargettedUser() {
        Permission subject = perUserVisibilityOfUserPermission("PERMISSION_NAME")
                .withUserTaskboardAdministrationPermission(false)
                .withVisibleTeams(
                        teamWithMembers("John", "Mary"),
                        teamWithMembers("Peter", "Joseph"))
                .build();

        LoggedUserDetails userDetails = mock(LoggedUserDetails.class);

        return subject.accepts(userDetails, new PermissionContext("John"));
    }

    private boolean givenUserWithoutTaskboardAdministrationPermission_andNoTeamInCommonUserTargettedUser() {
        Permission subject = perUserVisibilityOfUserPermission("PERMISSION_NAME")
                .withUserTaskboardAdministrationPermission(false)
                .withVisibleTeams(
                        teamWithMembers("John", "Mary"),
                        teamWithMembers("Peter", "Joseph"))
                .build();

        LoggedUserDetails userDetails = mock(LoggedUserDetails.class);

        return subject.accepts(userDetails, new PermissionContext("Mark"));
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

    public DSLBuilder perUserVisibilityOfUserPermission(String permissionName) {
        return new DSLBuilder(permissionName);
    }

    private static class DSLBuilder {

        private TaskboardAdministrationPermission tbAdminPermission = mock(TaskboardAdministrationPermission.class);
        private UserTeamService userTeamService = mock(UserTeamService.class);

        private String permissionName = "";

        private DSLBuilder(String permissionName) {
            this.permissionName = permissionName;
        }

        public DSLBuilder withUserTaskboardAdministrationPermission(boolean hasPermission) {
            when(tbAdminPermission.accepts(any(), any())).thenReturn(hasPermission);
            return this;
        }

        public DSLBuilder withVisibleTeams(Team... teams) {
            Set<Team> visibleTeams = stream(teams).collect(toSet());
            when(userTeamService.getTeamsVisibleToLoggedInUser()).thenReturn(visibleTeams);
            return this;
        }

        public PerUserVisibilityOfUserPermission build() {
            return new PerUserVisibilityOfUserPermission(permissionName, tbAdminPermission, userTeamService);
        }

    }

}
