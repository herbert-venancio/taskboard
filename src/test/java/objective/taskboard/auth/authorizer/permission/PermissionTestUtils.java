package objective.taskboard.auth.authorizer.permission;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.LoggedUserDetails.JiraRole;

public class PermissionTestUtils {

    public static JiraRole role(String roleName, String projectKey) {
        return new JiraRole(1L, roleName, projectKey);
    }

    public static LoggedUserDetails userWithIsAdmin(boolean isAdmin) {
        final LoggedUserDetails userDetails = mock(LoggedUserDetails.class);
        when(userDetails.isAdmin()).thenReturn(isAdmin);
        return userDetails;
    }

    public static LoggedUserDetails userWithRoles(JiraRole... roles) {
        final LoggedUserDetails userDetails = mock(LoggedUserDetails.class);
        when(userDetails.getJiraRoles()).thenReturn(asList(roles));
        return userDetails;
    }

    public static interface PermissionTest {
        void testName();
        void testAcceptsArguments();
        void testAccepts();
    }

}
