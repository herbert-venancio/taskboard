package objective.taskboard.repository;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;

import objective.taskboard.data.UserTeam;

public class UserTeamRepositoryMockBuilder {

    private final UserTeamCachedRepository mock = Mockito.mock(UserTeamCachedRepository.class);

    public static UserTeamRepositoryMockBuilder userTeamRepository() {
        return new UserTeamRepositoryMockBuilder();
    }

    public UserTeamRepositoryMockBuilder withUserTeamList(String userName, UserTeam... usersTeams) {
        when(mock.findByUserName(eq(userName))).thenReturn(asList(usersTeams));
        return this;
    }

    public UserTeamCachedRepository build() {
        return mock;
    }

}
