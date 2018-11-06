package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
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
        Permission subject = new TaskboardAdministrationPermission();
        assertEquals("taskboard.administration", subject.name());
    }

    @Test
    public void testAcceptsArguments() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Only PermissionContext.empty() is allowed for permission taskboard.administration."));

        Permission subject = new TaskboardAdministrationPermission();

        subject.accepts(loggedUser().withIsAdmin(true).build(), new PermissionContext("target"));
    }

    @Test
    public void testAccepts() {
        Permission subject = new TaskboardAdministrationPermission();

        assertTrue(subject.accepts(loggedUser().withIsAdmin(true).build(), PermissionContext.empty()));

        assertFalse(subject.accepts(loggedUser().withIsAdmin(false).build(), PermissionContext.empty()));
    }

}
