package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.role;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.LoggedUserDetails.JiraRole;

public class ProjectDashboardOperationalPermissionTest {

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
    public void testName() {
        assertEquals("project.dashboard.operational", permission.name());
    }

    @Test
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
        String[] expectedTargets = { "PROJECT-DEV-KPI", "PROJECT-ADMIN-KPI", "PROJECT-DEV-ADMIN", "PROJECT-DEV", "PROJECT-ADMIN" };
        assertEquals(String.join("\n", permission.applicableTargets()), String.join("\n", expectedTargets));
    }

}