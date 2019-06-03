package objective.taskboard.user;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.common.collect.Maps;

import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.Lousa;
import objective.taskboard.testUtils.FixedClock;

@RunWith(MockitoJUnitRunner.class)
public class TaskboardUserServiceTest {

    private TaskboardUser getUserResult;

    private TaskboardUserService subject;

    @Test
    public void givenExistentUser_whenGetUser_thenDontCreateIt() {
        given()
            .userExists("mary")

        .whenExecute(() -> {
            getUserResult = subject.getTaskboardUser("mary");
        })

        .thenAssert("mary")
            .wasGetFromRepository()
            .wasNotCreated();
    }

    @Test
    public void givenInexistentUser_whenGetUser_thenCreateItAsAdmin() {
        given()
            .usernameToConnectWithJiraIs("john")
            .userDoesntExists("john")

        .whenExecute(() -> {
            getUserResult = subject.getTaskboardUser("john");
        })

        .thenAssert("john")
            .wasntGetFromRepository()
            .wasCreatedWithAdminValueEquals(true);
    }

    @Test
    public void givenInexistentUser_whenGetUser_thenCreateItAsRegularUser() {
        given()
            .usernameToConnectWithJiraIs("mary")
            .userDoesntExists("john")

        .whenExecute(() -> {
            getUserResult = subject.getTaskboardUser("john");
        })

        .thenAssert("john")
            .wasntGetFromRepository()
            .wasCreatedWithAdminValueEquals(false);
    }

    @Test
    public void givenExistentUser_whenCallUpdateLastLoginToNow_thenUpdateLastLoginValue() {
        given()
            .userExists("mary")

        .whenExecute(() -> subject.updateLastLoginToNow("mary"))

        .thenAssert("mary")
            .lastLoginWasUpdated();
    }

    @Test
    public void givenCreatedUser_whenCallUpdateLastLoginToNow_thenUpdateLastLoginValue() {
        given()
            .usernameToConnectWithJiraIs("mary")
            .userDoesntExists("john")

        .whenExecute(() -> subject.updateLastLoginToNow("john"))

        .thenAssert("john")
            .wasCreatedWithAdminValueEquals(false)
            .lastLoginWasUpdated();
    }

    private TaskboardUserServiceTestDSL given() {
        return new TaskboardUserServiceTestDSL();
    }

    private class TaskboardUserServiceTestDSL {

        private Map<String, TaskboardUser> registeredUsers = Maps.newHashMap();

        private JiraProperties jiraProperties = mock(JiraProperties.class);
        private TaskboardUserRepository taskboardUserRepository = mock(TaskboardUserRepository.class);
        private FixedClock clock = new FixedClock();

        public TaskboardUserServiceTestDSL() {
            clock.setNow("2018-04-10T12:00:00.00Z");
            when(taskboardUserRepository.getByUsername(anyString())).thenReturn(Optional.empty());

            subject = new TaskboardUserService(jiraProperties, taskboardUserRepository, clock);
        }

        public TaskboardUserServiceTestDSL usernameToConnectWithJiraIs(String username) {
            Lousa lousaMock = mock(Lousa.class);
            when(jiraProperties.getLousa()).thenReturn(lousaMock);
            when(lousaMock.getUsername()).thenReturn(username);
            return this;
        }

        public TaskboardUserServiceTestDSL userExists(String username) {
            TaskboardUser userMock = spy(TaskboardUser.class);
            when(userMock.getUsername()).thenReturn(username);
            registeredUsers.put(username, userMock);

            when(taskboardUserRepository.getByUsername(eq(username))).thenReturn(Optional.of(userMock));
            return this;
        }

        public TaskboardUserServiceTestDSL userDoesntExists(String username) {
            when(taskboardUserRepository.getByUsername(eq(username))).thenReturn(Optional.empty());
            return this;
        }

        public TaskboardUserServiceTestDSL whenExecute(Runnable runnable) {
            runnable.run();
            return this;
        }

        public TaskboardUserServiceTestDSLAsserter thenAssert(String username) {
            return new TaskboardUserServiceTestDSLAsserter(username);
        }

        private class TaskboardUserServiceTestDSLAsserter {

            private final String username;

            public TaskboardUserServiceTestDSLAsserter(String username) {
                this.username = username;
            }

            public TaskboardUserServiceTestDSLAsserter wasGetFromRepository() {
                assertEquals(registeredUsers.get(username), getUserResult);
                return this;
            }

            public TaskboardUserServiceTestDSLAsserter wasntGetFromRepository() {
                assertEquals(registeredUsers.get(username), null);
                return this;
            }

            public TaskboardUserServiceTestDSLAsserter wasCreatedWithAdminValueEquals(boolean isAdmin) {
                Optional<TaskboardUser> createdUser = getCreatedUser();

                assertTrue("User "+ username +" needs to be created.", createdUser.isPresent());
                assertEquals(isAdmin, createdUser.get().isAdmin());

                return this;
            }

            public TaskboardUserServiceTestDSLAsserter wasNotCreated() {
                ArgumentCaptor<TaskboardUser> argumentUser = ArgumentCaptor.forClass(TaskboardUser.class);
                verify(taskboardUserRepository, atLeast(0)).add(argumentUser.capture());

                boolean wasSaved = argumentUser.getAllValues().stream()
                    .anyMatch(user -> user.getUsername().equals(username));

                assertFalse(wasSaved);

                return this;
            }

            public TaskboardUserServiceTestDSLAsserter lastLoginWasUpdated() {
                TaskboardUser user = getCreatedUser().orElse(registeredUsers.get(username));
                assertEquals(clock.now(), user.getLastLogin().orElseThrow(IllegalStateException::new)); 
                return this;
            }

            private Optional<TaskboardUser> getCreatedUser() {
                ArgumentCaptor<TaskboardUser> argumentUser = ArgumentCaptor.forClass(TaskboardUser.class);
                verify(taskboardUserRepository, atLeast(0)).add(argumentUser.capture());

                return argumentUser.getAllValues().stream()
                    .filter(user -> user.getUsername().equals(username))
                    .findAny();
            }

        }
    }

}
