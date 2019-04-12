package objective.taskboard.followup.kpi.filters;

import java.util.function.Predicate;

import objective.taskboard.followup.kpi.IssueKpi;

public interface KpiItemFilter extends Predicate<IssueKpi>{
   
    default KpiItemFilter concat(KpiItemFilter other) {
        return issue -> test(issue) && other.test(issue);
    }
    
}
