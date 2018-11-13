package objective.taskboard.auth.authorizer.permission;

import static org.junit.Assert.assertEquals;

import objective.taskboard.auth.LoggedUserDetails.JiraRole;
import objective.taskboard.data.UserTeam;
import objective.taskboard.data.UserTeam.UserTeamRole;

public class PermissionTestUtils {

    public static JiraRole role(String roleName, String projectKey) {
        return new JiraRole(1L, roleName, projectKey);
    }

    public static UserTeam userTeam(String memberName, String teamName, UserTeamRole role) {
        return new UserTeam(memberName, teamName, role);
    }

    public static void assertApplicableTargets(TargettedPermission permission, String... expectedTargets) {
        assertEquals(
                String.join("\n", permission.applicableTargets()),
                String.join("\n", expectedTargets));
    }

    public interface PermissionTest {
        void testName();
        void testIsAuthorized();
    }

}
