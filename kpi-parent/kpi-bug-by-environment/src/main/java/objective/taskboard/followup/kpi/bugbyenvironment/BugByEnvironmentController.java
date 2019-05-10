package objective.taskboard.followup.kpi.bugbyenvironment;

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
import objective.taskboard.followup.kpi.exception.KpiValidationException;
import objective.taskboard.jira.ProjectService;

@RestController
public class BugByEnvironmentController {
    
    private ProjectDashboardOperationalPermission projectDashboardOperationalPermission;
    private ProjectService projectService;
    private BugByEnvironmentDataProvider dataProvider;

    @Autowired
    public BugByEnvironmentController(
            ProjectDashboardOperationalPermission projectDashboardOperationalPermission,
            ProjectService projectService, 
            BugByEnvironmentDataProvider dataProvider) {
        this.projectDashboardOperationalPermission = projectDashboardOperationalPermission;
        this.projectService = projectService;
        this.dataProvider = dataProvider;
    }
    
    @GetMapping("/api/projects/{project}/followup/bugByEnvironment")
    public ResponseEntity<Object> get(
                    @PathVariable("project") String projectKey,
                    @RequestParam("timezone") String zoneId) {
        try {
            validate(projectKey);
        } catch (KpiValidationException e) { //NOSONAR
            return new ResponseEntity<>(e.getMessage(), e.getStatus());
        }
        ZoneId timezone = determineTimeZoneId(zoneId);
        return ResponseEntity.ok().body(dataProvider.getDataSet(projectKey, timezone));
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
