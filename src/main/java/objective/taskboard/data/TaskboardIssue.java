package objective.taskboard.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;

@Entity
@EntityListeners(IssuePersistenceListener.class)
public class TaskboardIssue implements Serializable {
    private static final long serialVersionUID = -1772950366555561419L;

    public TaskboardIssue(String issueKey, long priority) {
        this.issueKey = issueKey;
        this.priority = priority;
    }
    
    public TaskboardIssue(){
    }

    @Id
    private String issueKey;
    
    @Column
    private long priority;
    
    public long getPriority() {
        return priority;
    }
    
    public void setPriority(long priority) {
        this.priority = priority;
    }
    
    public String getProjectKey() {
        return issueKey;
    }
    
    public void setProjectKey(String projectKey) {
        this.issueKey = projectKey;
    }
}
