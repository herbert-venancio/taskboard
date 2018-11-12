package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
import static objective.taskboard.auth.authorizer.permission.PermissionTestUtils.userTeam;
import static objective.taskboard.repository.UserTeamRepositoryMockBuilder.userTeamRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.PermissionTestUtils.PermissionTest;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;

public class PerTeamPermissionAnyAcceptableRoleTest implements PermissionTest {

    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    @Test
    @Override
    public void testName() {
        Permission subject = new PerTeamPermissionAnyAcceptableRole("PERMISSION_NAME", loggedUserDetails, userTeamRepository().build(), UserTeamRole.MANAGER);
        assertEquals("PERMISSION_NAME", subject.name());
    }

    @Test
    @Override
    public void testIsAuthorized() {
        UserTeamCachedRepository userTeamRepo = userTeamRepository().withUserTeamList(
                "USER",
                userTeam("USER", "TEAM1", UserTeamRole.MANAGER),
                userTeam("USER", "TEAM2", UserTeamRole.MEMBER),
                userTeam("USER", "TEAM3", UserTeamRole.VIEWER)
                ).build();

        PerTeamPermissionAnyAcceptableRole subject = new PerTeamPermissionAnyAcceptableRole("PERMISSION_NAME", loggedUser().withName("USER").build(), userTeamRepo, UserTeamRole.MANAGER, UserTeamRole.MEMBER);

        assertTrue(subject.isAuthorizedFor("TEAM1"));

        assertTrue(subject.isAuthorizedFor("TEAM2"));

        assertFalse(subject.isAuthorizedFor("TEAM3"));
    }

}
