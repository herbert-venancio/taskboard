package objective.taskboard.followup.kpi.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

public class KpiCycleTimeProperties {
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
}
