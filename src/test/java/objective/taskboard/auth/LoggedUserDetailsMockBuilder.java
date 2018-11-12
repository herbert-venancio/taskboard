package objective.taskboard.auth;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import objective.taskboard.auth.LoggedUserDetails.JiraRole;

public class LoggedUserDetailsMockBuilder {

    private final LoggedUserDetails mock = mock(LoggedUserDetails.class);

    public static LoggedUserDetailsMockBuilder loggedUser() {
        return new LoggedUserDetailsMockBuilder();
    }

    public static JiraRole role(String project, String role) {
        return new JiraRole(0L, role, project);
    }

    public LoggedUserDetailsMockBuilder withName(String name) {
        when(mock.getUsername()).thenReturn(name);
        return this;
    }

    public LoggedUserDetailsMockBuilder withIsAdmin(boolean isAdmin) {
        when(mock.isAdmin()).thenReturn(isAdmin);
        return this;
    }

    public LoggedUserDetailsMockBuilder withRoles(JiraRole... roles) {
        when(mock.getJiraRoles()).thenReturn(asList(roles));
        return this;
    }

    public LoggedUserDetails build() {
        return mock;
    }

}
