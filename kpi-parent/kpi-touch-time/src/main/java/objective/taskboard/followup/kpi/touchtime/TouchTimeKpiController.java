package objective.taskboard.followup.kpi.touchtime;

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
import objective.taskboard.followup.kpi.KpiValidationException;
import objective.taskboard.jira.ProjectService;

@RestController
@RequestMapping(value = "/api/projects/{project}/followup/touchtime")
class TouchTimeKpiController {

    private ProjectDashboardOperationalPermission projectDashboardOperationalPermission;

    private ProjectService projectService;

    private TouchTimeKpiProvider provider;

    @Autowired
    public TouchTimeKpiController(ProjectDashboardOperationalPermission projectDashboardOperationalPermission,
            ProjectService projectService, TouchTimeKpiProvider provider) {
        this.projectDashboardOperationalPermission = projectDashboardOperationalPermission;
        this.projectService = projectService;
        this.provider = provider;
    }

    @GetMapping("{method}")
    public ResponseEntity<Object> getData(
            @PathVariable("method") String method,
            @PathVariable("project") String projectKey,
            @RequestParam("timezone") String zoneId,
            @RequestParam("level") String level) {

        KpiLevel kpiLevel;
        try {
            validate(projectKey);
            kpiLevel = getLevel(level);
            ZoneId timezone = determineTimeZoneId(zoneId);
            return getResponse(method, projectKey, kpiLevel, timezone);
        } catch (KpiValidationException e) { //NOSONAR
            return new ResponseEntity<>(e.getMessage(),e.getStatus());
        }
    }

    private ResponseEntity<Object> getResponse(String method, String projectKey, KpiLevel kpiLevel, ZoneId timezone) {
        try {
            return new ResponseEntity<>(provider.getDataSet(method, projectKey, kpiLevel, timezone), HttpStatus.OK);
        } catch (IllegalArgumentException e) { //NOSONAR
            throw new KpiValidationException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    private KpiLevel getLevel(String level) {
        try {
            return KpiLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) { //NOSONAR
            final String message = String.format("Invalid level value: %s.", level);
            throw new KpiValidationException(HttpStatus.BAD_REQUEST,message);
        }
    }

    private void validate(String projectKey) {
        final String projectExceptionMessage = String.format("Project not found: %s.", projectKey);
        if (!projectDashboardOperationalPermission.isAuthorizedFor(projectKey))
            throw new KpiValidationException(HttpStatus.NOT_FOUND,projectExceptionMessage);

        if (!projectService.taskboardProjectExists(projectKey)) {
            throw new KpiValidationException(HttpStatus.NOT_FOUND,projectExceptionMessage);
        }
    }

}