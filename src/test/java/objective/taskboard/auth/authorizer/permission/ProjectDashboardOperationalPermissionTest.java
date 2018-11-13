package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.role;
import static objective.taskboard.auth.authorizer.permission.PermissionTestUtils.assertApplicableTargets;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.LoggedUserDetails.JiraRole;
import objective.taskboard.auth.authorizer.permission.PermissionTestUtils.PermissionTest;

public class ProjectDashboardOperationalPermissionTest implements PermissionTest {

    private ProjectDashboardOperationalPermission permission;

    @Before
    public void setup() {
        LoggedUserDetails loggedUserDetails = loggedUser()
                .withRoles(
                        role("PROJECT-KPI", JiraRole.PROJECT_KPI),
                        role("PROJECT-DEV-KPI", JiraRole.PROJECT_DEVELOPERS),
                        role("PROJECT-DEV-KPI", JiraRole.PROJECT_KPI),
                        role("PROJECT-ADMIN-KPI", JiraRole.PROJECT_ADMINISTRATORS),
                        role("PROJECT-ADMIN-KPI", JiraRole.PROJECT_KPI),
                        role("PROJECT-DEV-ADMIN", JiraRole.PROJECT_DEVELOPERS),
                        role("PROJECT-DEV-ADMIN", JiraRole.PROJECT_ADMINISTRATORS),
                        role("PROJECT-DEV", JiraRole.PROJECT_DEVELOPERS),
                        role("PROJECT-ADMIN", JiraRole.PROJECT_ADMINISTRATORS))
                .build();

        permission = new ProjectDashboardOperationalPermission(loggedUserDetails);
    }

    @Test
    @Override
    public void testName() {
        assertEquals("project.dashboard.operational", permission.name());
    }

    @Test
    @Override
    public void testIsAuthorized() {
        assertFalse(permission.isAuthorizedFor("PROJECT-NO-ROLES"));
        assertFalse(permission.isAuthorizedFor("PROJECT-KPI"));
        assertTrue(permission.isAuthorizedFor("PROJECT-DEV-KPI"));
        assertTrue(permission.isAuthorizedFor("PROJECT-ADMIN-KPI"));
        assertTrue(permission.isAuthorizedFor("PROJECT-DEV-ADMIN"));
        assertTrue(permission.isAuthorizedFor("PROJECT-DEV"));
        assertTrue(permission.isAuthorizedFor("PROJECT-ADMIN"));
    }

    @Test
    public void applicableTargets() {
        assertApplicableTargets(permission, "PROJECT-DEV-KPI", "PROJECT-ADMIN-KPI", "PROJECT-DEV-ADMIN", "PROJECT-DEV", "PROJECT-ADMIN");
    }

}