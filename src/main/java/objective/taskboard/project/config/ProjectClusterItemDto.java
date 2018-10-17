package objective.taskboard.project.config;

import objective.taskboard.followup.cluster.FollowUpClusterItem;

class ProjectClusterItemDto {
    private String projectKey;
    private String issueType;
    private String sizing;
    private Double effort;
    private Double cycle;
    private Boolean isFromBaseCluster;

    public ProjectClusterItemDto() {}

    public ProjectClusterItemDto(String projectKey, String issueType, String sizing, Double effort, Double cycle, Boolean isFromBaseCluster) {
        this.projectKey = projectKey;
        this.issueType = issueType;
        this.sizing = sizing;
        this.effort = effort;
        this.cycle = cycle;
        this.isFromBaseCluster = isFromBaseCluster;
    }

    public ProjectClusterItemDto(FollowUpClusterItem object) {
        this.projectKey = object.getProject().getProjectKey();
        this.issueType = object.getSubtaskTypeName();
        this.sizing = object.getSizing();
        this.effort = object.getEffort();
        this.cycle = object.getCycle();
        this.isFromBaseCluster = object.isFromBaseCluster();
    }

    public String getProjectKey() {
        return projectKey;
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
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

    public Boolean isFromBaseCluster() {
        return isFromBaseCluster;
    }

    public void isFromBaseCluster(Boolean isFromBaseCluster) {
        this.isFromBaseCluster = isFromBaseCluster;
    }

}
