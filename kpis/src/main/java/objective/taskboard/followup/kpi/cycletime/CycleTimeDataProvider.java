package objective.taskboard.followup.kpi.cycletime;

import java.time.ZoneId;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.properties.KpiCycleTimeProperties;

@Service
public class CycleTimeDataProvider {

    private IssueKpiService issueKpiService;
    private KPIProperties kpiProperties;

    @Autowired
    public CycleTimeDataProvider(IssueKpiService issueKpiService, KPIProperties kpiProperties) {
        this.issueKpiService = issueKpiService;
        this.kpiProperties = kpiProperties;
    }

    public List<CycleTimeKpi> getDataSet(String projectKey, KpiLevel kpiLevel, ZoneId timezone) {
        List<IssueKpi> issues = issueKpiService.getIssuesFromCurrentState(projectKey, timezone, kpiLevel);
        Map<KpiLevel, Set<String>> cycleStatusesByLevel = getCycleStatusesMapFromProperties();
        CycleTimeKpiFactory factory = new CycleTimeKpiFactory(cycleStatusesByLevel, timezone);
        return issues.stream()
                .filter(i -> i.hasCompletedCycle(cycleStatusesByLevel.get(i.getLevel()), timezone))
                .map(i -> factory.create(i))
                .collect(Collectors.toList());
    }

    private Map<KpiLevel, Set<String>> getCycleStatusesMapFromProperties() {
        KpiCycleTimeProperties cycleTimeProperties = kpiProperties.getCycleTime();
        Map<KpiLevel, Set<String>> cycleStatusesByLevel = new EnumMap<>(KpiLevel.class);
        cycleStatusesByLevel.put(KpiLevel.DEMAND, new HashSet<>(cycleTimeProperties.getDemands()));
        cycleStatusesByLevel.put(KpiLevel.FEATURES, new HashSet<>(cycleTimeProperties.getFeatures()));
        cycleStatusesByLevel.put(KpiLevel.SUBTASKS, new HashSet<>(cycleTimeProperties.getSubtasks()));
        cycleStatusesByLevel.put(KpiLevel.UNMAPPED, Collections.emptySet());
        return cycleStatusesByLevel;
    }
}
