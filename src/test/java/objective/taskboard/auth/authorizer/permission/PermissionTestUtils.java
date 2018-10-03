package objective.taskboard.auth.authorizer.permission;

import objective.taskboard.auth.LoggedUserDetails.JiraRole;

public class PermissionTestUtils {

    public static JiraRole role(String roleName, String projectKey) {
        return new JiraRole(1L, roleName, projectKey);
    }

    public static interface PermissionTest {
        void testName();
        void testAcceptsArguments();
        void testAccepts();
    }

}
