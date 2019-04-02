package objective.taskboard.followup.kpi.services.snapshot;

import static java.util.Arrays.asList;
import static objective.taskboard.followup.kpi.KpiLevel.DEMAND;
import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static objective.taskboard.followup.kpi.KpiLevel.UNMAPPED;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import objective.taskboard.followup.AnalyticsTransitionsDataRow;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.services.IssueKpiMocker;
import objective.taskboard.followup.kpi.services.KpiEnvironment;
import objective.taskboard.jira.properties.JiraProperties;

public class AnalyticsDataSetsGenerator {
    
    private KpiEnvironment environment;
    private Map<KpiLevel,AnalyticsTransitionsDataSet> datasets = new EnumMap<>(KpiLevel.class);
    
    public AnalyticsDataSetsGenerator(KpiEnvironment environment) {
        this.environment = environment;
        generateDatasets();
    }
    
    public Optional<AnalyticsTransitionsDataSet> getOptionalDataSetForLevel(KpiLevel level) {
        return Optional.ofNullable(datasets.get(level));
    }

    public List<AnalyticsTransitionsDataSet> getAnalyticDataSets() {
        return Arrays.asList(getDs(DEMAND), getDs(FEATURES), getDs(SUBTASKS),getDs(UNMAPPED));
    }

    private AnalyticsTransitionsDataSet getDs(KpiLevel level) {
        return datasets.get(level);
    }

    private void generateDatasets() {
        Stream.of(KpiLevel.values()).forEach(level -> generateDataSet(level,environment.getJiraProperties(),allIssuesFrom(level)));
    }

    private List<IssueKpiMocker> allIssuesFrom(KpiLevel level){
        return environment.getAllIssueMockers().stream().filter(i -> i.level() == level).collect(Collectors.toList());
    }

    private void generateDataSet(KpiLevel level, JiraProperties jiraProperties, List<IssueKpiMocker> list) {
        List<String> headers = new LinkedList<>();
        headers.add("PKEY");
        headers.add("ISSUE_TYPE");
        headers.addAll(getHeaders(level, jiraProperties));

        List<AnalyticsTransitionsDataRow> rows = list.stream().map(this::buildRow).collect(Collectors.toList()); 

        AnalyticsTransitionsDataSet ds = new AnalyticsTransitionsDataSet(level.getName(),headers,rows);
        datasets.put(level, ds);
    }

    private List<String> getHeaders(KpiLevel level, JiraProperties jiraProperties) {
        return asList(level.getStatusPriorityOrder(jiraProperties));
    }

    private AnalyticsTransitionsDataRow buildRow(IssueKpiMocker issue) {
        String issueKey = issue.getIssueKey();
        String issueType = issue.getIssueTypeKpi().map(IssueTypeKpi::getType).orElse("UNMAPPED");
        List<ZonedDateTime> lastTransitionDate = new LinkedList<>(issue.getReversedTransitions().values());

        return new AnalyticsTransitionsDataRow(issueKey,issueType,lastTransitionDate);
    }

}
