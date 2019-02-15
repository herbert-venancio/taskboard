package objective.taskboard.controller;

import static objective.taskboard.utils.DateTimeUtils.determineTimeZoneId;
import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.ZoneId;
import java.util.Optional;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardTacticalPermission;
import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.SnapshotGeneratedEvent;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;
import objective.taskboard.followup.data.FollowupProgressCalculator;
import objective.taskboard.followup.data.ProgressData;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@CacheConfig(cacheNames = CacheConfiguration.DASHBOARD_PROGRESS_DATA)
@RestController
public class FollowUpProgressController {

    private static final Logger log = getLogger(FollowUpProgressController.class);

    @Autowired
    private FollowupProgressCalculator calculator;

    @Autowired
    private ProjectFilterConfigurationCachedRepository projects;

    @Autowired
    private ProjectDashboardTacticalPermission dashboardTacticalPermission;

    @CacheEvict(allEntries=true)
    @EventListener
    public void clearCache(SnapshotGeneratedEvent event) {
    }

    @Cacheable(key = "#projectKey + #zoneId + #projectionTimespanParam")
    @GetMapping("/api/projects/{project}/followup/progress")
    public ResponseEntity<Object> progress(
            @PathVariable("project") String projectKey,
            @RequestParam("timezone") String zoneId,
            @RequestParam(value = "projection", required = false) Optional<Integer> projectionTimespanParam) {
        if (!dashboardTacticalPermission.isAuthorizedFor(projectKey))
            return new ResponseEntity<>("Resource not found.", HttpStatus.NOT_FOUND);

        ZoneId timezone = determineTimeZoneId(zoneId);
        Optional<ProjectFilterConfiguration> projectOpt = projects.getProjectByKey(projectKey);

        if (!projectOpt.isPresent()) {
            log.error("Project not found or permission denied for it " + projectKey);
            return new ResponseEntity<>("Project not found: " + projectKey + ".", NOT_FOUND);
        }

        ProjectFilterConfiguration project = projectOpt.get();
        Integer projectionTimespan = projectionTimespanParam.orElse(project.getProjectionTimespan());

        if (projectionTimespan <= 0) {
            log.error("Project timespan was invalid. " + projectKey);
            return new ResponseEntity<>("The projection timespan should be a positive number.", BAD_REQUEST);
        }

        try {
            ProgressData progressData = calculator.calculateWithExpectedProjection(timezone, projectKey, projectionTimespan);
            return ResponseEntity.ok().body(progressData);
        } catch (ClusterNotConfiguredException e) {//NOSONAR
            return new ResponseEntity<>("No cluster configuration found for project " + projectKey + ".", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (ProjectDatesNotConfiguredException e) {//NOSONAR
            return new ResponseEntity<>("The project " + projectKey + " has no start or delivery date.", INTERNAL_SERVER_ERROR);
        } catch(Exception e) {
            log.error("Error on load " + projectKey, e);
            return new ResponseEntity<>("Unexpected behavior. Please, report this error to the administrator.", INTERNAL_SERVER_ERROR);
        }
    }
}
