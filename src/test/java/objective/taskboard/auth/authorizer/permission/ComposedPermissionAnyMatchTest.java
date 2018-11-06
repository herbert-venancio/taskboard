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
    public void accepts_everyPermissionDontAccept_shouldReturnFalse() {
        Permission permission = new ComposedPermissionAnyMatchImpl("ANY", loggedUserDetails,
                permission().withAccepts(false).asTargetted(),
                permission().withAccepts(false).asTargetless());

        assertFalse(permission.accepts(null));
    }

    @Test
    public void accepts_onePermissionAccepts_shouldReturnTrue() {
        Permission permission = new ComposedPermissionAnyMatchImpl("ANY", loggedUserDetails,
                permission().withAccepts(false).asTargetted(),
                permission().withAccepts(true).asTargetless(),
                permission().withAccepts(false).asTargetted());
        
        assertTrue(permission.accepts(null));
    }

    private static class ComposedPermissionAnyMatchImpl extends ComposedPermissionAnyMatch {

        public ComposedPermissionAnyMatchImpl(String name, LoggedUserDetails loggedUserDetails, Permission... permissions) {
            super(name, loggedUserDetails, permissions);
        }
    }

}
