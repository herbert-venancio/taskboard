package objective.taskboard.followup.cluster;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.Validate;

import objective.taskboard.domain.TaskboardEntity;
import objective.taskboard.followup.data.Template;

@Entity
@Table(name = "followup_cluster_item")
public class FollowUpClusterItem extends TaskboardEntity {

    @OneToOne(optional = false)
    @JoinColumn(name = "followup_config", nullable = false)
    private Template followUpConfiguration;

    private String subtaskTypeName;
    private String parentTypeName;
    private String sizing;
    private Double effort;
    private Double cycle;

    public FollowUpClusterItem(
            Template followUpConfiguration, 
            String subtaskTypeName, 
            String parentTypeName,
            String sizing, 
            Double effort, 
            Double cycle) {
        
        setFollowUpConfiguration(followUpConfiguration);
        setSubtaskTypeName(subtaskTypeName);
        setParentTypeName(parentTypeName);
        setSizing(sizing);
        setEffort(effort);
        setCycle(cycle);
    }
    
    protected FollowUpClusterItem() {
        //JPA
    }

    public Template getFollowUpConfiguration() {
        return followUpConfiguration;
    }

    public void setFollowUpConfiguration(Template followUpConfiguration) {
        Validate.notNull(followUpConfiguration);
        this.followUpConfiguration = followUpConfiguration;
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
}
