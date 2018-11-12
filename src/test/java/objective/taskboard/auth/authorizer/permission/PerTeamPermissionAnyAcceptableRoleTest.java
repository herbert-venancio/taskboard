package objective.taskboard.auth.authorizer.permission;

import static objective.taskboard.auth.LoggedUserDetailsMockBuilder.loggedUser;
import static objective.taskboard.data.UserTeam.UserTeamRole.MANAGER;
import static objective.taskboard.data.UserTeam.UserTeamRole.MEMBER;
import static objective.taskboard.repository.UserTeamRepositoryMockBuilder.userTeamRepository;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.auth.authorizer.permission.PermissionTestUtils.PermissionTest;
import objective.taskboard.data.UserTeam;
import objective.taskboard.repository.UserTeamCachedRepository;

public class PerTeamPermissionAnyAcceptableRoleTest implements PermissionTest {

    private LoggedUserDetails loggedUserDetails = mock(LoggedUserDetails.class);

    private UserTeamCachedRepository userTeamCachedRepository = mock(UserTeamCachedRepository.class);

    @Test
    @Override
    public void testName() {
        Permission subject = new PerTeamPermissionAnyAcceptableRole("PERMISSION_NAME", loggedUserDetails, userTeamRepository().build(), MANAGER);
        assertEquals("PERMISSION_NAME", subject.name());
    }

    @Test
    @Override
    public void testIsAuthorized() {
        UserTeam userTeam = mock(UserTeam.class);
        Optional<UserTeam> userTeamOpt = Optional.of(userTeam);

        when(userTeamCachedRepository.findByUsernameTeamAndRoles("USER", "TEAM1", MANAGER, MEMBER)).thenReturn(userTeamOpt);
        when(userTeamCachedRepository.findByUsernameTeamAndRoles("USER", "TEAM2", MANAGER, MEMBER)).thenReturn(userTeamOpt);
        when(userTeamCachedRepository.findByUsernameTeamAndRoles("USER", "TEAM3", MANAGER, MEMBER)).thenReturn(Optional.empty());

        LoggedUserDetails loggedUserDetails = loggedUser().withName("USER").build();

        PerTeamPermissionAnyAcceptableRole subject = new PerTeamPermissionAnyAcceptableRole("PERMISSION_NAME", loggedUserDetails, userTeamCachedRepository, MANAGER, MEMBER);

        assertTrue(subject.isAuthorizedFor("TEAM1"));

        assertTrue(subject.isAuthorizedFor("TEAM2"));

        assertFalse(subject.isAuthorizedFor("TEAM3"));
    }

}
