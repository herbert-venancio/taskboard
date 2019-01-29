package objective.taskboard.followup.kpi.cycletime;

import java.time.ZoneId;
import java.util.Map;
import java.util.Set;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;

public class CycleTimeKpiFactory {
    private Map<KpiLevel, Set<String>> cycleStatusesByLevel;
    private ZoneId timezone;

    public CycleTimeKpiFactory(Map<KpiLevel, Set<String>> cycleStatusesByLevel, ZoneId timezone) {
        this.timezone = timezone;
        this.cycleStatusesByLevel = cycleStatusesByLevel;
    }

    public CycleTimeKpi create(IssueKpi issue) {
        Set<String> cycleStatuses = cycleStatusesByLevel.get(issue.getLevel());
        return new CycleTimeKpi(issue.getIssueKey(), issue.getIssueTypeName(), issue.getSubCycles(cycleStatuses, timezone));
    }

}
