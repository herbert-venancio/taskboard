package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
import static objective.taskboard.repository.UserTeamRepositoryMockBuilder.userTeamRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.PermissionTestUtils.PermissionTest;

public class TaskboardAdministrationPermissionTest implements PermissionTest {

    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    @Test
    @Override
    public void testName() {
        Permission subject = new TaskboardAdministrationPermission(loggedUserDetails, userTeamRepository().build());
        assertEquals("taskboard.administration", subject.name());
    }

    @Test
    @Override
    public void testIsAuthorized() {
        TaskboardAdministrationPermission subject = new TaskboardAdministrationPermission(loggedUser().withIsAdmin(true).build(), userTeamRepository().build());
        assertTrue(subject.isAuthorized());

        subject = new TaskboardAdministrationPermission(loggedUser().withIsAdmin(false).build(), userTeamRepository().build());
        assertFalse(subject.isAuthorized());
    }

}
