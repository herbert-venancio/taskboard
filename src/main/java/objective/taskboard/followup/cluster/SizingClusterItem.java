package objective.taskboard.followup.cluster;

import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang3.Validate;
import org.hibernate.annotations.Check;

import objective.taskboard.domain.TaskboardEntity;

@Entity
@Table(name = "sizing_cluster_item")
@Check(constraints = "(project_key IS NULL AND base_cluster_id IS NOT NULL) OR (project_key IS NOT NULL AND base_cluster_id IS NULL)")
public class SizingClusterItem extends TaskboardEntity {

    private String subtaskTypeName;
    private String parentTypeName;
    private String sizing;
    private Double effort;
    private Double cycle;
    private String projectKey;
    
    @ManyToOne
    @JoinColumn(name = "base_cluster_id")
    private SizingCluster baseCluster;

    public SizingClusterItem(
            String subtaskTypeName,
            String parentTypeName,
            String sizing,
            Double effort,
            Double cycle,
            String projectKey,
            SizingCluster baseCluster
            ) {
        setSubtaskTypeName(subtaskTypeName);
        setParentTypeName(parentTypeName);
        setSizing(sizing);
        setEffort(effort);
        setCycle(cycle);
        setProjectKey(projectKey);
        setBaseCluster(baseCluster);
    }
    
    protected SizingClusterItem() {
        //JPA
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

    public Optional<SizingCluster> getBaseCluster() {
        return Optional.ofNullable(baseCluster);
    }

    public void setBaseCluster(SizingCluster baseCluster) {
        this.baseCluster = baseCluster;
    }

    public Optional<String> getProjectKey() {
        return Optional.ofNullable(projectKey);
    }

    public void setProjectKey(String projectKey) {
        this.projectKey = projectKey;
    }
}
