package objective.taskboard.monitor;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_OPERATIONAL;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_TACTICAL;
import static objective.taskboard.utils.DateTimeUtils.determineTimeZoneId;

import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.jira.AuthorizedProjectsService;
import objective.taskboard.jira.ProjectService;

@RestController
@RequestMapping("ws/strategical-dashboard")
public class StrategicalDashboardController {

    private final ProjectService projectService;
    private final AuthorizedProjectsService authorizedProjectsService;
    private final MonitorDataProvider monitorDataProvider;

    @Autowired
    public StrategicalDashboardController(
            ProjectService projectService,
            AuthorizedProjectsService authorizedProjectsService,
            MonitorDataProvider monitorDataProvider) {
        this.projectService = projectService;
        this.authorizedProjectsService = authorizedProjectsService;
        this.monitorDataProvider = monitorDataProvider;
    }

    @RequestMapping("/projects")
    public List<StrategicalProjectDataSet> getProjectsVisibleOnDashboard(
            @RequestParam("timezone") String zoneId
        ) {

        ZoneId timezone = determineTimeZoneId(zoneId);
        return authorizedProjectsService
                .getTaskboardProjects(projectService::isNonArchivedAndUserHasAccess, PROJECT_DASHBOARD_TACTICAL, PROJECT_DASHBOARD_OPERATIONAL)
                .stream()
                .map(p -> monitorDataProvider.fromProject(p, timezone))
                .collect(toList());
    }

}
