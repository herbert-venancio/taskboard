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
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.properties.KpiLeadTimeProperties;

@Service
public class LeadTimeKpiDataProvider {

    private IssueKpiService issueKpiService;
    private Map<KpiLevel, Set<String>> leadStatusesByLevel = new EnumMap<>(KpiLevel.class);

    @Autowired
    public LeadTimeKpiDataProvider(IssueKpiService issueKpiService, KPIProperties kpiProperties) {
        this.issueKpiService = issueKpiService;
        initMap(kpiProperties.getLeadTime());
    }

    public List<LeadTimeKpi> getDataSet(String projectKey, KpiLevel kpiLevel, ZoneId timezone) {
        List<IssueKpi> issues = issueKpiService.getIssuesFromCurrentState(projectKey, timezone, kpiLevel);
        LeadTimeKpiFactory factory = new LeadTimeKpiFactory(leadStatusesByLevel, timezone);
        return issues.stream()
                .filter(i -> i.hasCompletedCycle(leadStatusesByLevel.get(i.getLevel()), timezone))
                .map(factory::create)
                .collect(Collectors.toList());
    }

    private void initMap(KpiLeadTimeProperties leadTimeProperties) {
        leadStatusesByLevel.put(KpiLevel.DEMAND, new HashSet<>(leadTimeProperties.getDemands()));
        leadStatusesByLevel.put(KpiLevel.FEATURES, new HashSet<>(leadTimeProperties.getFeatures()));
        leadStatusesByLevel.put(KpiLevel.SUBTASKS, new HashSet<>(leadTimeProperties.getSubtasks()));
        leadStatusesByLevel.put(KpiLevel.UNMAPPED, Collections.emptySet());
    }
}
