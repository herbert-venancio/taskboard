package objective.taskboard.project.config;

import objective.taskboard.followup.cluster.FollowUpClusterItem;

class ProjectClusterItemDto {
    private String issueType;
    private String sizing;
    private double effort;
    private double cycle;
    private boolean isFromBaseCluster;

    public ProjectClusterItemDto() {}

    public ProjectClusterItemDto(String issueType, String sizing, double effort, double cycle, boolean isFromBaseCluster) {
        this.issueType = issueType;
        this.sizing = sizing;
        this.effort = effort;
        this.cycle = cycle;
        this.isFromBaseCluster = isFromBaseCluster;
    }

    public ProjectClusterItemDto(FollowUpClusterItem object) {
        this.issueType = object.getSubtaskTypeName();
        this.sizing = object.getSizing();
        this.effort = object.getEffort();
        this.cycle = object.getCycle();
        this.isFromBaseCluster = object.isFromBaseCluster();
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

    public double getEffort() {
        return effort;
    }

    public void setEffort(double effort) {
        this.effort = effort;
    }

    public double getCycle() {
        return cycle;
    }

    public void setCycle(double cycle) {
        this.cycle = cycle;
    }

    public boolean isFromBaseCluster() {
        return isFromBaseCluster;
    }

    public void setFromBaseCluster(boolean fromBaseCluster) {
        isFromBaseCluster = fromBaseCluster;
    }
}
