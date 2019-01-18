package objective.taskboard.controller;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import objective.taskboard.followup.ChangeRequestUpdatedEvent;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.event.EventListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.followup.SnapshotGeneratedEvent;
import objective.taskboard.followup.budget.BudgetChartService;
import objective.taskboard.followup.budget.ProjectNotFoundException;
import objective.taskboard.utils.DateTimeUtils;

@CacheConfig(cacheNames = CacheConfiguration.DASHBOARD_BUDGET_DATA)
@RestController
public class BudgetChartController {

    private static final Logger log = getLogger(BudgetChartController.class);
    private BudgetChartService budgetChartService;

    @Autowired
    public BudgetChartController (BudgetChartService budgetChartService) {
        this.budgetChartService = budgetChartService;
    }

    @CacheEvict(allEntries=true)
    @EventListener
    public void clearCache(SnapshotGeneratedEvent event) {
    }

    @CacheEvict(allEntries=true)
    @EventListener
    public void clearCache(ChangeRequestUpdatedEvent event) {
    }

    @Cacheable(key = "#projectKey + #zoneId")
    @GetMapping("/api/projects/{project}/followup/budget")
    public ResponseEntity<Object> budget(
            @PathVariable("project") String projectKey,
            @RequestParam("timezone") String zoneId) {

        try {
            return ResponseEntity.ok().body(budgetChartService.load(DateTimeUtils.determineTimeZoneId(zoneId), projectKey));
        } catch(ProjectNotFoundException e) {
            log.error("Project not found or permission denied for it " + projectKey, e);
            return new ResponseEntity<Object>(e.getMessage(), NOT_FOUND);
        }
        catch(RuntimeException e) {
            log.error("Error on load " + projectKey, e);
            return new ResponseEntity<Object>(e.getMessage(), INTERNAL_SERVER_ERROR);
        }

    }
}