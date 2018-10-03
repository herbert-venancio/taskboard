package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
import static objective.taskboard.auth.authorizer.permission.PermissionTestUtils.role;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.PermissionTestUtils.PermissionTest;

public class AnyProjectPermissionTest implements PermissionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testName() {
        Permission subject = new AnyProjectPermission("PERMISSION_NAME", "role1", "role2");
        assertEquals("PERMISSION_NAME", subject.name());
    }

    @Test
    public void testAcceptsArguments() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Only PermissionContext.empty() is allowed for permission PERMISSION_NAME."));

        Permission subject = new AnyProjectPermission("PERMISSION_NAME", "role1", "role2");

        subject.accepts(loggedUser().build(), new PermissionContext("target"));
    }

    @Test
    public void testAccepts() {
        Permission subject = new AnyProjectPermission("PERMISSION_NAME", "role1", "role2");

        LoggedUserDetails userWithPermission = loggedUser().withRoles(
                role("role1", "PROJ1"),
                role("role3", "PROJ1")
                ).build();
        assertTrue(subject.accepts(userWithPermission, PermissionContext.empty()));

        LoggedUserDetails userWithoutPermission = loggedUser().withRoles(
                role("role3", "PROJ1"),
                role("role4", "PROJ1")
                ).build();
        assertFalse(subject.accepts(userWithoutPermission, PermissionContext.empty()));
    }

}
