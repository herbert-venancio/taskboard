package objective.taskboard.repository;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static objective.taskboard.data.UserTeam.UserTeamRole.MANAGER;
import static objective.taskboard.data.UserTeam.UserTeamRole.MEMBER;
import static objective.taskboard.data.UserTeam.UserTeamRole.VIEWER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.data.UserTeam;
import objective.taskboard.data.UserTeam.UserTeamRole;

@RunWith(MockitoJUnitRunner.class)
public class UserTeamCachedRepositoryTest {

    @Mock
    private UserTeamRepository userTeamRepository;

    @InjectMocks
    private UserTeamCachedRepository repository;

    @Test
    public void findByUsernameAndRoles() {
        mockUserTeamRepositoryToReturn(
                userTeam("user.one", "red", MANAGER),
                userTeam("user.one", "green", MEMBER),
                userTeam("user.one", "blue", VIEWER),

                userTeam("user.two", "red", MEMBER),
                userTeam("user.two", "green", MANAGER),
                userTeam("user.two", "blue", VIEWER)
        );

        assertUserTeams(repository.findByUsernameAndRoles("user.one", MANAGER),
                "user.one|red|MANAGER");
        assertUserTeams(repository.findByUsernameAndRoles("user.one", MEMBER),
                "user.one|green|MEMBER");
        assertUserTeams(repository.findByUsernameAndRoles("user.one", VIEWER),
                "user.one|blue|VIEWER");
        assertUserTeams(repository.findByUsernameAndRoles("user.one", MANAGER, MEMBER),
                "user.one|red|MANAGER",
                "user.one|green|MEMBER");

        assertUserTeams(repository.findByUsernameAndRoles("user.two", MEMBER, VIEWER),
                "user.two|red|MEMBER",
                "user.two|blue|VIEWER");

        assertNoResult(repository.findByUsernameAndRoles("user.three", MANAGER, MEMBER, VIEWER));
    }

    @Test
    public void findByUsernameTeamAndRoles() {
        mockUserTeamRepositoryToReturn(
                userTeam("user.one", "red", MANAGER),
                userTeam("user.one", "green", MEMBER),
                userTeam("user.one", "blue", VIEWER),

                userTeam("user.two", "red", MEMBER),
                userTeam("user.two", "green", MANAGER),
                userTeam("user.two", "blue", VIEWER)
        );

        assertUserTeam(repository.findByUsernameTeamAndRoles("user.one", "red", MANAGER), "user.one|red|MANAGER");
        assertUserTeam(repository.findByUsernameTeamAndRoles("user.one", "green", MEMBER), "user.one|green|MEMBER");
        assertUserTeam(repository.findByUsernameTeamAndRoles("user.one", "blue", VIEWER), "user.one|blue|VIEWER");

        assertNoResult(repository.findByUsernameTeamAndRoles("user.one", "red", MEMBER, VIEWER));
        assertNoResult(repository.findByUsernameTeamAndRoles("user.one", "blue", MEMBER));
        assertNoResult(repository.findByUsernameTeamAndRoles("user.one", "orange", MANAGER, MEMBER, VIEWER));

        assertUserTeam(repository.findByUsernameTeamAndRoles("user.two", "red", MEMBER), "user.two|red|MEMBER");
        assertUserTeam(repository.findByUsernameTeamAndRoles("user.two", "green", MANAGER), "user.two|green|MANAGER");
        assertUserTeam(repository.findByUsernameTeamAndRoles("user.two", "blue", VIEWER), "user.two|blue|VIEWER");

        assertNoResult(repository.findByUsernameTeamAndRoles("user.two", "red", MANAGER, VIEWER));
        assertNoResult(repository.findByUsernameTeamAndRoles("user.two", "blue", MANAGER));
        assertNoResult(repository.findByUsernameTeamAndRoles("user.two", "orange", MANAGER, MEMBER, VIEWER));
    }

    private void mockUserTeamRepositoryToReturn(UserTeam... userTeams) {
        when(userTeamRepository.findAll()).thenReturn(asList(userTeams));
    }

    private UserTeam userTeam(String username, String team, UserTeamRole role) {
        UserTeam mock = mock(UserTeam.class);
        when(mock.getTeam()).thenReturn(team);
        when(mock.getUserName()).thenReturn(username);
        when(mock.getRole()).thenReturn(role);
        return mock;
    }

    private void assertNoResult(Optional<UserTeam> userTeam) {
        assertFalse(userTeam.isPresent());
    }

    private void assertUserTeam(Optional<UserTeam> actual, String expectedRepresentation) {
        assertTrue(actual.isPresent());
        assertEquals(expectedRepresentation, getRepresentation(actual.get()));
    }

    private void assertNoResult(List<UserTeam> userTeam) {
        assertTrue(userTeam.isEmpty());
    }

    private void assertUserTeams(List<UserTeam> actual, String... expectedRepresentations) {
        String actualRepresentation = actual.stream()
                .map(this::getRepresentation)
                .collect(joining("\n"));

        assertEquals(join("\n",expectedRepresentations), actualRepresentation);
    }

    private String getRepresentation(UserTeam userTeam) {
        return userTeam.getUserName() + "|" + userTeam.getTeam() + "|" + userTeam.getRole().name();
    }

}