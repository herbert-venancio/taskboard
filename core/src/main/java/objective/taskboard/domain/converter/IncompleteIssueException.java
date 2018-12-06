package objective.taskboard.domain.converter;

import objective.taskboard.data.IssueScratch;

public class IncompleteIssueException extends RuntimeException {
    private static final long serialVersionUID = -1758751615399513930L;
    private final transient IssueScratch incompleteIssue;
    private final String missingParentKey;
    
    public IncompleteIssueException(IssueScratch i, String missingParentKey) {
        this.incompleteIssue = i;
        this.missingParentKey = missingParentKey;
    }

    public IssueScratch getIncompleteIssue() {
        return incompleteIssue;
    }

    public String getMissingParentKey() {
        return missingParentKey;
    }

    @Override
    public String toString() {
        return "IncompleteIssueException [incompleteIssue=" + incompleteIssue + ", missingParentKey="
                + missingParentKey + "]";
    }

}
