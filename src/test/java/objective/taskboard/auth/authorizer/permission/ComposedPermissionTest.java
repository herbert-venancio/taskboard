package objective.taskboard.auth.authorizer.permission;

import static java.lang.String.join;
import static objective.taskboard.auth.authorizer.permission.PermissionBuilder.permission;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;

public class ComposedPermissionTest {

    @Test
    public void applicableTargets_withoutTargettedPermission_shouldReturnEmpty() {
        Permission permission = new ComposedPermissionImpl("ANY", 
                permission().asTargetless(),
                permission().asTargetless());

        Optional<List<String>> applicableTargets = permission.applicableTargets(null);
        assertFalse(applicableTargets.isPresent());
    }

    @Test
    public void applicableTargets_withTargettedPermission_shouldReturnAllApplicableTargetsDistincted() {
        Permission permission = new ComposedPermissionImpl("ANY", 
                permission().withApplicableTargets("TARGET_A", "TARGET_B").asTargetted(),
                permission().withApplicableTargets("TARGET_A", "TARGET_C").asTargetted(),
                permission().asTargetless());

        List<String> applicableTargets = permission.applicableTargets(null).orElseThrow(IllegalStateException::new);

        assertEquals("TARGET_A, TARGET_B, TARGET_C", join(", ", applicableTargets));
    }

    private static class ComposedPermissionImpl extends ComposedPermission {
        public ComposedPermissionImpl(String name, Permission... permissions) {
            super(name, permissions);
        }
        @Override
        public boolean accepts(LoggedUserDetails userDetails, PermissionContext permissionContext) {
            return false;
        }
    }
}
