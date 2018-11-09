package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
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
    @Override
    public void testName() {
        Permission subject = new TaskboardAdministrationPermission(loggedUserDetails);
        assertEquals("taskboard.administration", subject.name());
    }

    @Test
    @Override
    public void testIsAuthorized() {
        TaskboardAdministrationPermission subject = new TaskboardAdministrationPermission(loggedUser().withIsAdmin(true).build());
        assertTrue(subject.isAuthorized());

        subject = new TaskboardAdministrationPermission(loggedUser().withIsAdmin(false).build());
        assertFalse(subject.isAuthorized());
    }

}
