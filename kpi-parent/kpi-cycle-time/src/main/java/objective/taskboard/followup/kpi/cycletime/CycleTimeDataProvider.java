package objective.taskboard.followup.kpi.cycletime;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.domain.IssueColorService;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KpiCycleTimeProperties;
import objective.taskboard.followup.kpi.services.KpiDataService;

@Service
public class CycleTimeDataProvider {

    private KpiDataService kpiService;
    private KpiCycleTimeProperties cycleTimeProperties;
    private IssueColorService colorService;

    @Autowired
    public CycleTimeDataProvider(KpiDataService kpiService, KpiCycleTimeProperties cycleTimeProperties,
            IssueColorService colorService) {
        this.kpiService = kpiService;
        this.cycleTimeProperties = cycleTimeProperties;
        this.colorService = colorService;
    }

    public List<CycleTimeKpi> getDataSet(String projectKey, KpiLevel kpiLevel, ZoneId timezone) {
        List<IssueKpi> issues = kpiService.getIssuesFromCurrentStateWithDefaultFilters(projectKey, timezone, kpiLevel);
        Map<KpiLevel, Set<String>> cycleStatusesByLevel = cycleTimeProperties.getCycleTime().toMap();
        CycleTimeKpiFactory factory = new CycleTimeKpiFactory(cycleStatusesByLevel, colorService);
        return issues.stream()
                .filter(i -> i.hasCompletedCycle(cycleStatusesByLevel.get(i.getLevel())))
                .map(factory::create)
                .collect(Collectors.toList());
    }

}
