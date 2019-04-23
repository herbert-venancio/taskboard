package objective.taskboard.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "issue_type_visibility")
public class IssueTypeConfiguration extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private long issueTypeId;

    private long parentIssueTypeId;

    public long getIssueTypeId() {
        return this.issueTypeId;
    }

    public long getParentIssueTypeId() {
        return this.parentIssueTypeId;
    }

    public void setIssueTypeId(final long issueTypeId) {
        this.issueTypeId = issueTypeId;
    }

    public void setParentIssueTypeId(final long parentIssueTypeId) {
        this.parentIssueTypeId = parentIssueTypeId;
    }

    @Override
    public String toString() {
        return "IssueTypeConfiguration{" +
                "issueTypeId=" + issueTypeId +
                ", parentIssueTypeId=" + parentIssueTypeId +
                '}';
    }

}
