package objective.taskboard.followup;

import static org.apache.commons.lang3.ArrayUtils.INDEX_NOT_FOUND;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import objective.taskboard.data.Changelog;
import objective.taskboard.data.Issue;
import objective.taskboard.utils.DateTimeUtils;

@Service
public class IssueTransitionService {
    
    private static final Logger LOG = LoggerFactory.getLogger(IssueTransitionService.class);
    
    public Map<String, ZonedDateTime> getTransitions(Issue issue, ZoneId timezone,String[] statuses) {
        Map<String, ZonedDateTime> lastTransitionDateByStatus = new LinkedHashMap<>();
        
        if(statuses.length == 0)
            return lastTransitionDateByStatus;
        
        final String firstState = statuses[statuses.length - 1];
        for (String status : statuses)
            lastTransitionDateByStatus.put(status, null);
        lastTransitionDateByStatus.put(firstState, DateTimeUtils.get(issue.getCreated(), timezone));
        int lastStatusIndex = ArrayUtils.indexOf(statuses, issue.getStatusName());
        
        for (Changelog change : issue.getChangelog()) {
            if (!"status".equals(change.field))
                continue;
            
            int statusIndex = ArrayUtils.indexOf(statuses, change.to);
            
            if (lastStatusIndex != INDEX_NOT_FOUND && statusIndex >= lastStatusIndex) {

                if (change.timestamp == null) {
                    logWarning(issue);
                    continue;
                }
                
                lastTransitionDateByStatus.put(change.to, DateTimeUtils.get(change.timestamp, timezone));
                
            }
        }
        
        return lastTransitionDateByStatus;
    }

    private void logWarning(Issue issue) {
        StringBuilder history = new StringBuilder();
        issue.getChangelog().forEach(history::append);
        
        String warningMessage = String.format("Issue with transition date/hour without fill, Issue: %s, %s.", issue.getIssueKey(), history.toString());
        LOG.warn(warningMessage);
    }
    
}
