package objective.taskboard.controller;

import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_OPERATIONAL_VIEW;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_TACTICAL_VIEW;

import java.io.IOException;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import objective.taskboard.TaskboardProperties;
import objective.taskboard.auth.authorizer.Authorizer;
import objective.taskboard.data.User;
import objective.taskboard.jira.JiraService;

@Controller
public class FollowUpDashboardController {

    @Autowired
    private JiraService jiraService;

    @Autowired
    private TaskboardProperties taskboardProperties;

    @Autowired
    private Authorizer authorizer;

    @RequestMapping("/followup-dashboard")
    public String followUpDashboard(Model model) {
        if (!authorizer.hasPermission(PROJECT_DASHBOARD_TACTICAL_VIEW) &&
                !authorizer.hasPermission(PROJECT_DASHBOARD_OPERATIONAL_VIEW))
            throw new ResourceNotFoundException();

        User user = jiraService.getLoggedUser();
        model.addAttribute("logo", serialize(taskboardProperties.getLogo()));
        model.addAttribute("user", serialize(user));
        model.addAttribute("permissions", serialize(authorizer.getPermissions()));
        return "followup-dashboard";
    }

    private String serialize(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
