package objective.taskboard.auth;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_ADMINISTRATORS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_CUSTOMERS;
import static objective.taskboard.auth.LoggedUserDetails.JiraRole.PROJECT_DEVELOPERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.auth.LoggedUserDetails.JiraRole;

@RunWith(MockitoJUnitRunner.class)
public class LoggedUserDetailsTest {

    private LoggedUserDetails impersonateUser;

    private LoggedUserDetails subject;

    @Test
    public void givenUserWithoutImpersonateUsername_whenGetValues_returnRealUserValues() {
        subject = new LoggedUserDetailsBuilder()
                .username("real.username")
                .admin(true)
                .jiraRoles(PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS)
                .build();

        assertEquals("real.username", subject.defineUsername());
        assertEquals("real.username", subject.getRealUsername());
        assertEquals(true, subject.isAdmin());
        assertJiraRoles(subject.getJiraRoles(), PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS);
        assertEquals("real.username", subject.toString());
    }

    @Test
    public void givenUserImpersonatingSomeone_whenGetValues_returnImpersonateValues() {
        impersonateUser = new LoggedUserDetailsBuilder()
                .username("someone.username")
                .admin(false)
                .jiraRoles(PROJECT_CUSTOMERS)
                .build();

        subject = new LoggedUserDetailsBuilder()
                .username("real.username")
                .admin(true)
                .jiraRoles(PROJECT_ADMINISTRATORS, PROJECT_DEVELOPERS)
                .impersonating(impersonateUser)
                .build();

        assertEquals("someone.username", subject.defineUsername());
        assertEquals("real.username", subject.getRealUsername());
        assertEquals(false, subject.isAdmin());
        assertJiraRoles(subject.getJiraRoles(), PROJECT_CUSTOMERS);
        assertEquals("real.username", subject.toString());
    }

    private void assertJiraRoles(List<JiraRole> actualJiraRoles, String... expectedJiraRoles) {
        List<String> actualNames = actualJiraRoles.stream().map(jr -> jr.name).collect(toList());
        assertThat(actualNames)
            .containsExactlyInAnyOrder(expectedJiraRoles);
    }

    private class LoggedUserDetailsBuilder {

        private String username;
        private List<JiraRole> jiraRoles;
        private boolean isAdmin;
        private Optional<LoggedUserDetails> impersonateUser = Optional.empty();

        public LoggedUserDetailsBuilder username(String username) {
            this.username = username;
            return this;
        }

        public LoggedUserDetailsBuilder admin(boolean isAdmin) {
            this.isAdmin = isAdmin;
            return this;
        }

        public LoggedUserDetailsBuilder jiraRoles(String... jiraRoles) {
            this.jiraRoles = stream(jiraRoles).map(jr -> new JiraRole(0L, jr, "ANY")).collect(toList());
            return this;
        }

        public LoggedUserDetailsBuilder impersonating(LoggedUserDetails user) {
            this.impersonateUser = Optional.of(user);
            return this;
        }

        public LoggedUserDetails build() {
            LoggedUserDetails user = new LoggedUserDetails(username, jiraRoles, isAdmin);
            impersonateUser.ifPresent(impersonate -> user.setImpersonateUser(impersonate));
            return user;
        }

    }

}
