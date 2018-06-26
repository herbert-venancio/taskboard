package objective.taskboard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.data.User;
import objective.taskboard.jira.JiraService;

@RestController
@RequestMapping("/ws/app")
public class AppController {
    
    @Autowired
    private JiraService jiraService;

    @GetMapping("initial-data")
    public InitialDataDto getInitialData() {
        InitialDataDto data = new InitialDataDto();

        data.loggedInUser = new LoggedInUserDto(jiraService.getLoggedUser());

        return data;
    }
    
    public static class InitialDataDto {
        public LoggedInUserDto loggedInUser;
    }
    
    public static class LoggedInUserDto {
        public String username;
        public String name;
        public String avatarUrl;

        public LoggedInUserDto(User loggedInUser) {
            this.username = loggedInUser.name;
            this.name = loggedInUser.user;
            this.avatarUrl = "/ws/avatar?username=" + this.username;
        }
    }
}
