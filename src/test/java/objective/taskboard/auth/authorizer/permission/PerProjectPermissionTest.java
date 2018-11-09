package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
import static objective.taskboard.auth.authorizer.permission.PermissionTestUtils.role;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.PermissionTestUtils.PermissionTest;

public class PerProjectPermissionTest implements PermissionTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    @Test
    @Override
    public void testName() {
        Permission subject = new PerProjectPermission("PERMISSION_NAME", loggedUserDetails, "role1", "role2");
        assertEquals("PERMISSION_NAME", subject.name());
    }

    @Test
    @Override
    public void testIsAuthorized() {
        LoggedUserDetails userWithPermission = loggedUser().withRoles(
                role("role1", "PROJ1"),
                role("role3", "PROJ1")
                ).build();
        PerProjectPermission subject = new PerProjectPermission("PERMISSION_NAME", userWithPermission, "role1", "role2");
        assertTrue(subject.isAuthorizedFor("PROJ1"));

        LoggedUserDetails userWithoutPermission = loggedUser().withRoles(
                role("role1", "PROJ2"),
                role("role3", "PROJ2")
                ).build();
        subject = new PerProjectPermission("PERMISSION_NAME", userWithoutPermission, "role1", "role2");
        assertFalse(subject.isAuthorizedFor("PROJ1"));
    }

}
