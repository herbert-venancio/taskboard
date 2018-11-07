package objective.taskboard.auth.authorizer.permission;

import static java.lang.String.join;
import static objective.taskboard.auth.authorizer.permission.PermissionBuilder.permission;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;

public class ComposedPermissionTest {

    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    @Test
    public void applicableTargets_withoutTargettedPermission_shouldReturnEmpty() {
        Permission permission = new ComposedPermissionImpl("ANY", loggedUserDetails,
                permission().asTargetless(),
                permission().asTargetless());

        Optional<List<String>> applicableTargets = permission.applicableTargets();
        assertFalse(applicableTargets.isPresent());
    }

    @Test
    public void applicableTargets_withTargettedPermission_shouldReturnAllApplicableTargetsDistincted() {
        Permission permission = new ComposedPermissionImpl("ANY", loggedUserDetails,
                permission().withApplicableTargets("TARGET_A", "TARGET_B").asTargetted(),
                permission().withApplicableTargets("TARGET_A", "TARGET_C").asTargetted(),
                permission().asTargetless());

        List<String> applicableTargets = permission.applicableTargets().orElseThrow(IllegalStateException::new);

        assertEquals("TARGET_A, TARGET_B, TARGET_C", join(", ", applicableTargets));
    }

    private static class ComposedPermissionImpl extends ComposedPermission {
        public ComposedPermissionImpl(String name, LoggedUserDetails loggedUserDetails, Permission... permissions) {
            super(name, loggedUserDetails, permissions);
        }
        @Override
        public boolean isAuthorized(PermissionContext permissionContext) {
            return false;
        }
    }
}
