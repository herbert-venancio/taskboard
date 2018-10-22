package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.authorizer.permission.PermissionTestUtils.userWithIsAdmin;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import objective.taskboard.auth.authorizer.permission.PermissionTestUtils.PermissionTest;

public class TaskboardAdministrationPermissionTest implements PermissionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testName() {
        Permission subject = new TaskboardAdministrationPermission("PERMISSION_NAME");
        assertEquals("PERMISSION_NAME", subject.name());
    }

    @Test
    public void testAcceptsArguments() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Only PermissionContext.empty() is allowed for permission PERMISSION_NAME."));

        Permission subject = new TaskboardAdministrationPermission("PERMISSION_NAME");

        subject.accepts(userWithIsAdmin(true), new PermissionContext("target"));
    }

    @Test
    public void testAccepts() {
        Permission subject = new TaskboardAdministrationPermission("PERMISSION_NAME");

        assertTrue(subject.accepts(userWithIsAdmin(true), PermissionContext.empty()));

        assertFalse(subject.accepts(userWithIsAdmin(false), PermissionContext.empty()));
    }

}
