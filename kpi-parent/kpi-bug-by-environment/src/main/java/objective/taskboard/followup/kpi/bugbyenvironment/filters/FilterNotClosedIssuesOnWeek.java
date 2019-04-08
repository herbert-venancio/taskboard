package objective.taskboard.followup.kpi.bugbyenvironment.filters;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.function.Predicate;

import org.apache.commons.lang3.Range;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.filters.KpiWeekRange;

public class FilterNotClosedIssuesOnWeek implements Predicate<IssueKpi>{

    private KpiWeekRange weekRange;
    private ZoneId timezone;

    public FilterNotClosedIssuesOnWeek(KpiWeekRange weekRange) {
        this.weekRange = weekRange;
        this.timezone = weekRange.getTimezone();
    }

    @Override
    public boolean test(IssueKpi issue) {
        Optional<Range<LocalDate>> progressingRange = issue.getDateRangeBasedOnProgressingStatuses(timezone);
        
        return progressingRange.map(weekRange::overlaps).orElse(openedDuringWeekOrBefore(issue));
    }
    
    private boolean openedDuringWeekOrBefore(IssueKpi issue) {
        return issue.getStatusChain().getMinimumDate().map( date -> !date.isAfter(weekRange.getLastDay())).orElse(false);
    }

}
