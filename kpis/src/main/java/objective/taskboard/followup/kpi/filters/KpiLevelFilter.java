package objective.taskboard.followup.kpi.filters;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;

public class KpiLevelFilter implements KpiItemFilter{
    
    private KpiLevel level;

    public KpiLevelFilter(KpiLevel level) {
        this.level = level;
    }

    @Override
    public boolean test(IssueKpi issue) {
        return issue.getLevel().equals(level);
    }
    
}
