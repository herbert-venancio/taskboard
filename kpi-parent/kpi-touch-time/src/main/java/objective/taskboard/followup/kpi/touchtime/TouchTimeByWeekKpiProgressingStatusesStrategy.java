package objective.taskboard.followup.kpi.touchtime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.utils.DateTimeUtils;

class TouchTimeByWeekKpiProgressingStatusesStrategy extends TouchTimeByWeekKpiStrategy {

    private List<String> progressingStatuses;

    protected TouchTimeByWeekKpiProgressingStatusesStrategy(
            Range<LocalDate> projectRange, ZoneId timezone, List<IssueKpi> issues, List<String> progressingStatuses) {
        super(projectRange, timezone, issues);
        this.progressingStatuses = progressingStatuses;
    }

    @Override
    protected List<TouchTimeByWeekKpiDataPoint> getDataPoints() {
        return super.issuesByWeek.entrySet().stream()
                .sorted(Comparator.comparing(Entry::getKey))
                .map(this::transformToDataPoints)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<TouchTimeByWeekKpiDataPoint> transformToDataPoints(Entry<TouchTimeKpiWeekRange, List<IssueKpi>> entry) {
        TouchTimeKpiWeekRange week = entry.getKey();
        List<IssueKpi> issues = entry.getValue();
        return progressingStatuses.stream()
            .map(status -> new TouchTimeByWeekKpiDataPoint(
                    week.getFirstDay().toInstant(),
                    status,
                    getWeekAvgEffortForStatusFromIssues(week, status, issues)))
            .collect(Collectors.toList());
    }

    private double getWeekAvgEffortForStatusFromIssues(TouchTimeKpiWeekRange week, String status, List<IssueKpi> issues) {
        return issues.stream()
            .map(i -> i.getEffortFromStatusUntilDate(status, week.getLastDay()))
            .mapToDouble(DateTimeUtils::secondsToHours)
            .average().orElse(0D);
    }
}
