package objective.taskboard.auth.authorizer.permission;

import static java.lang.String.join;
import static objective.taskboard.auth.authorizer.permission.PermissionBuilder.permission;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;

public class ComposedPermissionTest {

    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    @Test
    public void applicableTargets_withoutTargettedPermission_shouldReturnEmpty() {
        ComposedPermission permission = new ComposedPermissionImpl("ANY", loggedUserDetails,
                permission().asTargetless(),
                permission().asTargetless());

        List<String> applicableTargets = permission.applicableTargets();
        assertTrue(applicableTargets.isEmpty());
    }

    @Test
    public void applicableTargets_withTargettedPermission_shouldReturnAllApplicableTargetsDistincted() {
        ComposedPermission permission = new ComposedPermissionImpl("ANY", loggedUserDetails,
                permission().withApplicableTargets("TARGET_A", "TARGET_B").asTargetted(),
                permission().withApplicableTargets("TARGET_A", "TARGET_C").asTargetted(),
                permission().asTargetless());

        List<String> applicableTargets = permission.applicableTargets();

        assertEquals("TARGET_A, TARGET_B, TARGET_C", join(", ", applicableTargets));
    }

    private static class ComposedPermissionImpl extends ComposedPermission {
        public ComposedPermissionImpl(String name, LoggedUserDetails loggedUserDetails, Permission... permissions) {
            super(name, loggedUserDetails, permissions);
        }
        @Override
        protected boolean isAuthorized(LoggedUserDetails loggedUserDetails, String target) {
            return false;
        }
    }
}
