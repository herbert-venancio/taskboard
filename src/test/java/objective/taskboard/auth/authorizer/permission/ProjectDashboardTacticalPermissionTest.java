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

public class ProjectDashboardTacticalPermissionTest implements PermissionTest {

    private ProjectDashboardTacticalPermission permission;

    @Before
    public void setup() {
        LoggedUserDetails loggedUserDetails = loggedUser()
                .withRoles(
                        role("PROJECT-KPI", JiraRole.PROJECT_KPI),
                        role("PROJECT-DEV", JiraRole.PROJECT_DEVELOPERS),
                        role("PROJECT-ADMIN", JiraRole.PROJECT_ADMINISTRATORS),
                        role("PROJECT-DEV-ADMIN", JiraRole.PROJECT_DEVELOPERS),
                        role("PROJECT-DEV-ADMIN", JiraRole.PROJECT_ADMINISTRATORS),
                        role("PROJECT-OTHER-ROLE", "OtherRole"))
                .build();

        permission = new ProjectDashboardTacticalPermission(loggedUserDetails);
    }

    @Test
    @Override
    public void testName() {
        assertEquals("project.dashboard.tactical", permission.name());
    }

    @Test
    @Override
    public void testIsAuthorized() {
        assertFalse(permission.isAuthorizedFor("PROJECT-NO-ROLES"));
        assertTrue(permission.isAuthorizedFor("PROJECT-KPI"));
        assertTrue(permission.isAuthorizedFor("PROJECT-DEV"));
        assertTrue(permission.isAuthorizedFor("PROJECT-ADMIN"));
        assertFalse(permission.isAuthorizedFor("PROJECT-OTHER-ROLE"));
    }

    @Test
    public void applicableTargets() {
        assertApplicableTargets(permission, "PROJECT-KPI", "PROJECT-DEV", "PROJECT-ADMIN", "PROJECT-DEV-ADMIN");
    }

}
