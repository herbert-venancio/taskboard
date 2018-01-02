package objective.taskboard.data.converter;

import objective.taskboard.data.User;
import objective.taskboard.data.UserTeam;
import objective.taskboard.jira.data.JiraUser;
import objective.taskboard.repository.UserTeamCachedRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willReturn;

@RunWith(MockitoJUnitRunner.class)
public class JiraUserToUserConverterTest {

    private static final String USER_LOGIN = "username";
    private static final String USER_NAME = "User Name";
    private static final String USER_EMAIL = "user@mail.com";

    private JiraUser jiraUser;

    @Mock
    private UserTeamCachedRepository userTeamRepo;

    @InjectMocks
    private JiraUserToUserConverter subject;

    @Before
    public void setup() {
        Map<String, URI> avatarUrls = Collections.singletonMap("48x48",
                URI.create("http://www.gravatar.com/avatar/c2b78b1ds52b346ff4528044ee123cc74?d=mm&s=48"));
        jiraUser = new JiraUser(USER_LOGIN, USER_NAME, USER_EMAIL, avatarUrls);
    }

    @Test
    public void givenNoTeams_shouldAssumeCustomer() {
        // given
        willReturn(emptyList()).given(userTeamRepo).findByUserName(USER_LOGIN);

        // when
        User user = subject.convert(jiraUser);

        // then
        assertThat(user.isCustomer).isTrue();
    }

    @Test
    public void givenOnlyCustomerTeams_shouldBeCustomer() {
        // given
        willReturn(teams("PROJ1_CUSTOMER", "PROJ2_CUSTOMER")).given(userTeamRepo).findByUserName(USER_LOGIN);

        // when
        User user = subject.convert(jiraUser);

        // then
        assertThat(user.isCustomer).isTrue();
    }

    @Test
    public void givenOnlyDevTeams_shouldNotBeCustomer() {
        // given
        willReturn(teams("PROJ1_DEV", "PROJ2_DEV")).given(userTeamRepo).findByUserName(USER_LOGIN);

        // when
        User user = subject.convert(jiraUser);

        // then
        assertThat(user.isCustomer).isFalse();
    }

    @Test
    public void givenMixedTeams_shouldAssumeCustomer() {
        // given
        willReturn(teams("PROJ1_DEV", "PROJ2_CUSTOMER")).given(userTeamRepo).findByUserName(USER_LOGIN);

        // when
        User user = subject.convert(jiraUser);

        // then
        assertThat(user.isCustomer).isTrue();
    }

    private List<UserTeam> teams(String... teamNames) {
        return Arrays.stream(teamNames)
                .map(teamName -> new UserTeam(USER_LOGIN, teamName))
                .collect(Collectors.toList());
    }
}
