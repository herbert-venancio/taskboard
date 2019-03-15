package objective.taskboard.followup.kpi.touchtime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.utils.DateTimeUtils;

public class TouchTimeByIssueGroupedBySubtaskTypeKpiStrategy extends TouchTimeByIssueKpiStrategy {

    private Set<JiraIssueTypeDto> subtaskTypes;

    public TouchTimeByIssueGroupedBySubtaskTypeKpiStrategy(
            ZoneId timezone,
            List<IssueKpi> issues,
            List<String> progressingStatuses,
            Set<JiraIssueTypeDto> subtaskTypes) {
        super(timezone, issues, progressingStatuses);
        this.subtaskTypes = subtaskTypes;
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
        return subtaskTypes.stream().map(t -> this.getStack(issue, t)).collect(Collectors.toList());
    }

    private TouchTimeByIssueKpiDataPoint.Stack getStack(IssueKpi issue, JiraIssueTypeDto subtaskType) {
        long effortInSeconds = issue.getEffortSumFromChildrenWithSubtaskTypeId(subtaskType.getId());
        final double effortInHours = DateTimeUtils.secondsToHours(effortInSeconds);
        return new TouchTimeByIssueKpiDataPoint.Stack(subtaskType.getName(), effortInHours);
    }
}
