package objective.taskboard.data;

import static java.lang.Integer.valueOf;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import objective.taskboard.domain.Filter;

public class IssuesConfiguration implements Serializable {
    private static final long serialVersionUID = -1742753347921403277L;

    private long issueType;
    private long status;
    private Integer limitInDays;

    public IssuesConfiguration() {
        this(0L, 0L);
    }

    public IssuesConfiguration(long issueType, long status) {
        this(issueType, status, null);
    }

    public IssuesConfiguration(long issueType, long status, Integer limitInDays) {
        this.issueType = issueType;
        this.status = status;
        this.limitInDays = limitInDays;
    }

    public static IssuesConfiguration fromFilter(Filter filter) {
        Integer limitInDays = Optional.ofNullable(filter.getLimitInDays())
                .map(days -> valueOf(days.replaceAll("[^0-9-]", "")))
                .orElse(null);
        return new IssuesConfiguration(filter.getIssueTypeId(), filter.getStatusId(), limitInDays);
    }

    public boolean matches(Issue issue) {
        return issue.getType() == getIssueType() && issue.getStatus() == getStatus();
    }

    public boolean matches(Long issueType, long status) {
        return issueType == getIssueType() && status == getStatus();
    }

    public long getIssueType() {
        return this.issueType;
    }

    public long getStatus() {
        return this.status;
    }

    public Integer getLimitInDays() {
        return this.limitInDays;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IssuesConfiguration that = (IssuesConfiguration) o;

        if (issueType != that.issueType) return false;
        if (status != that.status) return false;
        return Objects.equals(limitInDays, that.limitInDays);
    }

    @Override
    public int hashCode() {
        int result = (int) (issueType ^ (issueType >>> 32));
        result = 31 * result + (int) (status ^ (status >>> 32));
        result = 31 * result + (limitInDays != null ? limitInDays.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "IssuesConfiguration{" +
                "issueType=" + issueType +
                ", status=" + status +
                ", limitInDays='" + limitInDays + '\'' +
                '}';
    }
}
