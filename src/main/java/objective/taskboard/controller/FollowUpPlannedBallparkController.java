package objective.taskboard.controller;

import static objective.taskboard.auth.authorizer.Permissions.PROJECT_DASHBOARD_OPERATIONAL;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.Authorizer;
import objective.taskboard.followup.PlannedVsBallparkCalculator;
import objective.taskboard.followup.PlannedVsBallparkChartData;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@RestController
public class FollowUpPlannedBallparkController {
    
    @Autowired
    private PlannedVsBallparkCalculator calculator;
    
    @Autowired
    private ProjectFilterConfigurationCachedRepository projects;

    @Autowired
    private Authorizer authorizer;

    @RequestMapping(value = "/api/projects/{projectKey}/followup/planned-ballpark", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> data(@PathVariable("projectKey") String projectKey) {
        if (!authorizer.hasPermission(PROJECT_DASHBOARD_OPERATIONAL, projectKey))
            return new ResponseEntity<>("Resource not found", HttpStatus.NOT_FOUND);

        if (!projects.exists(projectKey))
            return new ResponseEntity<>("Project not found: " + projectKey + ".", HttpStatus.NOT_FOUND);

        try {
            List<PlannedVsBallparkChartData> data = calculator.calculate(projectKey);
            return ResponseEntity.ok(data);

        } catch (ClusterNotConfiguredException e) {//NOSONAR
            return new ResponseEntity<>("No cluster configuration found for project " + projectKey + ".", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
