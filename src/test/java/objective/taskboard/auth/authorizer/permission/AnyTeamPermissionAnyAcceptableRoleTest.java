package objective.taskboard.auth.authorizer.permission;

import static java.util.Collections.emptyList;
import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
import static objective.taskboard.data.UserTeam.UserTeamRole.MANAGER;
import static objective.taskboard.data.UserTeam.UserTeamRole.MEMBER;
import static objective.taskboard.repository.UserTeamRepositoryMockBuilder.userTeamRepository;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.PermissionTestUtils.PermissionTest;
import objective.taskboard.data.UserTeam;
import objective.taskboard.repository.UserTeamCachedRepository;

public class AnyTeamPermissionAnyAcceptableRoleTest implements PermissionTest {

    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    @Test
    @Override
    public void testName() {
        Permission subject = new AnyTeamPermissionAnyAcceptableRole("PERMISSION_NAME", loggedUserDetails, userTeamRepository().build(), MANAGER);
        assertEquals("PERMISSION_NAME", subject.name());
    }

    @Test
    @Override
    public void testIsAuthorized() {
        UserTeamCachedRepository userTeamCachedRepository = mock(UserTeamCachedRepository.class);
        UserTeam userTeam = mock(UserTeam.class);

        when(userTeamCachedRepository.findByUsernameAndRoles("USER1", MANAGER, MEMBER)).thenReturn(asList(userTeam));
        when(userTeamCachedRepository.findByUsernameAndRoles("USER2", MANAGER, MEMBER)).thenReturn(emptyList());

        LoggedUserDetails userWithPermission = loggedUser().withName("USER1").build();
        AnyTeamPermissionAnyAcceptableRole subject = new AnyTeamPermissionAnyAcceptableRole("PERMISSION_NAME", userWithPermission, userTeamCachedRepository, MANAGER, MEMBER);
        assertTrue(subject.isAuthorized());

        LoggedUserDetails userWithoutPermission = loggedUser().withName("USER2").build();
        subject = new AnyTeamPermissionAnyAcceptableRole("PERMISSION_NAME", userWithoutPermission, userTeamCachedRepository, MANAGER, MEMBER);
        assertFalse(subject.isAuthorized());
    }

}
