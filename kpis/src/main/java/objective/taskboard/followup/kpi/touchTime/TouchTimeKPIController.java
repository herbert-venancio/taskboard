package objective.taskboard.followup.kpi.touchTime;

import static objective.taskboard.utils.DateTimeUtils.determineTimeZoneId;

import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardOperationalPermission;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.jira.ProjectService;

@RestController
@RequestMapping(value = "/api/projects/{project}/followup/touchtime")
class TouchTimeKPIController {

    @Autowired
    private TouchTimeKPIDataProvider touchTimeKpiDataProvider;

    @Autowired
    private ProjectDashboardOperationalPermission projectDashboardOperationalPermission;

    @Autowired
    private ProjectService projectService;

    @GetMapping("byissues")
    public ResponseEntity<Object> byIssues(
            @PathVariable("project") String projectKey,
            @RequestParam("timezone") String zoneId,
            @RequestParam("level") String level) {

        if (!projectDashboardOperationalPermission.isAuthorizedFor(projectKey))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (!projectService.taskboardProjectExists(projectKey)) {
            final String message = String.format("Project not found: %s.", projectKey);
            return new ResponseEntity<>(message, HttpStatus.NOT_FOUND);
        }

        ZoneId timezone = determineTimeZoneId(zoneId);

        final KpiLevel kpiLevel;
        try {
            kpiLevel = KpiLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {//NOSONAR
            final String message = String.format("Invalid level value: %s.", level);
            return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
        }
        final TouchTimeChartDataSet dataSet = touchTimeKpiDataProvider.getDataSet(projectKey, kpiLevel, timezone);
        return new ResponseEntity<>(dataSet, HttpStatus.OK);
    }
}
