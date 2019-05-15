package objective.taskboard.domain;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "filtro")
public class Filter extends TaskboardEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "step")
    private Step step;

    private long issueTypeId;

    private long statusId;

    private String limitInDays;

    public Step getStep() {
        return this.step;
    }

    public long getIssueTypeId() {
        return this.issueTypeId;
    }

    public long getStatusId() {
        return this.statusId;
    }

    public String getLimitInDays() {
        return this.limitInDays;
    }

    public void setStep(final Step step) {
        this.step = step;
    }

    public void setIssueTypeId(final long issueTypeId) {
        this.issueTypeId = issueTypeId;
    }

    public void setStatusId(final long statusId) {
        this.statusId = statusId;
    }

    public void setLimitInDays(final String limitInDays) {
        this.limitInDays = limitInDays;
    }

    @Override
    public String toString() {
        return "Filter{" +
                "step=" + step +
                ", issueTypeId=" + issueTypeId +
                ", statusId=" + statusId +
                ", limitInDays='" + limitInDays + '\'' +
                '}';
    }
}
