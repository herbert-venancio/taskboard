package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
import static objective.taskboard.auth.authorizer.permission.PermissionTestUtils.role;
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

public class AnyProjectPermissionTest implements PermissionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    @Test
    public void testName() {
        Permission subject = new AnyProjectPermission("PERMISSION_NAME", loggedUserDetails, "role1", "role2");
        assertEquals("PERMISSION_NAME", subject.name());
    }

    @Test
    public void testIsAuthorizedArguments() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Only PermissionContext.empty() is allowed for permission PERMISSION_NAME."));

        Permission subject = new AnyProjectPermission("PERMISSION_NAME", loggedUser().build(), "role1", "role2");

        subject.isAuthorized(new PermissionContext("target"));
    }

    @Test
    public void testIsAuthorized() {
        LoggedUserDetails userWithPermission = loggedUser().withRoles(
                role("role1", "PROJ1"),
                role("role3", "PROJ1")
                ).build();
        Permission subject = new AnyProjectPermission("PERMISSION_NAME", userWithPermission, "role1", "role2");
        
        assertTrue(subject.isAuthorized(PermissionContext.empty()));

        LoggedUserDetails userWithoutPermission = loggedUser().withRoles(
                role("role3", "PROJ1"),
                role("role4", "PROJ1")
                ).build();
        subject = new AnyProjectPermission("PERMISSION_NAME", userWithoutPermission, "role1", "role2");
        
        assertFalse(subject.isAuthorized(PermissionContext.empty()));
    }
}
