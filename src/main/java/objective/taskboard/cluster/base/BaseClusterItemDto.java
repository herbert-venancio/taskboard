package objective.taskboard.cluster.base;

import objective.taskboard.followup.cluster.SizingCluster;
import objective.taskboard.followup.cluster.SizingClusterItem;

class BaseClusterItemDto {

    private String subtaskTypeName;
    private String sizing;
    private Double effort;
    private Double cycle;
    private static final String NOTUSED = "notused";

    public BaseClusterItemDto() {
    }

    public BaseClusterItemDto(String subtaskTypeName, String sizing, Double effort, Double cycle) {
        this.subtaskTypeName = subtaskTypeName;
        this.sizing = sizing;
        this.effort = effort;
        this.cycle = cycle;
    }

    public BaseClusterItemDto(SizingClusterItem sizingClusterItem) {
        this.subtaskTypeName = sizingClusterItem.getSubtaskTypeName();
        this.sizing = sizingClusterItem.getSizing();
        this.effort = sizingClusterItem.getEffort();
        this.cycle = sizingClusterItem.getCycle();
    }

    public String getSubtaskTypeName() {
        return subtaskTypeName;
    }

    public void setSubtaskTypeName(String subtaskTypeName) {
        this.subtaskTypeName = subtaskTypeName;
    }

    public String getSizing() {
        return sizing;
    }

    public void setSizing(String sizing) {
        this.sizing = sizing;
    }

    public Double getEffort() {
        return effort;
    }

    public void setEffort(Double effort) {
        this.effort = effort;
    }

    public Double getCycle() {
        return cycle;
    }

    public void setCycle(Double cycle) {
        this.cycle = cycle;
    }

    public SizingClusterItem toEntity(SizingCluster clusterSaved) {
        return new SizingClusterItem(this.subtaskTypeName, NOTUSED, this.sizing, this.effort, this.cycle, null, clusterSaved);
    }
}
