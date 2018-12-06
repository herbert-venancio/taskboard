package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.role;
import static objective.taskboard.auth.authorizer.permission.PermissionTestUtils.assertApplicableTargets;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.LoggedUserDetails.JiraRole;
import objective.taskboard.auth.authorizer.permission.PermissionTestUtils.PermissionTest;

public class ProjectAdministrationPermissionTest implements PermissionTest {

    private ProjectAdministrationPermission permission;

    @Before
    public void setup() {
        LoggedUserDetails loggedUserDetails = loggedUser()
                .withRoles(
                        role("PROJECT-DEV-KPI", JiraRole.PROJECT_DEVELOPERS),
                        role("PROJECT-DEV-KPI", JiraRole.PROJECT_KPI),
                        role("PROJECT-DEV-ADMIN", JiraRole.PROJECT_DEVELOPERS),
                        role("PROJECT-DEV-ADMIN", JiraRole.PROJECT_ADMINISTRATORS),
                        role("PROJECT-ADMIN", JiraRole.PROJECT_ADMINISTRATORS))
                .build();

        permission = new ProjectAdministrationPermission(loggedUserDetails);
    }

    @Test
    @Override
    public void testName() {
        assertEquals("project.administration",permission.name());
    }

    @Test
    @Override
    public void testIsAuthorized() {
        assertFalse(permission.isAuthorizedFor("PROJECT-NO-ROLES"));
        assertFalse(permission.isAuthorizedFor("PROJECT-DEV-KPI"));
        assertTrue(permission.isAuthorizedFor("PROJECT-DEV-ADMIN"));
        assertTrue(permission.isAuthorizedFor("PROJECT-ADMIN"));
    }

    @Test
    public void applicableTargets() {
        assertApplicableTargets(permission, "PROJECT-DEV-ADMIN", "PROJECT-ADMIN");
    }

}
