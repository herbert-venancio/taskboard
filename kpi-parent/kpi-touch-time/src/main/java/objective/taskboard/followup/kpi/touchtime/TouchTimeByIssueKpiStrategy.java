package objective.taskboard.followup.kpi.touchtime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.Range;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.utils.DateTimeUtils;

public class TouchTimeByIssueKpiStrategy implements TouchTimeKpiStrategy<TouchTimeByIssueKpiDataPoint> {

    private ZoneId timezone;
    private List<IssueKpi> issues;
    private List<String> progressingStatuses;

    public TouchTimeByIssueKpiStrategy(
            ZoneId timezone, List<IssueKpi> issues, List<String> progressingStatuses) {
        this.timezone = timezone;
        this.issues = issues;
        this.progressingStatuses = progressingStatuses;
    }

    @Override
    public List<TouchTimeByIssueKpiDataPoint> getDataSet() {
        if (this.issues.isEmpty())
            return Collections.emptyList();
        return getDataPoints();
    }

    private List<TouchTimeByIssueKpiDataPoint> getDataPoints() {
        return transformToDataPoints(issues, progressingStatuses, timezone);
    }

    private List<TouchTimeByIssueKpiDataPoint> transformToDataPoints(List<IssueKpi> issues, List<String> statuses, ZoneId timezone) {
        final List<TouchTimeByIssueKpiDataPoint> points = new LinkedList<>();
        issues.forEach(issue ->
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
            })
        );
        return points;
    }

}
