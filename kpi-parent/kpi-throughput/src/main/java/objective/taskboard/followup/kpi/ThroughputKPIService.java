package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import objective.taskboard.followup.ThroughputDataSet;
import objective.taskboard.followup.ThroughputRow;
import objective.taskboard.followup.kpi.services.KPIUsingStatusService;
import objective.taskboard.followup.kpi.services.KpiDataService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.StatusConfiguration;

@Service
public class ThroughputKPIService extends KPIUsingStatusService<ThroughputDataSet,ThroughputRow> {

    public ThroughputKPIService(JiraProperties jiraProperties, KpiDataService kpiDataService) {
        super(jiraProperties, kpiDataService);
    }

    @Override
    protected List<ThroughputRow> makeRows(String[] statuses, Map<String, List<IssueKpi>> issues, ZonedDateTime date) {

        return issues.entrySet().stream().sorted((e1, e2) -> e1.getKey().compareTo(e2.getKey()))
                .map(entry -> new ThroughputRow(date, entry.getKey(),countIssuesThroughput(entry.getValue(), date, statuses)))
                .collect(Collectors.toList());
    }

    @Override
    protected StatusConfiguration getConfiguration() {
        return jiraProperties.getFinalStatuses();
    }

    @Override
    protected ThroughputDataSet makeDataSet(String type, List<ThroughputRow> rows) {
        return new ThroughputDataSet(type, rows);
    }
    
    private Long countIssuesThroughput(List<IssueKpi> issues, ZonedDateTime date, String... statuses) {
        return issues.stream().filter( i -> i.hasTransitedToAnyStatusOnDay(date, statuses)).count();
    }

}
