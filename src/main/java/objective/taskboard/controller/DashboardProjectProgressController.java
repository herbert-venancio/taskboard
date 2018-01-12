package objective.taskboard.controller;

import static objective.taskboard.utils.DateTimeUtils.determineTimeZoneId;
import static objective.taskboard.utils.DateTimeUtils.toLocalDate;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.FollowUpDataSnapshot;
import objective.taskboard.followup.FollowupCluster;
import objective.taskboard.followup.FollowupClusterProvider;
import objective.taskboard.followup.data.FollowupProgressCalculator;
import objective.taskboard.followup.data.ProgressData;
import objective.taskboard.followup.impl.FollowUpDataProviderFromCurrentState;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@RestController
public class DashboardProjectProgressController {
    @Autowired
    private FollowUpDataProviderFromCurrentState providerFromCurrentState;
    
    @Autowired
    private FollowupClusterProvider clusterProvider;
    
    @Autowired
    private ProjectFilterConfigurationCachedRepository projects;
    
    private static Map<String, ProgressData> cache = new LinkedHashMap<>();
    
    @RequestMapping(value = "/api/projects/{project}/followup/progress", method = RequestMethod.GET)
    public ResponseEntity<Object> progress(@PathVariable("project") String projectKey, @RequestParam("timezone") String zoneId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String cacheKey = projectKey+"_"+today+"_";
        
        if (cache.get(cacheKey) != null)
            return ResponseEntity.ok().body(cache.get(cacheKey));
        
        Optional<ProjectFilterConfiguration> project = projects.getProjectByKey(projectKey);
        
        if (!project.isPresent())
            return new ResponseEntity<>("Project not found: " + projectKey + ".", HttpStatus.NOT_FOUND);
        
        Optional<FollowupCluster> cluster = clusterProvider.getFor(project.get());
        if (!cluster.isPresent())
            return new ResponseEntity<>("No cluster configuration found for project " + projectKey + ".", INTERNAL_SERVER_ERROR);
        
        ZoneId timezone = determineTimeZoneId(zoneId);
                
        if (project.get().getDeliveryDate() == null)
            return new ResponseEntity<>("The project " + projectKey + " has no delivery date.", INTERNAL_SERVER_ERROR);
        
        LocalDate deliveryDate = toLocalDate(project.get().getDeliveryDate(), timezone);
        
        FollowUpDataSnapshot snapshot = providerFromCurrentState.getJiraData(cluster.get(), new String[]{projectKey}, timezone);
        if (!snapshot.getHistory().isPresent())
            return new ResponseEntity<>("No progress history found for project " + projectKey, INTERNAL_SERVER_ERROR);
        
        FollowupProgressCalculator calculator = new FollowupProgressCalculator(timezone);
        
        ProgressData progressData = calculator.calculate(snapshot, deliveryDate);
        
        cache.put(cacheKey, progressData);
        return ResponseEntity.ok().body(progressData);
    }
}
