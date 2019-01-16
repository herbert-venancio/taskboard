package objective.taskboard.followup.kpi.enviroment;

import static java.util.Arrays.asList;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import objective.taskboard.followup.AnalyticsTransitionsDataRow;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.jira.properties.JiraProperties;

public class GenerateAnalyticsDataSets {
    
    private KpiEnvironment environment;
    private Map<KpiLevel,AnalyticsTransitionsDataSet> datasets = new EnumMap<>(KpiLevel.class);
    
    public GenerateAnalyticsDataSets(KpiEnvironment environment) {
        this.environment = environment;
        buildsDatasets();
    }

    private void buildsDatasets() {
        Stream.of(KpiLevel.values()).forEach(level -> buildDataSet(level,environment.getJiraProperties(),allIssuesFrom(level)));
    }
    
    private List<IssueKpiMocker> allIssuesFrom(KpiLevel level){
        return environment.getAllIssueMockers().stream().filter(i -> i.level() == level).collect(Collectors.toList());
    }
    

    private void buildDataSet(KpiLevel level, JiraProperties jiraProperties, List<IssueKpiMocker> list) {
        List<String> headers = new LinkedList<>();
        headers.add("PKEY");
        headers.add("ISSUE_TYPE");
        headers.addAll(getHeaders(level, jiraProperties));

        List<AnalyticsTransitionsDataRow> rows = list.stream().map(i -> buildRow(i)).collect(Collectors.toList()); 
        
        AnalyticsTransitionsDataSet ds = new AnalyticsTransitionsDataSet(level.getName(),headers,rows);
        datasets.put(level, ds);
    }

    private List<String> getHeaders(KpiLevel level, JiraProperties jiraProperties) {
        KpiLevel levelToConsult = level;// == KpiLevel.UNMAPPED ? KpiLevel.SUBTASKS : level;
        return asList(levelToConsult.getStatusPriorityOrder(jiraProperties));
    }

    private AnalyticsTransitionsDataRow buildRow(IssueKpiMocker issue) {
        String issueKey = issue.getIssueKey();
        String issueType = issue.getIssueTypeKpi().map(t -> t.getType()).orElse("UNMAPPED");
        List<ZonedDateTime> lastTransitionDate = new LinkedList<>(issue.getReversedTransitions().values());
        
        return new AnalyticsTransitionsDataRow(issueKey,issueType,lastTransitionDate);
    }

    public Optional<AnalyticsTransitionsDataSet> getOptionalDataSetForLevel(KpiLevel level) {
        return Optional.ofNullable(datasets.get(level));
    }


}
