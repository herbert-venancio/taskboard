package objective.taskboard.controller;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_OPERATIONAL;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_TACTICAL;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.Authorizer;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.FollowUpSnapshotService;
import objective.taskboard.jira.AuthorizedProjectsService;
import objective.taskboard.jira.ProjectService;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final AuthorizedProjectsService authorizedProjectsService;

    private final FollowUpSnapshotService snapshotService;

    private final Authorizer authorizer;
    
    private final ProjectService projectService;


    @Autowired
    public DashboardController(
        ProjectService projectService,
        AuthorizedProjectsService authorizedProjectsService,
        Authorizer authorizer,
        FollowUpSnapshotService snapshotService) {
        this.projectService = projectService;
        this.authorizedProjectsService = authorizedProjectsService;
        this.authorizer = authorizer;
        this.snapshotService = snapshotService;
    }    
    
    @RequestMapping("/projects")
    public List<ProjectData> getProjectsVisibleOnDashboard() {
        return authorizedProjectsService.getTaskboardProjects(projectService::isNonArchivedAndUserHasAccess, PROJECT_DASHBOARD_TACTICAL, PROJECT_DASHBOARD_OPERATIONAL).stream()
                .map(this::generateProjectData)
                .collect(toList());
    }
    
    private ProjectData generateProjectData(ProjectFilterConfiguration projectFilterConfiguration) {
        return generateProjectData(projectFilterConfiguration.getProjectKey(), projectFilterConfiguration.getProjectKey());
    }

    private ProjectData generateProjectData(String projectKey, String projectDisplayName) {
        ProjectData projectData = new ProjectData();
        projectData.projectKey = projectKey;
        projectData.projectDisplayName = projectDisplayName;
        projectData.followUpDataHistory = snapshotService.getAvailableHistory(projectKey);
        projectData.roles = authorizer.getRolesForProject(projectKey);

        return projectData;
    }
}
