package objective.taskboard.followup.cluster;

import org.apache.commons.lang3.Validate;

import objective.taskboard.domain.ProjectFilterConfiguration;

public class FollowUpClusterItem {

    private ProjectFilterConfiguration project;
    private String subtaskTypeName;
    private String parentTypeName;
    private String sizing;
    private Double effort;
    private Double cycle;
    
    public FollowUpClusterItem(
            ProjectFilterConfiguration project,
            String subtaskTypeName,
            String parentTypeName,
            String sizing,
            Double effort,
            Double cycle
            ) {
        setProject(project);
        setSubtaskTypeName(subtaskTypeName);
        setParentTypeName(parentTypeName);
        setSizing(sizing);
        setEffort(effort);
        setCycle(cycle);
    }
    
    protected FollowUpClusterItem() {
        //JPA
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

    @Override
    public String toString() {
        return "FollowUpClusterItem [project=" + project + ", subtaskTypeName=" + subtaskTypeName + ", parentTypeName="
                + parentTypeName + ", sizing=" + sizing + ", effort=" + effort + ", cycle=" + cycle + "]";
    }
}
