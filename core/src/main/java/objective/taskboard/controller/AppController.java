package objective.taskboard.controller;

import static objective.taskboard.auth.authorizer.Permissions.TEAM_EDIT;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.permission.TaskboardAdministrationPermission;
import objective.taskboard.auth.authorizer.permission.TeamEditPermission;
import objective.taskboard.auth.authorizer.permission.TeamsEditViewPermission;
import objective.taskboard.data.User;
import objective.taskboard.jira.JiraService;

@RestController
@RequestMapping("/ws/app")
public class AppController {

    @Autowired
    private JiraService jiraService;

    @Autowired
    private TaskboardAdministrationPermission taskboardAdministrationPermission;

    @Autowired
    private TeamsEditViewPermission teamsEditViewPermission;

    @Autowired
    private TeamEditPermission teamEditPermission;

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
        public Map<String, List<String>> permissionsPerKey;

        public LoggedInUserDto(User user) {
            this.username = user.name;
            this.name = user.user;
            this.avatarUrl = "/ws/avatar?username=" + this.username;
            this.permissions = getPermissions();
            this.permissionsPerKey = getPermissionsPerKey();
        }

        private List<String> getPermissions() {
            List<String> permissions = new ArrayList<>();

            if (taskboardAdministrationPermission.isAuthorized()) {
                permissions.add(taskboardAdministrationPermission.name());

                if(!taskboardAdministrationPermission.getAllTeams().isEmpty())
                    permissions.add(teamEditPermission.name());
            }

            if (teamsEditViewPermission.isAuthorized())
                permissions.add(teamsEditViewPermission.name());

            if(!teamEditPermission.applicableTargets().isEmpty())
                permissions.add(teamEditPermission.name());

            return permissions;
        }

        public Map<String, List<String>> getPermissionsPerKey() {
            Map<String, List<String>> permissionsPerKey = new HashMap<String, List<String>>();

            if (taskboardAdministrationPermission.isAuthorized()) {
                if(!taskboardAdministrationPermission.getAllTeams().isEmpty())
                    permissionsPerKey.put(TEAM_EDIT, taskboardAdministrationPermission.getAllTeams());
            }else if(!teamEditPermission.applicableTargets().isEmpty()){
                permissionsPerKey.put(teamEditPermission.name(), teamEditPermission.applicableTargets());
            }

            return permissionsPerKey;
        }

    }

}
