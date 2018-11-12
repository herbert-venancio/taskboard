package objective.taskboard.auth.authorizer.permission;

import static java.lang.String.join;
import static objective.taskboard.auth.authorizer.permission.PermissionBuilder.targetlessPermission;
import static objective.taskboard.auth.authorizer.permission.PermissionBuilder.targettedPermission;
import static objective.taskboard.auth.authorizer.permission.PermissionUtils.applicableTargetsInAnyPermission;
import static objective.taskboard.auth.authorizer.permission.PermissionUtils.isAuthorizedForAnyPermission;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

public class PermissionUtilsTest {

    @Test
    public void applicableTargetsInAnyPermission_shouldReturnAllApplicableTargetsDistincted() {
        TargettedPermission[] permissions = new TargettedPermission[] {
                targettedPermission().applicableTo("TARGET_A", "TARGET_B", "TARGET_C"),
                targettedPermission().applicableTo("TARGET_A"),
                targettedPermission().applicableTo("TARGET_A", "TARGET_C")
        };
        assertApplicableTargets(permissions, "TARGET_A", "TARGET_B", "TARGET_C");
    }

    @Test
    public void isAuthorizedForAnyPermission_ifAllPermissionsAreNotAuthorized_shouldReturnFalse() {
        TargetlessPermission[] permissions = new TargetlessPermission[] {
                targetlessPermission().notAuthorized(),
                targetlessPermission().notAuthorized()
        };
        assertFalse(isAuthorizedForAnyPermission(permissions));
    }

    @Test
    public void isAuthorizedForAnyPermission_ifAtLeastOnePermissionIsAuthorized_shouldReturnTrue() {
        TargetlessPermission[] permissions = new TargetlessPermission[] {
                targetlessPermission().notAuthorized(),
                targetlessPermission().authorized(),
                targetlessPermission().notAuthorized()
        };
        assertTrue(isAuthorizedForAnyPermission(permissions));
    }

    @Test
    public void isAuthorizedForAnyPermission_targetted_ifAllPermissionsAreNotAuthorized_shouldReturnFalse() {
        Permission[] permissions = new Permission[] {
                targetlessPermission().notAuthorized(),
                targettedPermission().notApplicableToAnyTarget()
        };
        assertFalse(isAuthorizedForAnyPermission("TARGET", permissions));
    }

    @Test
    public void isAuthorizedForAnyPermission_targetted_ifAtLeastOnePermissionIsAuthorized_shouldReturnTrue() {
        Permission[] permissions = new Permission[] {
                targetlessPermission().notAuthorized(),
                targettedPermission().applicableTo("TARGET")
        };
        assertTrue(isAuthorizedForAnyPermission("TARGET", permissions));

        permissions = new Permission[] {
                targetlessPermission().authorized(),
                targettedPermission().notApplicableToAnyTarget()
        };
        assertTrue(isAuthorizedForAnyPermission("TARGET", permissions));
    }


    private void assertApplicableTargets(TargettedPermission[] permissions, String... expectedTargets) {
        List<String> actual = applicableTargetsInAnyPermission(permissions);
        assertEquals(join("\n", expectedTargets), join("\n", actual));
    }

}