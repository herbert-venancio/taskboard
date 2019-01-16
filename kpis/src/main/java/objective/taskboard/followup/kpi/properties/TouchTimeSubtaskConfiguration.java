package objective.taskboard.followup.kpi.properties;

import java.util.List;

import javax.validation.constraints.NotNull;

public class TouchTimeSubtaskConfiguration {
    @NotNull
    private String stackName;
    @NotNull
    private List<String> statuses;
    @NotNull
    private List<Long> typeIds;

    public String getStackName() {
        return stackName;
    }
    public void setStackName(String stackName) {
        this.stackName = stackName;
    }
    public List<String> getStatuses() {
        return statuses;
    }
    public void setStatuses(List<String> statuses) {
        this.statuses = statuses;
    }
    public List<Long> getTypeIds() {
        return typeIds;
    }
    public void setTypeIds(List<Long> typeIds) {
        this.typeIds = typeIds;
    }
    @Override
    public String toString() {
        return "TouchTimeSubtaskConfiguration [stackName=" + stackName + ", statuses=" + statuses + ", typeIds="
                + typeIds + "]";
    }
}
