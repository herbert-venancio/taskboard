package objective.taskboard.followup.cluster;

import org.apache.commons.lang3.Validate;

import objective.taskboard.domain.ProjectFilterConfiguration;

public class FollowUpClusterItem {

    private Long entityId;
    private ProjectFilterConfiguration project;
    private String subtaskTypeName;
    private String parentTypeName;
    private String sizing;
    private Double effort;
    private Double cycle;
    private Boolean isFromBaseCluster;
    
    public FollowUpClusterItem(
            ProjectFilterConfiguration project,
            String subtaskTypeName,
            String parentTypeName,
            String sizing,
            Double effort,
            Double cycle
            ) {
        setEntityId(0L);
        setProject(project);
        setSubtaskTypeName(subtaskTypeName);
        setParentTypeName(parentTypeName);
        setSizing(sizing);
        setEffort(effort);
        setCycle(cycle);
        isFromBaseCluster(false);
    }

    public FollowUpClusterItem(
            Long entityId,
            ProjectFilterConfiguration project,
            String subtaskTypeName,
            String parentTypeName,
            String sizing,
            Double effort,
            Double cycle,
            Boolean isFromBaseCluster
            ) {
        setEntityId(entityId);
        setProject(project);
        setSubtaskTypeName(subtaskTypeName);
        setParentTypeName(parentTypeName);
        setSizing(sizing);
        setEffort(effort);
        setCycle(cycle);
        isFromBaseCluster(isFromBaseCluster);
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public ProjectFilterConfiguration getProject() { 
        return project; 
    } 
 
    public void setProject(ProjectFilterConfiguration project) { 
        Validate.notNull(project); 
        this.project = project; 
    }
    
    public String getSubtaskTypeName() {
        return subtaskTypeName;
    }

    public void setSubtaskTypeName(String subtaskTypeName) {
        Validate.notBlank(subtaskTypeName);
        this.subtaskTypeName = subtaskTypeName;
    }

    public String getParentTypeName() {
        return parentTypeName;
    }

    public void setParentTypeName(String parentTypeName) {
        Validate.notBlank(parentTypeName);
        this.parentTypeName = parentTypeName;
    }

    public String getSizing() {
        return sizing;
    }

    public void setSizing(String sizing) {
        Validate.notBlank(sizing);
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

    @Override
    public String toString() {
        return "FollowUpClusterItem [entityId=" + entityId + ", project=" + project + ", subtaskTypeName="
                + subtaskTypeName + ", parentTypeName=" + parentTypeName + ", sizing=" + sizing + ", effort=" + effort
                + ", cycle=" + cycle + ", isFromBaseCluster=" + isFromBaseCluster + "]";
    }
}
