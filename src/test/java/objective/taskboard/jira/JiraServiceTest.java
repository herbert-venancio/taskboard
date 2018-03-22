package objective.taskboard.jira;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.willReturn;
import static org.mockito.Matchers.eq;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.jira.data.JiraUser;
import objective.taskboard.jira.data.plugin.UserDetail;

@RunWith(MockitoJUnitRunner.class)
public class JiraServiceTest {

    private static final Map<String, URI> AVATARS = singletonMap("48x48", URI.create("http://www.gravatar.com/avatar/c2b78b1ds52b346ff4528044ee123cc74?d=mm&s=48"));

    @Spy
    private JiraService jiraService;

    @Test
    public void givenUserHasCustomerRole_thenUserIsCustomer() {
        // given
        willReturn(customerRole()).given(jiraService).getUserRoles(eq("albert.customer"));

        // then
        assertThat(jiraService.getUser(albertCustomer()).isCustomer).isTrue();
    }

    @Test
    public void givenUserHasDevelopersRole_thenUserIsNotCustomer() {
        // given
        willReturn(developerRole()).given(jiraService).getUserRoles(eq("thomas.developer"));

        // then
        assertThat(jiraService.getUser(thomasDeveloper()).isCustomer).isFalse();
    }

    @Test
    public void givenUserHasReviewerRole_thenUserIsNotCustomer() {
        // given
        willReturn(reviewerRole()).given(jiraService).getUserRoles(eq("graham.reviewer"));

        // then
        assertThat(jiraService.getUser(grahamReviewer()).isCustomer).isFalse();
    }

    @Test
    public void givenUserHasNoRoleForSomeReason_thenUserIsCustomer() {
        // given
        willReturn(emptyList()).given(jiraService).getUserRoles(eq("john.doe"));

        // then
        assertThat(jiraService.getUser(johnDoe()).isCustomer).isTrue();
    }

    private static JiraUser albertCustomer() {
        String username = "albert.customer";
        String displayName = "Albert Customer";
        String email = "albert.customer@mail.com";
        return new JiraUser(username, displayName, email, AVATARS);
    }

    private static JiraUser thomasDeveloper() {
        String username = "thomas.developer";
        String displayName = "Thomas Developer";
        String email = "thomas.developer@mail.com";
        return new JiraUser(username, displayName, email, AVATARS);
    }

    private static JiraUser grahamReviewer() {
        String username = "graham.reviewer";
        String displayName = "Graham Reviewer";
        String email = "graham.reviewer@mail.com";
        return new JiraUser(username, displayName, email, AVATARS);
    }

    private static JiraUser johnDoe() {
        String username = "john.doe";
        String displayName = "John Doe";
        String email = "john.doe@mail.com";
        return new JiraUser(username, displayName, email, AVATARS);
    }

    private static List<UserDetail.Role> customerRole() {
        return singletonList(new UserDetail.Role(1L, "Customer", "TASKB"));
    }

    private static List<UserDetail.Role> developerRole() {
        return singletonList(new UserDetail.Role(2L, "Developers", "TASKB"));
    }

    private static List<UserDetail.Role> reviewerRole() {
        return singletonList(new UserDetail.Role(3L, "Reviewer", "TASKB"));
    }
}
