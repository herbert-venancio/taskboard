package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
import static objective.taskboard.auth.authorizer.permission.PermissionTestUtils.userTeam;
import static objective.taskboard.repository.UserTeamRepositoryMockBuilder.userTeamRepository;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.PermissionTestUtils.PermissionTest;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;

public class AnyTeamPermissionAnyAcceptableRoleTest implements PermissionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    @Test
    public void testName() {
        Permission subject = new AnyTeamPermissionAnyAcceptableRole("PERMISSION_NAME", loggedUserDetails, userTeamRepository().build(), UserTeamRole.MANAGER);
        assertEquals("PERMISSION_NAME", subject.name());
    }

    @Test
    public void testAcceptsArguments() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Only PermissionContext.empty() is allowed for permission PERMISSION_NAME."));

        Permission subject = new AnyTeamPermissionAnyAcceptableRole("PERMISSION_NAME", loggedUser().build(), userTeamRepository().build(), UserTeamRole.MANAGER);

        subject.accepts(new PermissionContext("target"));
    }

    @Test
    public void testAccepts() {
        UserTeamCachedRepository userTeamRepo = userTeamRepository()
                .withUserTeamList(
                        "USER1",
                        userTeam("USER1", "TEAM1", UserTeamRole.MANAGER),
                        userTeam("USER1", "TEAM2", UserTeamRole.MEMBER)
                ).withUserTeamList(
                        "USER2",
                        userTeam("USER2", "TEAM1", UserTeamRole.VIEWER),
                        userTeam("USER2", "TEAM2", UserTeamRole.VIEWER)
                ).build();

        LoggedUserDetails userWithPermission = loggedUser().withName("USER1").build();
        Permission subject = new AnyTeamPermissionAnyAcceptableRole("PERMISSION_NAME", userWithPermission, userTeamRepo, UserTeamRole.MANAGER, UserTeamRole.MEMBER);
        assertTrue(subject.accepts(PermissionContext.empty()));

        LoggedUserDetails userWithoutPermission = loggedUser().withName("USER2").build();
        subject = new AnyTeamPermissionAnyAcceptableRole("PERMISSION_NAME", userWithoutPermission, userTeamRepo, UserTeamRole.MANAGER, UserTeamRole.MEMBER);
        assertFalse(subject.accepts(PermissionContext.empty()));
    }

}
