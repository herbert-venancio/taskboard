package objective.taskboard.followup.kpi.touchTime;

import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.DateTimeUtils;

@Service
class TouchTimeKPIDataProvider implements TouchTimeProvider<TouchTimeChartDataSet>{

    private IssueKpiService issueKpiService;

    private JiraProperties jiraProperties;

    private KPIProperties kpiProperties;

    @Autowired
    public TouchTimeKPIDataProvider(IssueKpiService issueKpiService, JiraProperties jiraProperties, KPIProperties kpiProperties) {
        this.issueKpiService = issueKpiService;
        this.jiraProperties = jiraProperties;
        this.kpiProperties = kpiProperties;
    }

    @Override
    public TouchTimeChartDataSet getDataSet(String projectKey, KpiLevel kpiLevel, ZoneId timezone) {
        final List<IssueKpi> issues = issueKpiService.getIssuesFromCurrentState(projectKey, timezone, kpiLevel);
        final List<String> allProgressingStatuses = kpiProperties.getProgressingStatuses();
        final List<String> statuses = kpiLevel.filterProgressingStatuses(allProgressingStatuses, jiraProperties);
        final List<TouchTimeDataPoint> points = transformToDataPoints(issues, statuses);
        return new TouchTimeChartDataSet(points);
    }

    private List<TouchTimeDataPoint> transformToDataPoints(List<IssueKpi> issues, List<String> statuses) {
        final List<TouchTimeDataPoint> points = new LinkedList<>();
        issues.stream().forEach(issue -> {
            statuses.forEach(status -> {
                final Long effortInSeconds = issue.getEffort(status);
                final double effortInHours = DateTimeUtils.secondsToHours(effortInSeconds);
                final TouchTimeDataPoint dataPoint = new TouchTimeDataPoint(issue.getIssueKey(),
                        issue.getIssueTypeName(), status, effortInHours);
                points.add(dataPoint);
            });
        });
        return points;
    }
}
