package objective.taskboard.followup.kpi;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapter;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapterFactory;
import objective.taskboard.followup.kpi.transformer.IssueKpiTransformer;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.Clock;

@Service
public class IssueKpiService {

    private IssueBufferService issueBufferService;
    private JiraProperties jiraProperties;
    private KPIProperties kpiProperties;
    private Clock clock;
    private IssueKpiDataItemAdapterFactory factory;

    @Autowired
    public IssueKpiService(
            IssueBufferService issueBufferService,
            JiraProperties jiraProperties,
            KPIProperties kpiProperties,
            Clock clock,
            IssueKpiDataItemAdapterFactory factory) {
        this.issueBufferService = issueBufferService;
        this.jiraProperties = jiraProperties;
        this.kpiProperties = kpiProperties;
        this.clock = clock;
        this.factory = factory;
    }

    public List<IssueKpi> getIssuesFromCurrentState(String projectKey, ZoneId timezone, KpiLevel kpiLevel){

        List<Issue> issuesVisibleToUser = issueBufferService.getAllIssues().stream()
                .filter(new FollowupIssueFilter(jiraProperties, projectKey))
                .collect(Collectors.toList());

        List<IssueKpiDataItemAdapter> items = factory.getItems(issuesVisibleToUser,timezone);
        List<IssueKpi> issuesKpi = new IssueKpiTransformer(kpiProperties, clock)
                                        .withItems(items)
                                        .withOriginalIssues(issuesVisibleToUser)
                                        .mappingHierarchically()
                                        .settingWorklogWithTimezone(timezone)
                                        .transform();

        return issuesKpi.stream().filter(i -> i.getLevel() == kpiLevel).collect(Collectors.toList());
    }

    public List<IssueKpi> getIssues(Optional<AnalyticsTransitionsDataSet> analyticSet){
        List<IssueKpiDataItemAdapter> items = factory.getItems(analyticSet);
        return new IssueKpiTransformer(kpiProperties, clock).withItems(items).transform();
    }

}
