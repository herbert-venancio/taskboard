package objective.taskboard.followup.kpi.services;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapter;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapterFactory;
import objective.taskboard.followup.kpi.transformer.IssueKpiTransformer;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.Clock;

@Service
class IssueKpiService {

    private IssueBufferService issueBufferService;
    private KPIProperties kpiProperties;
    private Clock clock;
    private IssueKpiDataItemAdapterFactory factory;

    @Autowired
    IssueKpiService(
            IssueBufferService issueBufferService,
            JiraProperties jiraProperties,
            KPIProperties kpiProperties,
            Clock clock,
            IssueKpiDataItemAdapterFactory factory) {
        this.issueBufferService = issueBufferService;
        this.kpiProperties = kpiProperties;
        this.clock = clock;
        this.factory = factory;
    }

    List<IssueKpi> getIssuesFromCurrentState(String projectKey, ZoneId timezone){

        List<Issue> issuesVisibleToUser = issueBufferService.getAllIssues().stream()
                .filter(issue -> projectKey.equals(issue.getProjectKey()))
                .collect(Collectors.toList());

        List<IssueKpiDataItemAdapter> items = factory.getItems(issuesVisibleToUser,timezone);
        return new IssueKpiTransformer(kpiProperties, clock)
                                        .withItems(items)
                                        .withOriginalIssues(issuesVisibleToUser)
                                        .mappingHierarchically()
                                        .settingWorklogWithTimezone(timezone)
                                        .transform();
    }

    List<IssueKpi> getIssues(Optional<AnalyticsTransitionsDataSet> analyticSet){
        List<IssueKpiDataItemAdapter> items = factory.getItems(analyticSet);
        return new IssueKpiTransformer(kpiProperties, clock).withItems(items).transform();
    }

}
