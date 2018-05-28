package objective.taskboard.controller;

import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.data.Issue;
import objective.taskboard.data.TaskboardTimeTracking;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.JiraService.PermissaoNegadaException;
import objective.taskboard.jira.client.JiraTimeTrackingDto;
import objective.taskboard.jira.data.Transition;
import objective.taskboard.utils.DateTimeUtils;

@RestController
@RequestMapping("/ws/issue-detail")
public class IssueDetailController {
    
    private static final Logger LOG = LoggerFactory.getLogger(IssueDetailController.class);

    @Autowired
    private IssueBufferService issueBufferService;
    
    @GetMapping("/{issueKey}")
    public ResponseEntity<Object> findByKey(@PathVariable final String issueKey, @RequestParam("timezone") final String timezone) {
        
        final Optional<Issue> issueOptional = issueBufferService.getVisibleIssueByKey(issueKey);
        
        if (!issueOptional.isPresent())
            return new ResponseEntity<>("Issue not found.", NOT_FOUND);
        
        final Issue issue = issueOptional.get();
        final ZoneId zoneId = DateTimeUtils.determineTimeZoneId(timezone);
        
        Double cycleTime = issue.getCycleTime(zoneId).orElse(null);
        List<Transition> transitions = null;
        
        try {
            transitions = issueBufferService.transitions(issueKey);
        } catch (PermissaoNegadaException e) {
            LOG.debug("Could not fetch transitions", e);
        }
        
        JiraTimeTrackingDto jiraTimeTraking = getJiraTimeTraking(issue);
        
        IssueDetailDto detailDto = new IssueDetailDto(cycleTime, transitions, jiraTimeTraking);
        return ResponseEntity.ok(detailDto);
    }

    private JiraTimeTrackingDto getJiraTimeTraking(final Issue issue) {
        Integer timeEstimateMinutes = 0;
        Integer timeSpentMinutes = 0;

        final TaskboardTimeTracking timeTracking = issue.getTimeTracking();

        timeEstimateMinutes += timeTracking.getOriginalEstimateMinutes().orElse(0);
        timeSpentMinutes +=  timeTracking.getTimeSpentMinutes().orElse(0);

        for (final Issue subTaskJira : issue.getSubtaskCards()) {
            final TaskboardTimeTracking tracking = subTaskJira.getTimeTracking();
            
            timeEstimateMinutes += tracking.getOriginalEstimateMinutes().orElse(0);
            timeSpentMinutes +=  tracking.getTimeSpentMinutes().orElse(0);
        }

        return new JiraTimeTrackingDto(timeEstimateMinutes, timeSpentMinutes);
    }

    public static class IssueDetailDto {

        public Double cycleTime;
        public List<Transition> transitions;
        public JiraTimeTrackingDto jiraTimeTracking;

        public IssueDetailDto(Double cycleTime, List<Transition> transitions, JiraTimeTrackingDto jiraTimeTracking) {
            super();
            this.cycleTime = cycleTime;
            this.transitions = transitions;
            this.jiraTimeTracking = jiraTimeTracking;
        }
    }
}

