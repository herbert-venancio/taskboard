package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
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

public class TaskboardAdministrationPermissionTest implements PermissionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    @Test
    public void testName() {
        Permission subject = new TaskboardAdministrationPermission(loggedUserDetails);
        assertEquals("taskboard.administration", subject.name());
    }

    @Test
    public void testIsAuthorizedArguments() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Only PermissionContext.empty() is allowed for permission taskboard.administration."));

        Permission subject = new TaskboardAdministrationPermission(loggedUser().withIsAdmin(true).build());

        subject.isAuthorized(new PermissionContext("target"));
    }

    @Test
    public void testIsAuthorized() {
        Permission subject = new TaskboardAdministrationPermission(loggedUser().withIsAdmin(true).build());
        assertTrue(subject.isAuthorized(PermissionContext.empty()));

        subject = new TaskboardAdministrationPermission(loggedUser().withIsAdmin(false).build());
        assertFalse(subject.isAuthorized(PermissionContext.empty()));
    }

}
