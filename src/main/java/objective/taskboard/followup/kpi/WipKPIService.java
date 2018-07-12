package objective.taskboard.followup.kpi;

import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_DEMAND;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_FEATURES;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_SUBTASKS;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.FollowUpData;
import objective.taskboard.followup.FollowUpTransitionsDataProvider;
import objective.taskboard.followup.WipDataSet;
import objective.taskboard.followup.WipRow;
import objective.taskboard.jira.JiraProperties;

@Service
public class WipKPIService {

    @Autowired
    private JiraProperties jiraProperties;

    public List<WipDataSet> getWipData(FollowUpData followupData){
        
        List<WipDataSet> wipDs = new LinkedList<>();
        
        List<AnalyticsTransitionsDataSet> dataSets = followupData.analyticsTransitionsDsList;
        List<WipRow> rowsDemand = getRows(jiraProperties.getStatusCountingOnWip().getDemands(), findAnalyticTransitionDs(dataSets,TYPE_DEMAND));
        List<WipRow> rowsFeature = getRows(jiraProperties.getStatusCountingOnWip().getTasks(), findAnalyticTransitionDs(dataSets,TYPE_FEATURES));
        List<WipRow> rowsSubtasks = getRows(jiraProperties.getStatusCountingOnWip().getSubtasks(), findAnalyticTransitionDs(dataSets,TYPE_SUBTASKS));
        
        
        wipDs.add(new WipDataSet(TYPE_DEMAND, rowsDemand));
        wipDs.add(new WipDataSet(TYPE_FEATURES, rowsFeature));
        wipDs.add(new WipDataSet(TYPE_SUBTASKS, rowsSubtasks));
        
        return wipDs;
    }
    
    private Optional<AnalyticsTransitionsDataSet> findAnalyticTransitionDs(List<AnalyticsTransitionsDataSet> dataSets, String type) {
        if(isEmpty(dataSets))
            return Optional.empty();
        return dataSets.stream().filter(a -> type.equals(a.issueType)).findFirst();
    }
    
    private List<WipRow> getRows(String[] statuses, Optional<AnalyticsTransitionsDataSet> analyticTransitionDs) {
        if (!analyticTransitionDs.isPresent())
            return Arrays.asList();
        AnalyticsTransitionsDataSet ds = analyticTransitionDs.get();

        List<IssueStatusFlow> issues = new IssueStatusFlowFactory(ds).getIssues();
        if(issues.isEmpty())
            return Arrays.asList();

        List<WipRow> rows = new LinkedList<>();
        Range<ZonedDateTime> dateRange = FollowUpTransitionsDataProvider.calculateInterval(ds);

        for (ZonedDateTime date = dateRange.getMinimum(); !date.isAfter(dateRange.getMaximum()); date = date.plusDays(1)) {
            for (String status : statuses) {
                List<WipRow> wipRows = getRows(issues, status, date);
                rows.addAll(wipRows);
            }
        }

        return rows;
    }

    private List<WipRow> getRows(List<IssueStatusFlow> issues, String status, ZonedDateTime date) {
        
        Map<String, List<IssueStatusFlow>> byType = issues.stream().collect(Collectors.groupingBy(IssueStatusFlow::getIssueType));
        
        return byType.entrySet().stream()
                .sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .map(entry -> new WipRow(date, entry.getKey(), status, countIssuesInWip(entry.getValue(), status, date)))
                .collect(Collectors.toList());

    }

    private Long countIssuesInWip(List<IssueStatusFlow> issues, String status, ZonedDateTime date) {
        return issues.stream().filter(i -> i.isOnStatusOnDay(status, date)).count();
    }
    
}
