package objective.taskboard.controller;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.followup.FollowupCluster;
import objective.taskboard.followup.FollowupClusterProvider;
import objective.taskboard.followup.PlannedVsBallparkDataAccumulator;
import objective.taskboard.followup.impl.FollowUpDataProviderFromCurrentState;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;


@RestController
public class FollowUpPlannedBallparkController {
    
    @Autowired
    private FollowUpDataProviderFromCurrentState followUpDataProviderFromCurrentState;
    
    @Autowired
    private FollowupClusterProvider followupClusterProvider;
    
    @Autowired
    private ProjectFilterConfigurationCachedRepository projects;
    
    @RequestMapping(value = "/api/projects/{projectKey}/followup/planned-ballpark", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> data(@PathVariable("projectKey") String projectKey) {
        if (!projects.exists(projectKey))
            return new ResponseEntity<>("Project not found: " + projectKey + ".", HttpStatus.NOT_FOUND);
        
        Optional<FollowupCluster> followupCluster = followupClusterProvider.getForProject(projectKey);
        if (!followupCluster.isPresent())
            return new ResponseEntity<>("No cluster configuration found for project " + projectKey + ".", INTERNAL_SERVER_ERROR);
        
        final PlannedVsBallparkDataAccumulator accumulator = new PlannedVsBallparkDataAccumulator();
        followUpDataProviderFromCurrentState.getJiraData(followupCluster.get(), projectKey).forEachRow(accumulator::accumulate);

        return ResponseEntity.ok(accumulator.getData());
    }
}
