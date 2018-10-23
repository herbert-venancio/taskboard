package objective.taskboard.auth.authorizer.permission;

import objective.taskboard.auth.LoggedUserDetails.JiraRole;
import objective.taskboard.data.UserTeam;
import objective.taskboard.data.UserTeam.UserTeamRole;

public class PermissionTestUtils {

    public static JiraRole role(String roleName, String projectKey) {
        return new JiraRole(1L, roleName, projectKey);
    }

    public static UserTeam userTeam(String memberName, String teamName, UserTeamRole role) {
        UserTeam userTeam = new UserTeam(memberName, teamName);
        userTeam.setRole(role);
        return userTeam;
    }

    public static interface PermissionTest {
        void testName();
        void testAcceptsArguments();
        void testAccepts();
    }

}
