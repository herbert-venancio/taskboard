package objective.taskboard.followup.kpi.touchTime;

import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        Optional<Range<ZonedDateTime>> opRange = getIssueDateRange(issue);
        return opRange.map(issueRange -> this.rangeCalculationStrategy.isWithinRange(issueRange)).orElse(false);
    }

    private Optional<Range<ZonedDateTime>> getIssueDateRange(IssueKpi issue) {
        return issue.getDateRangeBasedOnProgressinsStatuses(clock, timezone);
    }
    
    

}
