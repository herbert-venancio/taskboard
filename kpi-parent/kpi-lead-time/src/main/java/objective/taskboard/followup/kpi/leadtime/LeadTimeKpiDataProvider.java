package objective.taskboard.followup.kpi.leadtime;

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
import objective.taskboard.followup.kpi.KpiDataService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KpiLeadTimeProperties;
import objective.taskboard.followup.kpi.properties.LeadTimeProperties;

@Service
public class LeadTimeKpiDataProvider {

    private KpiDataService kpiDataService;
    private Map<KpiLevel, Set<String>> leadStatusesByLevel = new EnumMap<>(KpiLevel.class);

    @Autowired
    public LeadTimeKpiDataProvider(KpiDataService kpiDataService, KpiLeadTimeProperties kpiProperties) {
        this.kpiDataService = kpiDataService;
        mapLeadPropertiesByLevel(kpiProperties.getLeadTime());
    }

    public List<LeadTimeKpi> getDataSet(String projectKey, KpiLevel kpiLevel, ZoneId timezone) {
        List<IssueKpi> issues = kpiDataService.getIssuesFromCurrentState(projectKey, timezone, kpiLevel);
        LeadTimeKpiFactory factory = new LeadTimeKpiFactory(leadStatusesByLevel);
        return issues.stream()
                .filter(i -> i.hasCompletedCycle(leadStatusesByLevel.get(i.getLevel())))
                .map(factory::create)
                .collect(Collectors.toList());
    }

    private void mapLeadPropertiesByLevel(LeadTimeProperties leadTimeProperties) {
        leadStatusesByLevel.put(KpiLevel.DEMAND, new HashSet<>(leadTimeProperties.getDemands()));
        leadStatusesByLevel.put(KpiLevel.FEATURES, new HashSet<>(leadTimeProperties.getFeatures()));
        leadStatusesByLevel.put(KpiLevel.SUBTASKS, new HashSet<>(leadTimeProperties.getSubtasks()));
        leadStatusesByLevel.put(KpiLevel.UNMAPPED, Collections.emptySet());
    }
}
