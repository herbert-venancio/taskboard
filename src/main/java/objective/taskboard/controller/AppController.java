package objective.taskboard.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.permission.TaskboardAdministrationPermission;
import objective.taskboard.data.User;
import objective.taskboard.jira.JiraService;

@RestController
@RequestMapping("/ws/app")
public class AppController {

    @Autowired
    private JiraService jiraService;

    @Autowired
    private TaskboardAdministrationPermission taskboardAdministrationPermission;

    @GetMapping("initial-data")
    public InitialDataDto getInitialData() {
        InitialDataDto data = new InitialDataDto();

        data.loggedInUser = new LoggedInUserDto(jiraService.getLoggedUser());

        return data;
    }

    class InitialDataDto {
        public LoggedInUserDto loggedInUser;
    }

    class LoggedInUserDto {
        public String username;
        public String name;
        public String avatarUrl;
        public List<String> permissions;

        public LoggedInUserDto(User user) {
            this.username = user.name;
            this.name = user.user;
            this.avatarUrl = "/ws/avatar?username=" + this.username;
            this.permissions = getPermissions();
        }

        private List<String> getPermissions() {
            List<String> permissions = new ArrayList<>();

            if (taskboardAdministrationPermission.isAuthorized())
                permissions.add(taskboardAdministrationPermission.name());

            return permissions;
        }
    }

}
