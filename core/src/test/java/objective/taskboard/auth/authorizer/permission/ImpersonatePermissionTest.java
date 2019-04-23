package objective.taskboard.auth.authorizer.permission;

import static ch.qos.logback.classic.Level.WARN;
import static objective.taskboard.auth.authorizer.Permissions.IMPERSONATE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import objective.taskboard.auth.LoggedUserDetails;

@RunWith(MockitoJUnitRunner.class)
public class ImpersonatePermissionTest {

    private final Logger log = (Logger) LoggerFactory.getLogger(ImpersonatePermission.class);
    private final ListAppender<ILoggingEvent> listAppender = new ListAppender<>();

    private final LoggedUserDetails loggedInUser = user();

    private ImpersonatePermission subject;

    @Test
    public void testName() {
        setupLoggedInUser("mary.harper")
            .done();

        assertEquals(IMPERSONATE, subject.name());
    }

    @Test
    public void givenLoggedInUserWithAllNecessaryPermissions_thenIsAuthorized() {
        setupLoggedInUser("mary")
            .withTaskboardAdministrationPermission()
            .withUserVisibilityPermissionFor("john")
            .done();

        assertTrue(subject.isAuthorized(loggedInUser, "john"));
        assertTrue(subject.isAuthorizedFor("john"));

        assertTotalOfWarnsLogged(0);
    }

    @Test
    public void givenSomeUserWithAllNecessaryPermissions_thenIsAuthorized() {
        final LoggedUserDetails user = user();

        setupSomeUser(user, "daniel")
            .withTaskboardAdministrationPermission()
            .withUserVisibilityPermissionFor("john")
            .done();

        assertTrue(subject.isAuthorized(user, "john"));

        assertTotalOfWarnsLogged(0);
    }

    @Test
    public void givenLoggedInUserWithoutNecessaryPermission_thenIsNotAuthorized() {
        setupLoggedInUser("mary")
            .withoutTaskboardAdministrationPermission()
            .withUserVisibilityPermissionFor("john")
            .done();

        assertFalse(subject.isAuthorized(loggedInUser, "john"));
        assertFalse(subject.isAuthorizedFor("john"));

        setupLoggedInUser("mary")
            .withTaskboardAdministrationPermission()
            .withoutUserVisibilityPermissionFor("john")
            .done();

        assertFalse(subject.isAuthorized(loggedInUser, "john"));
        assertFalse(subject.isAuthorizedFor("john"));

        assertTotalOfWarnsLogged(4);
    }

    @Test
    public void givenSomeUserWithoutNecessaryPermission_thenIsNotAuthorized() {
        final LoggedUserDetails user = user();

        setupSomeUser(user, "daniel")
            .withoutTaskboardAdministrationPermission()
            .withUserVisibilityPermissionFor("john")
            .done();

        assertFalse(subject.isAuthorized(user, "john"));

        setupSomeUser(user, "daniel")
            .withTaskboardAdministrationPermission()
            .withoutUserVisibilityPermissionFor("john")
            .done();

        assertFalse(subject.isAuthorized(user, "john"));

        setupSomeUser(user, "daniel")
            .withoutTaskboardAdministrationPermission()
            .withoutUserVisibilityPermissionFor("john")
            .done();

        assertFalse(subject.isAuthorized(user, "john"));

        assertTotalOfWarnsLogged(3);
    }

    public LoggedUserDetails user() {
        return mock(LoggedUserDetails.class);
    }

    private ImpersonatePermissionTestDSL setupLoggedInUser(String username) {
        return new ImpersonatePermissionTestDSL(loggedInUser, username);
    }

    private ImpersonatePermissionTestDSL setupSomeUser(LoggedUserDetails user, String username) {
        return new ImpersonatePermissionTestDSL(user, username);
    }

    private void assertTotalOfWarnsLogged(long expectedTotalOfWarnings) {
        long actualTotalOfWarnings = listAppender.list.stream()
            .filter(log -> WARN.equals(log.getLevel()))
            .count();

        assertEquals(expectedTotalOfWarnings, actualTotalOfWarnings);
    }

    private class ImpersonatePermissionTestDSL {

        private TaskboardAdministrationPermission taskboardAdministrationPermission = mock(TaskboardAdministrationPermission.class);
        private UserVisibilityPermission userVisibilityPermission = mock(UserVisibilityPermission.class);

        private final LoggedUserDetails user;

        public ImpersonatePermissionTestDSL(LoggedUserDetails user, String username) {
            reset(user);
            reset(taskboardAdministrationPermission);
            reset(userVisibilityPermission);

            when(user.defineUsername()).thenReturn(username);

            listAppender.start();
            log.addAppender(listAppender);

            this.user = user;
        }

        public ImpersonatePermissionTestDSL withTaskboardAdministrationPermission() {
            when(taskboardAdministrationPermission.isAuthorized(user)).thenReturn(true);
            return this;
        }

        public ImpersonatePermissionTestDSL withoutTaskboardAdministrationPermission() {
            when(taskboardAdministrationPermission.isAuthorized(user)).thenReturn(false);
            return this;
        }

        public ImpersonatePermissionTestDSL withUserVisibilityPermissionFor(String target) {
            when(userVisibilityPermission.isAuthorized(user, target)).thenReturn(true);
            return this;
        }

        public ImpersonatePermissionTestDSL withoutUserVisibilityPermissionFor(String target) {
            when(userVisibilityPermission.isAuthorized(user, target)).thenReturn(false);
            return this;
        }

        public void done() {
            subject = new ImpersonatePermission(loggedInUser, taskboardAdministrationPermission, userVisibilityPermission);
        }

    }

}
