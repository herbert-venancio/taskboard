package objective.taskboard.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import objective.taskboard.domain.BaseEntity;

@Entity
public class TaskboardIssue extends BaseEntity implements Serializable {
    private static final long serialVersionUID = -1772950366555561419L;

    @Id
    private String issueKey;
    
    @Column
    private long priority;
    
    public TaskboardIssue(String issueKey, long priority) {
        this.issueKey = issueKey;
        this.priority = priority;
    }
    
    public TaskboardIssue(){ }

    
    public long getPriority() {
        return priority;
    }
    
    public void setPriority(long priority) {
        this.priority = priority;
    }
    
    public String getIssueKey() {
        return issueKey;
    }
    
    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }
    
}
