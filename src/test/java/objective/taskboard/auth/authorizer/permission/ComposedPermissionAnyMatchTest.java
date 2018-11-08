package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.authorizer.permission.PermissionBuilder.permission;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ComposedPermissionAnyMatchTest {

    @Test
    public void accepts_everyPermissionDontAccept_shouldReturnFalse() {
        Permission permission = new ComposedPermissionAnyMatch("ANY", 
                permission().withAccepts(false).asTargetted(),
                permission().withAccepts(false).asTargetless());

        assertFalse(permission.accepts(null, null));
    }

    @Test
    public void accepts_onePermissionAccepts_shouldReturnTrue() {
        Permission permission = new ComposedPermissionAnyMatch("ANY", 
                permission().withAccepts(false).asTargetted(),
                permission().withAccepts(true).asTargetless(),
                permission().withAccepts(false).asTargetted());
        
        assertTrue(permission.accepts(null, null));
    }

}
