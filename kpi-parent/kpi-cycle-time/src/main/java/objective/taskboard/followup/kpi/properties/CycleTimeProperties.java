package objective.taskboard.followup.kpi.properties;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import objective.taskboard.followup.kpi.KpiLevel;

public class CycleTimeProperties {

    @NotNull
    @NotEmpty
    public List<String> demands;
    @NotNull
    @NotEmpty
    public List<String> features;
    @NotNull
    @NotEmpty
    public List<String> subtasks;
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
        Map<KpiLevel, Set<String>> cycleStatusesByLevel = new EnumMap<>(KpiLevel.class);
        cycleStatusesByLevel.put(KpiLevel.DEMAND, new HashSet<>(getDemands()));
        cycleStatusesByLevel.put(KpiLevel.FEATURES, new HashSet<>(getFeatures()));
        cycleStatusesByLevel.put(KpiLevel.SUBTASKS, new HashSet<>(getSubtasks()));
        cycleStatusesByLevel.put(KpiLevel.UNMAPPED, Collections.emptySet());
        return cycleStatusesByLevel;
    }

}
