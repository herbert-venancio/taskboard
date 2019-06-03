package objective.taskboard.user;

import static java.util.stream.Collectors.toList;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.auth.authorizer.permission.UserVisibilityPermission;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.data.JiraUser.UserDetails;

@Service
class UserSearchService {

    private final JiraService jiraService;

    private final UserVisibilityPermission userVisibilityPermission;

    @Autowired
    public UserSearchService(
            JiraService jiraService,
            UserVisibilityPermission userVisibilityPermission) {
        this.jiraService = jiraService;
        this.userVisibilityPermission = userVisibilityPermission;
    }

    public List<UserDetails> getUsersVisibleToLoggedInUserByQuery(String userQuery) {
        List<UserDetails> usersFound = jiraService.findUsers(userQuery);
        return usersFound.stream()
                .filter(user -> userVisibilityPermission.isAuthorizedFor(user.name))
                .collect(toList());
    }

}
