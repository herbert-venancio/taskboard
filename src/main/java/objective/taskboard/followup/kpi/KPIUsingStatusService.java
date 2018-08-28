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

import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.FollowUpData;
import objective.taskboard.followup.FollowUpTransitionsDataProvider;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.StatusConfiguration;

public abstract class KPIUsingStatusService<DS,R> {
    
    @Autowired
    protected JiraProperties jiraProperties;
    
    @Autowired
    private IssueStatusFlowService issueStatusFlowService;
    
    public List<DS> getData(FollowUpData followupData) {
        StatusConfiguration configuration = getConfiguration();

        List<DS> ds = new LinkedList<>();
        List<AnalyticsTransitionsDataSet> dataSets = followupData.analyticsTransitionsDsList;
        
        List<R> rowsDemand = getRows(configuration.getDemands(),findAnalyticTransitionDs(dataSets, TYPE_DEMAND));
        List<R> rowsFeature = getRows(configuration.getTasks(),findAnalyticTransitionDs(dataSets, TYPE_FEATURES));
        List<R> rowsSubtasks = getRows(configuration.getSubtasks(),findAnalyticTransitionDs(dataSets, TYPE_SUBTASKS));

        ds.add(makeDataSet(TYPE_DEMAND, rowsDemand));
        ds.add(makeDataSet(TYPE_FEATURES, rowsFeature));
        ds.add(makeDataSet(TYPE_SUBTASKS, rowsSubtasks));

        return ds;
    }
    
    protected abstract StatusConfiguration getConfiguration();

    protected abstract DS makeDataSet(String type, List<R> rows);
    
    protected abstract List<R> makeRows(String[] statuses, Map<String, List<IssueStatusFlow>> byType, ZonedDateTime date);
    
    private Optional<AnalyticsTransitionsDataSet> findAnalyticTransitionDs(List<AnalyticsTransitionsDataSet> dataSets, String type) {
        if(isEmpty(dataSets))
            return Optional.empty();
        return dataSets.stream().filter(a -> type.equals(a.issueType)).findFirst();
    }
    
    private Map<String, List<IssueStatusFlow>> getIssuesByType(List<IssueStatusFlow> issues) {
        return issues.stream().collect(Collectors.groupingBy(IssueStatusFlow::getIssueType));
    }
    
    private List<R> getRows(String[] statuses, Optional<AnalyticsTransitionsDataSet> analyticTransitionDs) {
        if (!analyticTransitionDs.isPresent())
            return Arrays.asList();
        AnalyticsTransitionsDataSet ds = analyticTransitionDs.get();

        List<IssueStatusFlow> issues = issueStatusFlowService.getIssues(ds);

        if (issues.isEmpty())
            return Arrays.asList();

        Map<String, List<IssueStatusFlow>> byType = getIssuesByType(issues);
        
        List<R> rows = new LinkedList<>();
        Range<ZonedDateTime> dateRange = FollowUpTransitionsDataProvider.calculateInterval(ds);

        for (ZonedDateTime date = dateRange.getMinimum(); !date.isAfter(dateRange.getMaximum()); date = date.plusDays(1)) {
            List<R> tpRows = makeRows(statuses, byType, date);
            rows.addAll(tpRows);
        }

        return rows;
    }

}
