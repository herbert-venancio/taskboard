package objective.taskboard.controller;

import static objective.taskboard.auth.authorizer.Permissions.TASKBOARD_ADMINISTRATION;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.Authorizer;
import objective.taskboard.data.User;
import objective.taskboard.jira.JiraService;

@RestController
@RequestMapping("/ws/app")
public class AppController {

    @Autowired
    private JiraService jiraService;

    @Autowired
    private Authorizer authorizer;

    @GetMapping("initial-data")
    public InitialDataDto getInitialData() {
        InitialDataDto data = new InitialDataDto();

        data.loggedInUser = new LoggedInUserDto(jiraService.getLoggedUser(), authorizer);

        return data;
    }

    static class InitialDataDto {
        public LoggedInUserDto loggedInUser;
    }

    static class LoggedInUserDto {
        public String username;
        public String name;
        public String avatarUrl;
        public List<String> permissions;

        public LoggedInUserDto(User user, Authorizer authorizer) {
            this.username = user.name;
            this.name = user.user;
            this.avatarUrl = "/ws/avatar?username=" + this.username;
            this.permissions = getPermissions(authorizer);
        }

        private List<String> getPermissions(Authorizer authorizer) {
            List<String> permissions = new ArrayList<>();

            if (authorizer.hasPermission(TASKBOARD_ADMINISTRATION))
                permissions.add(TASKBOARD_ADMINISTRATION);

            return permissions;
        }
    }

}
