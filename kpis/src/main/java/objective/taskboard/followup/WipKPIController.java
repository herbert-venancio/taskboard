package objective.taskboard.followup;

import static objective.taskboard.utils.DateTimeUtils.determineTimeZoneId;

import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardOperationalPermission;
import objective.taskboard.auth.authorizer.permission.ProjectDashboardTacticalPermission;
import objective.taskboard.jira.ProjectService;

@RestController
public class WipKPIController {

    @Autowired
    private WipKPIDataProvider wipDataProvider;
    
    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectDashboardTacticalPermission dashboardTacticalPermission;

    @Autowired
    private ProjectDashboardOperationalPermission projectDashboardOperationalPermission;
    
    @RequestMapping(value = "/api/projects/{project}/followup/wip", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> data(
            @PathVariable("project") String projectKey,
            @RequestParam("timezone") String zoneId,
            @RequestParam("level") String level) {
        
        if (!dashboardTacticalPermission.isAuthorizedFor(projectKey)
                && !projectDashboardOperationalPermission.isAuthorizedFor(projectKey))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (!projectService.taskboardProjectExists(projectKey))
            return new ResponseEntity<>("Project not found: " + projectKey + ".", HttpStatus.NOT_FOUND);
        
        ZoneId timezone = determineTimeZoneId(zoneId);
        
        return new ResponseEntity<>(wipDataProvider.getDataSet(projectKey, level, timezone), HttpStatus.OK);
    }
}
