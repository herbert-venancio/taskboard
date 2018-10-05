package objective.taskboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.LoggedUserDetails;
import objective.taskboard.data.User;
import objective.taskboard.jira.JiraService;

@RestController
@RequestMapping("/ws/app")
public class AppController {

    @Autowired
    private JiraService jiraService;

    @Autowired
    private LoggedUserDetails loggedInUser;

    @GetMapping("initial-data")
    public InitialDataDto getInitialData() {
        InitialDataDto data = new InitialDataDto();

        data.loggedInUser = new LoggedInUserDto(jiraService.getLoggedUser(), loggedInUser);

        return data;
    }

    public static class InitialDataDto {
        public LoggedInUserDto loggedInUser;
    }

    public static class LoggedInUserDto {
        public String username;
        public String name;
        public String avatarUrl;
        public boolean isAdmin;

        public LoggedInUserDto(User user, LoggedUserDetails userDetails) {
            this.username = user.name;
            this.name = user.user;
            this.avatarUrl = "/ws/avatar?username=" + this.username;
            this.isAdmin = userDetails.isAdmin();
        }
    }

}
