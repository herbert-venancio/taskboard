package objective.taskboard.data.converter;

import objective.taskboard.data.User;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.data.JiraUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JiraUserToUserConverter {

    private static final String CUSTOMER_ROLE_NAME = "Customer";

    @Autowired
    private ProjectService projectService;

    public User convert(JiraUser source) {
        return new User(source.displayName, source.name, source.emailAddress, source.getAvatarUri(), isCustomer(source));
    }

    private boolean isCustomer(JiraUser jiraUser) {
        List<JiraProject.Role> allUserRoles = projectService.getUserRoles(jiraUser.name)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        return allUserRoles.isEmpty() ||
                allUserRoles.stream()
                        .anyMatch(role -> CUSTOMER_ROLE_NAME.equals(role.name));
    }
}
