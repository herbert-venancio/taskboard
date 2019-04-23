package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.PermissionTestUtils.PermissionTest;
import objective.taskboard.data.Team;
import objective.taskboard.data.UserTeam;
import objective.taskboard.team.UserTeamPermissionService;

public class UserVisibilityPermissionTest implements PermissionTest {

    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    @Test
    @Override
    public void testName() {
        Permission subject = permission()
                .build();

        assertEquals("user.visibility", subject.name());
    }

    @Test
    @Override
    public void testIsAuthorized() {
        assertTrue(givenUserWithTaskboardAdministrationPermission());

        assertTrue(givenUserWithoutTaskboardAdministrationPermission_butWithTeamInCommonUserTargettedUser());

        assertFalse(givenUserWithoutTaskboardAdministrationPermission_andNoTeamInCommonUserTargettedUser());

        assertTrue(givenNoOtherPermission_butLoggedInUserAndTargetWithSameName());
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

    private boolean givenNoOtherPermission_butLoggedInUserAndTargetWithSameName() {
        UserVisibilityPermission subject = permission()
                .withUserTaskboardAdministrationPermission(false)
                .withVisibleTeams()
                .build();

        LoggedUserDetails loggedInUser = mock(LoggedUserDetails.class);
        when(loggedInUser.defineUsername()).thenReturn("Mark");

        return subject.isAuthorized(loggedInUser, "Mark");
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
            when(tbAdminPermission.isAuthorized(any())).thenReturn(hasPermission);
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
