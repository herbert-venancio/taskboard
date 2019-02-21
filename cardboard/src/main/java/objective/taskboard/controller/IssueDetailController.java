package objective.taskboard.controller;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.data.Issue;
import objective.taskboard.data.TaskboardTimeTracking;
import objective.taskboard.issue.CardStatusOrderCalculator;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.client.JiraTimeTrackingDto;
import objective.taskboard.jira.data.FieldsRequiredInTransition;
import objective.taskboard.jira.data.Transition;
import objective.taskboard.utils.DateTimeUtils;

@RestController
@RequestMapping("/ws/issue-detail")
public class IssueDetailController {

    @Autowired
    private IssueBufferService issueBufferService;

    @Autowired
    private JiraService jiraService;

    @Autowired
    private CardStatusOrderCalculator statusOrderCalculator;

    @GetMapping("/{issueKey}")
    public ResponseEntity<Object> findByKey(@PathVariable final String issueKey, @RequestParam("timezone") final String timezone,
                                            @RequestParam(required = false, defaultValue ="true") boolean onlyVisible) {

        final Optional<Issue> issueOptional = issueBufferService.getIssueByKey(issueKey, onlyVisible);
        
        if (!issueOptional.isPresent())
            return new ResponseEntity<>("Issue not found.", NOT_FOUND);
        
        final Issue issue = issueOptional.get();
        final ZoneId zoneId = DateTimeUtils.determineTimeZoneId(timezone);
        
        Double cycleTime = issue.getCycleTime(zoneId).orElse(null);

        List<TransitionDto> transitionDtos = getTransitions(issue);

        JiraTimeTrackingDto jiraTimeTraking = getJiraTimeTraking(issue);
        
        IssueDetailDto detailDto = new IssueDetailDto(cycleTime, transitionDtos, jiraTimeTraking);
        return ResponseEntity.ok(detailDto);
    }

    private List<TransitionDto> getTransitions(Issue issue) {
        List<Transition> transitions = jiraService.getTransitions(issue.getIssueKey());

        List<Long> transitionIds = transitions.stream()
                .map(t -> t.id)
                .collect(toList());

        Map<Long, FieldsRequiredInTransition> fieldsRequiredInTransitions = transitionIds.isEmpty()
                ? Collections.emptyMap()
                : jiraService.getFieldsRequiredInTransitions(issue.getIssueKey(), transitionIds).stream()
                    .collect(toMap(t -> t.id, t -> t));

        return transitions.stream()
                .map(t -> TransitionDto.from(t, statusOrderCalculator.computeStatusOrder(issue.getType(), t.to.id), fieldsRequiredInTransitions))
                .collect(toList());
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

        public final Double cycleTime;
        public final List<TransitionDto> transitions;
        public final JiraTimeTrackingDto jiraTimeTracking;

        public IssueDetailDto(Double cycleTime, List<TransitionDto> transitions, JiraTimeTrackingDto jiraTimeTracking) {
            super();
            this.cycleTime = cycleTime;
            this.transitions = transitions;
            this.jiraTimeTracking = jiraTimeTracking;
        }
    }
}

