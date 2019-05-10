package objective.taskboard.followup.kpi.bugbyenvironment.filters;

import java.util.List;
import java.util.function.Predicate;

import objective.taskboard.followup.kpi.IssueKpi;

public class FilterOnlyBugs implements Predicate<IssueKpi>{

    private final List<Long> bugTypes;
    
    public FilterOnlyBugs(List<Long> bugTypes) {
        this.bugTypes = bugTypes;
    }

    @Override
    public boolean test(IssueKpi issue) {
        return issue.getIssueType().map(type -> bugTypes.contains(type.getId())).orElse(false);
    }

}
