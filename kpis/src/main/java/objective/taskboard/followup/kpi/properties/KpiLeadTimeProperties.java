package objective.taskboard.followup.kpi.properties;

import java.util.Collections;
import java.util.List;

public class KpiLeadTimeProperties {
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
}
