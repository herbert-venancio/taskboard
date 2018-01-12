package objective.taskboard.controller;

import static objective.taskboard.utils.DateTimeUtils.parseDate;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.OK;

import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.zone.ZoneRulesException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.followup.FollowupCluster;
import objective.taskboard.followup.FollowupClusterProvider;
import objective.taskboard.followup.impl.FollowUpScopeByTypeDataProvider;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

@RestController
public class FollowUpScopeByTypeController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FollowUpScopeByTypeController.class);

    @Autowired
    private FollowUpScopeByTypeDataProvider provider;

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepository;

    @Autowired
    private FollowupClusterProvider followUpClusterProvider;

    @RequestMapping(value = "/api/projects/{projectKey}/followup/scope-by-type", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> scopeByTypeData(@PathVariable("projectKey") String projectKey, @RequestParam("date") String date, @RequestParam("timezone") String timezone) {
        if (isEmpty(projectKey))
            return new ResponseEntity<>("Project is required.", HttpStatus.BAD_REQUEST);

        if (!projectRepository.exists(projectKey))
            return new ResponseEntity<>("Project not found: " + projectKey + ".", HttpStatus.NOT_FOUND);

        if (!clusterExistis(projectKey))
            return new ResponseEntity<>("No cluster configuration found for project " + projectKey + ".", INTERNAL_SERVER_ERROR);

        if (!isValidDate(date))
            return new ResponseEntity<>("Invalid date: " + date + ".", HttpStatus.BAD_REQUEST);

        if (!isValidZoneId(timezone))
            return new ResponseEntity<>("Invalid timezone: " + timezone + ".", HttpStatus.BAD_REQUEST);

        ZoneId zoneId = isEmpty(timezone) ? ZoneId.systemDefault() : ZoneId.of(timezone);
        return new ResponseEntity<>(provider.getScopeByTypeData(projectKey, date, zoneId), OK);
    }

    private boolean clusterExistis(String projectKey) {
        Optional<FollowupCluster> cluster = followUpClusterProvider.getForProject(projectKey);
        return cluster.isPresent();
    }

    private boolean isValidDate(String yyyymmdd) {
        if (!isEmpty(yyyymmdd)) {
            try {
                parseDate(yyyymmdd);
            } catch (DateTimeParseException e) {
                log.warn("Error on getScopeByTypeData: Invalid date parameter: " + yyyymmdd, e);
                return false;
            }
        }
        return true;
    }

    private boolean isValidZoneId(String zoneId) {
        if (!isEmpty(zoneId)) {
            try {
                ZoneId.of(zoneId);
            } catch(ZoneRulesException e) {
                log.warn("Error on getScopeByTypeData: Invalid timezone parameter: " + zoneId, e);
                return false;
            }
        }
        return true;
    }

}
