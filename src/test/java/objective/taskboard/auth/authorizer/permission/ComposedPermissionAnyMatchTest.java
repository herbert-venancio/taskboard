package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.authorizer.permission.PermissionBuilder.permission;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;

public class ComposedPermissionAnyMatchTest {

    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    @Test
    public void isAuthorizedFor_everyPermissionIsNotAuthorized_shouldReturnFalse() {
        ComposedPermissionAnyMatch permission = new ComposedPermissionAnyMatchImpl("ANY", loggedUserDetails,
                permission().withIsAuthorized(false).asTargetted(),
                permission().withIsAuthorized(false).asTargetless());

        assertFalse(permission.isAuthorizedFor("target"));
    }

    @Test
    public void isAuthorizedFor_onePermissionIsAuthorized_shouldReturnTrue() {
        ComposedPermissionAnyMatch permission = new ComposedPermissionAnyMatchImpl("ANY", loggedUserDetails,
                permission().withIsAuthorized(false).asTargetted(),
                permission().withIsAuthorized(true).asTargetless(),
                permission().withIsAuthorized(false).asTargetted());
        
        assertTrue(permission.isAuthorizedFor("target"));
    }

    private static class ComposedPermissionAnyMatchImpl extends ComposedPermissionAnyMatch {

        public ComposedPermissionAnyMatchImpl(String name, LoggedUserDetails loggedUserDetails, Permission... permissions) {
            super(name, loggedUserDetails, permissions);
        }
    }

}
