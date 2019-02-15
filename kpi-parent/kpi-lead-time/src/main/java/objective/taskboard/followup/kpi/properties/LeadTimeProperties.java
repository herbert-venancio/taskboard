package objective.taskboard.followup.kpi.properties;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import objective.taskboard.followup.kpi.KpiLevel;

public class LeadTimeProperties {
    private List<String> demands = Collections.emptyList();
    private List<String> features = Collections.emptyList();
    private List<String> subtasks = Collections.emptyList();
    public List<String> getDemands() {
        return demands;
    }
    public void setDemands(List<String> demands) {
        this.demands = demands;
    }
    public List<String> getFeatures() {
        return features;
    }
    public void setFeatures(List<String> features) {
        this.features = features;
    }
    public List<String> getSubtasks() {
        return subtasks;
    }
    public void setSubtasks(List<String> subtasks) {
        this.subtasks = subtasks;
    }

    public Map<KpiLevel, Set<String>> toMap() {
        Map<KpiLevel, Set<String>> leadStatusMap = new EnumMap<>(KpiLevel.class);
        leadStatusMap.put(KpiLevel.DEMAND, new HashSet<>(getDemands()));
        leadStatusMap.put(KpiLevel.FEATURES, new HashSet<>(getFeatures()));
        leadStatusMap.put(KpiLevel.SUBTASKS, new HashSet<>(getSubtasks()));
        leadStatusMap.put(KpiLevel.UNMAPPED, Collections.emptySet());
        return leadStatusMap;
    }
}
