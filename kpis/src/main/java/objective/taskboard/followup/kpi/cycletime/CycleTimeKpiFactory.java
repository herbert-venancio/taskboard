package objective.taskboard.followup.kpi.cycletime;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import objective.taskboard.domain.IssueColorService;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.StatusTransition;
import objective.taskboard.followup.kpi.cycletime.CycleTimeKpi.SubCycleKpi;

public class CycleTimeKpiFactory {
    private static final String DEFAULT_COLOR = "#A1423C";
    private Map<KpiLevel, Set<String>> cycleStatusesByLevel;
    private ZoneId timezone;
    private IssueColorService colorService;

    public CycleTimeKpiFactory(Map<KpiLevel, Set<String>> cycleStatusesByLevel, ZoneId timezone, IssueColorService colorService) {
        this.timezone = timezone;
        this.cycleStatusesByLevel = cycleStatusesByLevel;
        this.colorService = colorService;
    }

    public CycleTimeKpi create(IssueKpi issue) {
        Set<String> cycleStatuses = cycleStatusesByLevel.get(issue.getLevel());
        return new CycleTimeKpi(issue.getIssueKey(), issue.getIssueTypeName(), createSubCycles(issue, cycleStatuses));
    }

    public List<SubCycleKpi> createSubCycles(IssueKpi issue, Set<String> cycleStatuses) {
        return issue.getStatusChainAsList().stream()
                .filter(s -> cycleStatuses.contains(s.getStatusName()))
                .map(s -> this.createSubCycle(s, issue))
                .collect(Collectors.toList());
    }

    private SubCycleKpi createSubCycle(StatusTransition status, IssueKpi issue) {
        Optional<IssueTypeKpi> opIssueType = issue.getIssueType();
        Optional<String> color = opIssueType.map(
                type -> colorService.getStatusColor(type.getId(), status.getStatusName()));
        return new SubCycleKpi(
                status.getStatusName(),
                status.getEnterDate(timezone),
                status.getExitDate(timezone),
                color.orElse(DEFAULT_COLOR));
    }

}
