package objective.taskboard.controller;

import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_OPERATIONAL;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_TACTICAL;
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

import objective.taskboard.auth.authorizer.Authorizer;
import objective.taskboard.followup.ThroughputKPIDataProvider;
import objective.taskboard.jira.ProjectService;

@RestController
public class ThroughputKPIController {
    @Autowired
    private ThroughputKPIDataProvider throughputDataProvider;
    
    @Autowired
    private Authorizer authorizer;
    
    @Autowired
    private ProjectService projectService;
    
    @RequestMapping(value = "/api/projects/{project}/followup/throughput", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> data(
            @PathVariable("project") String projectKey,
            @RequestParam("timezone") String zoneId,
            @RequestParam("level") String level) {

        if (!authorizer.hasPermission(PROJECT_DASHBOARD_TACTICAL, projectKey)
                && !authorizer.hasPermission(PROJECT_DASHBOARD_OPERATIONAL, projectKey))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        if (!projectService.taskboardProjectExists(projectKey))
            return new ResponseEntity<>("Project not found: " + projectKey + ".", HttpStatus.NOT_FOUND);

        ZoneId timezone = determineTimeZoneId(zoneId);

        return new ResponseEntity<>(throughputDataProvider.getDataSet(projectKey, level, timezone), HttpStatus.OK);
    }
}

