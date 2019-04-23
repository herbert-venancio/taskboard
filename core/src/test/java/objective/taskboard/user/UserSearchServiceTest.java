package objective.taskboard.user;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.auth.authorizer.permission.UserVisibilityPermission;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.data.JiraUser.UserDetails;

@RunWith(MockitoJUnitRunner.class)
public class UserSearchServiceTest {

    private List<UserDetails> userResultList;

    private UserSearchService subject;

    @Test
    public void givenQuery_returnVisibleUsers() {
        given()
            .usersFoundBySearchingByAre("Ja",
                    "Jaidyn",
                    "Jaime",
                    "Jaimeson",
                    "Jaiquan")
            .userWithVisibilityPermissionToSee(
                    "Jane",
                    "Jaime",
                    "Jaiquan")

        .whenExecute(() -> userResultList = subject.getUsersVisibleToLoggedInUserByQuery("Ja"))

        .thenAssertVisibleUsersEquals(
                    "Jaime",
                    "Jaiquan");
    }

    private UserSearchServiceTestDSL given() {
        return new UserSearchServiceTestDSL();
    }

    private class UserSearchServiceTestDSL {

        private final JiraService jiraService = mock(JiraService.class);
        private final UserVisibilityPermission userVisibilityPermission = mock(UserVisibilityPermission.class);

        public UserSearchServiceTestDSL() {
            subject = new UserSearchService(jiraService, userVisibilityPermission);
        }

        public UserSearchServiceTestDSL usersFoundBySearchingByAre(String query, String... usersFound) {
            when(jiraService.findUsers(query)).thenReturn(userDetailListByNames(usersFound));
            return this;
        }

        public UserSearchServiceTestDSL userWithVisibilityPermissionToSee(String... visibleUsers) {
            stream(visibleUsers).forEach(user ->
                when(userVisibilityPermission.isAuthorizedFor(user)).thenReturn(true)
                );
            return this;
        }

        private List<UserDetails> userDetailListByNames(String... names) {
            return stream(names)
                .map(userName -> {
                    UserDetails userDetails = new UserDetails();
                    userDetails.name = userName;
                    return userDetails;
                })
                .collect(toList());
        }

        public UserSearchServiceTestDSL whenExecute(Runnable runnable) {
            runnable.run();
            return this;
        }

        public UserSearchServiceTestDSL thenAssertVisibleUsersEquals(String... expectedUsers) {
            assertThat(
                    userResultList.stream().map(user -> user.name).collect(toList()),
                    containsInAnyOrder(expectedUsers));
            return this;
        }
    }

}
