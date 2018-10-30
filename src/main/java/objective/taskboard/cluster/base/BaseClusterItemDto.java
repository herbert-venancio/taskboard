package objective.taskboard.cluster.base;

import objective.taskboard.followup.cluster.SizingCluster;
import objective.taskboard.followup.cluster.SizingClusterItem;

class BaseClusterItemDto {

    private String issueType;
    private String sizing;
    private Double effort;
    private Double cycle;
    private static final String NOTUSED = "notused";

    public BaseClusterItemDto() {
    }

    public BaseClusterItemDto(String issueType, String sizing, Double effort, Double cycle) {
        this.issueType = issueType;
        this.sizing = sizing;
        this.effort = effort;
        this.cycle = cycle;
    }

    public BaseClusterItemDto(SizingClusterItem sizingClusterItem) {
        this.issueType = sizingClusterItem.getSubtaskTypeName();
        this.sizing = sizingClusterItem.getSizing();
        this.effort = sizingClusterItem.getEffort();
        this.cycle = sizingClusterItem.getCycle();
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
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
        return new SizingClusterItem(this.issueType, NOTUSED, this.sizing, this.effort, this.cycle, null, clusterSaved);
    }
}
