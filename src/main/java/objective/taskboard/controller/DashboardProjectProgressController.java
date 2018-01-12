package objective.taskboard.controller;

import static objective.taskboard.utils.DateTimeUtils.determineTimeZoneId;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import com.google.common.base.Objects;
import com.google.common.cache.Cache;
import objective.taskboard.config.CacheConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
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

import javax.annotation.PostConstruct;

@RestController
public class DashboardProjectProgressController {
    @Autowired
    private FollowUpDataProviderFromCurrentState providerFromCurrentState;
    
    @Autowired
    private FollowupClusterProvider clusterProvider;
    
    @Autowired
    private ProjectFilterConfigurationCachedRepository projects;

    @Autowired
    private CacheManager cacheManager;

    private Cache<Key, ResponseEntity<Object>> cache;

    @PostConstruct
    @SuppressWarnings("unchecked")
    public void initCache() {
        cache = (Cache<Key, ResponseEntity<Object>>) cacheManager.getCache(CacheConfiguration.DASHBOARD_PROGRESS_DATA).getNativeCache();
    }
    
    @RequestMapping(value = "/api/projects/{project}/followup/progress", method = RequestMethod.GET)
    public ResponseEntity<Object> progress(@PathVariable("project") String projectKey, @RequestParam("timezone") String zoneId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Key cacheKey = new Key(projectKey, zoneId, today);

        try {
            return cache.get(cacheKey, () -> load(cacheKey));
        } catch(Exception ex) {
            return new ResponseEntity<>(ex, INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<Object> load(Key key) throws Exception {
        String projectKey = key.projectKey;
        String zoneId = key.zoneId;

        Optional<ProjectFilterConfiguration> project = projects.getProjectByKey(projectKey);

        if (!project.isPresent())
            return new ResponseEntity<>("Project not found: " + projectKey + ".", HttpStatus.NOT_FOUND);

        Optional<FollowupCluster> cluster = clusterProvider.getFor(project.get());
        if (!cluster.isPresent())
            return new ResponseEntity<>("No cluster configuration found for project " + projectKey + ".", INTERNAL_SERVER_ERROR);

        ZoneId timezone = determineTimeZoneId(zoneId);

        if (project.get().getDeliveryDate() == null)
            return new ResponseEntity<>("The project " + projectKey + " has no delivery date.", INTERNAL_SERVER_ERROR);

        LocalDate deliveryDate = project.get().getDeliveryDate();

        FollowUpDataSnapshot snapshot = providerFromCurrentState.getJiraData(cluster.get(), new String[]{projectKey}, timezone);
        if (!snapshot.getHistory().isPresent())
            return new ResponseEntity<>("No progress history found for project " + projectKey, INTERNAL_SERVER_ERROR);

        FollowupProgressCalculator calculator = new FollowupProgressCalculator();

        ProgressData progressData = calculator.calculate(snapshot, deliveryDate);

        return ResponseEntity.ok().body(progressData);
    }

    private static class Key {
        public final String projectKey;
        public final String zoneId;
        public final String today;

        public Key(String projectKey, String zoneId, String today) {
            this.projectKey = projectKey;
            this.zoneId = zoneId;
            this.today = today;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key key = (Key) o;
            return Objects.equal(projectKey, key.projectKey) &&
                    Objects.equal(zoneId, key.zoneId) &&
                    Objects.equal(today, key.today);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(projectKey, zoneId, today);
        }
    }
}
