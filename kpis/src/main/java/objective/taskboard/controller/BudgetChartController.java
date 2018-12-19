package objective.taskboard.controller;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Objects;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.followup.budget.BudgetChartService;
import objective.taskboard.followup.budget.ProjectNotFoundException;
import objective.taskboard.utils.DateTimeUtils;

@RestController
public class BudgetChartController {

    private static final Logger log = getLogger(BudgetChartController.class);

    private CacheManager cacheManager;
    private BudgetChartService budgetChartService;
    private Cache cache;

    @Autowired
    public BudgetChartController (CacheManager cacheManager, BudgetChartService budgetChartService) {
        this.cacheManager = cacheManager;
        this.budgetChartService = budgetChartService;
        cache = this.cacheManager.getCache(CacheConfiguration.DASHBOARD_BUDGET_DATA);
    }

    @RequestMapping(value = "/api/projects/{project}/followup/budget", method = RequestMethod.GET)
    public ResponseEntity<Object> budget(
            @PathVariable("project") String projectKey,
            @RequestParam("timezone") String zoneId) {

        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Key cacheKey = new Key(projectKey, zoneId, today);

        try {
            return ResponseEntity.ok().body(cache.get(cacheKey, () -> budgetChartService.load(DateTimeUtils.determineTimeZoneId(zoneId), cacheKey.projectKey)));
        } catch(ProjectNotFoundException e) {
            log.error("Project not found or permission denied for it " + projectKey, e);
            return new ResponseEntity<Object>(e.getMessage(), NOT_FOUND);
        }
        catch(RuntimeException e) {
            log.error("Error on load " + projectKey, e);
            return new ResponseEntity<Object>(e.getMessage(), INTERNAL_SERVER_ERROR);
        }

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
