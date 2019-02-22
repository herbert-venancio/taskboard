package objective.taskboard.followup.kpi.touchtime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.DateTimeUtils;

@Service
class TouchTimeByIssueKpiDataProvider implements TouchTimeKpiDataProvider<TouchTimeByIssueKpiDataSet>{

    private IssueKpiService issueKpiService;

    private JiraProperties jiraProperties;

    private KPIProperties kpiProperties;

    @Autowired
    public TouchTimeByIssueKpiDataProvider(IssueKpiService issueKpiService, JiraProperties jiraProperties, KPIProperties kpiProperties) {
        this.issueKpiService = issueKpiService;
        this.jiraProperties = jiraProperties;
        this.kpiProperties = kpiProperties;
    }

    @Override
    public TouchTimeByIssueKpiDataSet getDataSet(String projectKey, KpiLevel kpiLevel, ZoneId timezone) {
        final List<IssueKpi> issues = issueKpiService.getIssuesFromCurrentState(projectKey, timezone, kpiLevel);
        final List<String> allProgressingStatuses = kpiProperties.getProgressingStatuses();
        final List<String> statuses = kpiLevel.filterProgressingStatuses(allProgressingStatuses, jiraProperties);
        final List<TouchTimeByIssueKpiDataPoint> points = transformToDataPoints(issues, statuses, timezone);
        return new TouchTimeByIssueKpiDataSet(points);
    }

    private List<TouchTimeByIssueKpiDataPoint> transformToDataPoints(List<IssueKpi> issues, List<String> statuses, ZoneId timezone) {
        final List<TouchTimeByIssueKpiDataPoint> points = new LinkedList<>();
        issues.forEach(issue -> {
            statuses.forEach(status -> {
                final Long effortInSeconds = issue.getEffort(status);
                final double effortInHours = DateTimeUtils.secondsToHours(effortInSeconds);
                final Range<LocalDate> progressingRange = issue.getDateRangeBasedOnProgressingStatuses(timezone).get();
                final ZonedDateTime startProgressingDate = progressingRange.getMinimum().atStartOfDay(timezone);
                final ZonedDateTime endProgressingDate = progressingRange.getMaximum().atStartOfDay(timezone);
                final TouchTimeByIssueKpiDataPoint dataPoint = new TouchTimeByIssueKpiDataPoint(
                        issue.getIssueKey(),
                        issue.getIssueTypeName(),
                        status,
                        effortInHours,
                        startProgressingDate.toInstant(),
                        endProgressingDate.toInstant());
                points.add(dataPoint);
            });
        });
        return points;
    }
}
