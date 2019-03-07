package objective.taskboard.followup.kpi.touchtime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.utils.DateTimeUtils;

public class TouchTimeByIssueGroupedByProgressingStatusesKpiStrategy extends TouchTimeByIssueKpiStrategy {

    public TouchTimeByIssueGroupedByProgressingStatusesKpiStrategy(
            ZoneId timezone,
            List<IssueKpi> issues,
            List<String> progressingStatuses) {
        super(timezone, issues, progressingStatuses);
    }

    @Override
    protected List<TouchTimeByIssueKpiDataPoint> getDataPoints() {
        return issues.stream().map(this::transform).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private TouchTimeByIssueKpiDataPoint transform(IssueKpi issue) {
        Optional<Range<LocalDate>> opDateRange = issue.getDateRangeBasedOnProgressingStatuses(timezone);
        if (!opDateRange.isPresent()) {
            return null;
        }
        final Range<LocalDate> progressingRange = opDateRange.get();
        final ZonedDateTime startProgressingDate = progressingRange.getMinimum().atStartOfDay(timezone);
        final ZonedDateTime endProgressingDate = progressingRange.getMaximum().atStartOfDay(timezone);
        List<TouchTimeByIssueKpiDataPoint.Stack> stacks = getStacks(issue);
        return new TouchTimeByIssueKpiDataPoint(
                issue.getIssueKey(),
                issue.getIssueTypeName(),
                startProgressingDate.toInstant(),
                endProgressingDate.toInstant(),
                stacks);
    }

    private List<TouchTimeByIssueKpiDataPoint.Stack> getStacks(IssueKpi issue) {
        return progressingStatuses.stream()
                .map(s -> this.getStack(issue, s))
                .collect(Collectors.toList());
    }

    private TouchTimeByIssueKpiDataPoint.Stack getStack(IssueKpi issue, String status) {
        final Long effortInSeconds = issue.getEffort(status);
        final double effortInHours = DateTimeUtils.secondsToHours(effortInSeconds);
        return new TouchTimeByIssueKpiDataPoint.Stack(status, effortInHours);
    }
}
