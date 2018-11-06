package objective.taskboard.auth.authorizer;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.AnyProjectPermission;
import objective.taskboard.auth.authorizer.permission.PerProjectPermission;
import objective.taskboard.auth.authorizer.permission.Permission;
import objective.taskboard.auth.authorizer.permission.PermissionContext;
import objective.taskboard.auth.authorizer.permission.TaskboardAdministrationPermission;

public class PermissionRepositoryTest {

    private List<Permission> permissions = new ArrayList<>();
    private List<PerProjectPermission> perProjectPermissions = new ArrayList<>();
    private PermissionRepository subject = new PermissionRepository(permissions, perProjectPermissions);
    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void findByName_givenInvalidPermissionName_throwException() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Permission INVALID_PERMISSION is invalid."));

        subject.findByName("INVALID_PERMISSION");
    }

    @Test
    public void specificProjectPermission_givenEmptyContext_shouldThrowError() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Empty PermissionContext isn't allowed for permission permission.name."));

        Permission permission = new PerProjectPermission("permission.name", loggedUserDetails, "any_role_1", "any_role_1");
        permission.accepts(PermissionContext.empty());
    }

    @Test
    public void anyProjectPermission_givenNonEmptyContext_shouldThrowError() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Only PermissionContext.empty() is allowed for permission permission.name."));

        Permission permission = new AnyProjectPermission("permission.name", loggedUserDetails, "any_role_1", "any_role_1");
        permission.accepts(new PermissionContext("target"));
    }

    @Test
    public void taskboardPermission_givenNonEmptyContext_shouldThrowError() {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(is("Only PermissionContext.empty() is allowed for permission taskboard.administration."));

        Permission permission = new TaskboardAdministrationPermission(loggedUserDetails);
        permission.accepts(new PermissionContext("target"));
    }

}
