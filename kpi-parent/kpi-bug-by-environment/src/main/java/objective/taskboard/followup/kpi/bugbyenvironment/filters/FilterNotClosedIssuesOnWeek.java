package objective.taskboard.followup.kpi.bugbyenvironment.filters;

import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Predicate;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.filters.KpiWeekRange;

public class FilterNotClosedIssuesOnWeek implements Predicate<IssueKpi>{

    private KpiWeekRange weekRange;

    public FilterNotClosedIssuesOnWeek(KpiWeekRange weekRange) {
        this.weekRange = weekRange;
    }

    @Override
    public boolean test(IssueKpi issue) {
        if(openedAfterWeek(issue))
            return false;
        
        return openedDuringWeek(issue) || endOfProgressingRangeIsOnWeekOrAfter(issue);
    }

    private boolean endOfProgressingRangeIsOnWeekOrAfter(IssueKpi issue) {
        Optional<LocalDate> endDate = issue.dateAfterLeavingLastProgressingStatus();
        
        return endDate.map(date -> weekRange.contains(date) || date.isAfter(weekRange.getLastDay().toLocalDate())).orElse(true);
    }
    
    private boolean openedAfterWeek(IssueKpi issue) {
        return issue.getStatusChain().getMinimumDate().map( date -> date.isAfter(weekRange.getLastDay())).orElse(true);
    }

    private boolean openedDuringWeek(IssueKpi issue) {
        return issue.getStatusChain().getMinimumDate().map(date -> weekRange.contains(date.toLocalDate())).orElse(false);
    }
    

}
