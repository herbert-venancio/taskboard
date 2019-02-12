package objective.taskboard.followup.kpi.leadtime;

import static objective.taskboard.utils.DateTimeUtils.determineTimeZoneId;

import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardOperationalPermission;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.touchTime.KpiValidationException;
import objective.taskboard.jira.ProjectService;

@RestController
public class LeadTimeKpiController {
    private ProjectDashboardOperationalPermission projectDashboardOperationalPermission;
    private ProjectService projectService;
    private LeadTimeKpiDataProvider dataProvider;

    @Autowired
    public LeadTimeKpiController(ProjectDashboardOperationalPermission projectDashboardOperationalPermission,
            ProjectService projectService, LeadTimeKpiDataProvider dataProvider) {
        this.projectDashboardOperationalPermission = projectDashboardOperationalPermission;
        this.projectService = projectService;
        this.dataProvider = dataProvider;
    }

    @GetMapping("/api/projects/{project}/followup/leadTime")
    public ResponseEntity<Object> get(
                    @PathVariable("project") String projectKey,
                    @RequestParam("timezone") String zoneId,
                    @RequestParam("level") String level) {
        KpiLevel kpiLevel;
        try {
            validate(projectKey);
            kpiLevel = getLevel(level);
        } catch (KpiValidationException e) { //NOSONAR
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        ZoneId timezone = determineTimeZoneId(zoneId);
        return ResponseEntity.ok().body(dataProvider.getDataSet(projectKey, kpiLevel, timezone));
    }

    private KpiLevel getLevel(String level) throws KpiValidationException{
        try {
            return KpiLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {//NOSONAR
            final String message = String.format("Invalid level value: %s.", level);
            throw new KpiValidationException(HttpStatus.BAD_REQUEST, message);
        }
    }

    private void validate(String projectKey) throws KpiValidationException {
        final String projectExceptionMessage = String.format("Project not found: %s.", projectKey);
        if (!projectDashboardOperationalPermission.isAuthorizedFor(projectKey))
            throw new KpiValidationException(HttpStatus.NOT_FOUND, projectExceptionMessage);

        if (!projectService.taskboardProjectExists(projectKey)) {
            throw new KpiValidationException(HttpStatus.NOT_FOUND, projectExceptionMessage);
        }
    }
}
