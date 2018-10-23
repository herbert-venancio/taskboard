package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
import static objective.taskboard.auth.authorizer.permission.PermissionTestUtils.userTeam;
import static objective.taskboard.repository.UserTeamRepositoryMockBuilder.userTeamRepository;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.PermissionTestUtils.PermissionTest;
import objective.taskboard.data.UserTeam.UserTeamRole;
import objective.taskboard.repository.UserTeamCachedRepository;

public class PerTeamPermissionAnyAcceptableRoleTest implements PermissionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testName() {
        Permission subject = new PerTeamPermissionAnyAcceptableRole("PERMISSION_NAME", userTeamRepository().build(), UserTeamRole.MANAGER);
        assertEquals("PERMISSION_NAME", subject.name());
    }

    @Test
    public void testAcceptsArguments() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Empty PermissionContext isn't allowed for permission PERMISSION_NAME."));

        Permission subject = new PerTeamPermissionAnyAcceptableRole("PERMISSION_NAME", userTeamRepository().build(), UserTeamRole.MANAGER);

        subject.accepts(loggedUser().build(), PermissionContext.empty());
    }

    @Test
    public void testAccepts() {
        UserTeamCachedRepository userTeamRepo = userTeamRepository().withUserTeamList(
                "USER",
                userTeam("USER", "TEAM1", UserTeamRole.MANAGER),
                userTeam("USER", "TEAM2", UserTeamRole.MEMBER),
                userTeam("USER", "TEAM3", UserTeamRole.VIEWER)
                ).build();

        Permission subject = new PerTeamPermissionAnyAcceptableRole("PERMISSION_NAME", userTeamRepo, UserTeamRole.MANAGER, UserTeamRole.MEMBER);

        LoggedUserDetails user = loggedUser().withName("USER").build();

        assertTrue(subject.accepts(user, new PermissionContext("TEAM1")));

        assertTrue(subject.accepts(user, new PermissionContext("TEAM2")));

        assertFalse(subject.accepts(user, new PermissionContext("TEAM3")));
    }

}
