package objective.taskboard.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.data.plugin.RoleData;

@RestController
@RequestMapping("/ws/roles")
public class RoleController {

    @Autowired
    private JiraService jiraService;

    @GetMapping
    public List<RoleData> get() {
        return jiraService.getVisibleRoles();
    }

}
