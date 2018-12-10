package objective.taskboard.followup.kpi.touchTime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.Range;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.ProjectTimelineRange;
import objective.taskboard.utils.Clock;

public class TouchTimeFilter implements Predicate<IssueKpi>{

    private ProjectTimelineRange rangeCalculationStrategy;
    private Clock clock;
    private ZoneId timezone;
    
    public TouchTimeFilter(Clock clock, ZoneId timezone, ProjectTimelineRange range) {
        this.clock = clock;
        this.rangeCalculationStrategy = range;
        this.timezone = timezone;
    }

    @Override
    public boolean test(IssueKpi issue) {
        Optional<Range<LocalDate>> opRange = getIssueDateRange(issue);
        return opRange.map(issueRange -> this.rangeCalculationStrategy.isWithinRange(issueRange)).orElse(false);
    }

    private Optional<Range<LocalDate>> getIssueDateRange(IssueKpi issue) {
        return issue.getDateRangeBasedOnProgressingStatuses(clock, timezone);
    }
    
    

}
