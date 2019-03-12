package objective.taskboard.followup.kpi;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.Range;

public class WithinRangeFilter implements Predicate<IssueKpi>{

    private ProjectTimelineRange rangeCalculationStrategy;
    private ZoneId timezone;
    
    public WithinRangeFilter(ZoneId timezone, ProjectTimelineRange range) {
        this.rangeCalculationStrategy = range;
        this.timezone = timezone;
    }

    @Override
    public boolean test(IssueKpi issue) {
        Optional<Range<LocalDate>> opRange = getIssueDateRange(issue);
        return opRange.map(issueRange -> this.rangeCalculationStrategy.isWithinRange(issueRange)).orElse(false);
    }

    private Optional<Range<LocalDate>> getIssueDateRange(IssueKpi issue) {
        return issue.getDateRangeBasedOnProgressingStatuses(timezone);
    }

}
