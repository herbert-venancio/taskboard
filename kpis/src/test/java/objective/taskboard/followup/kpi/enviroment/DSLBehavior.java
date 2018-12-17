package objective.taskboard.followup.kpi.enviroment;

import objective.taskboard.followup.kpi.IssueKpi;

public interface DSLBehavior {
    public void execute(KpiEnvironment environment, IssueKpi issueKpi);
}
