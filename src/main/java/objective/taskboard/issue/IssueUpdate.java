package objective.taskboard.issue;

import objective.taskboard.data.Issue;

public class IssueUpdate {
    public final Issue target;
    public final IssueUpdateType updateType;
    public IssueUpdate(Issue target, IssueUpdateType updateType) {
        this.target = target;
        this.updateType = updateType;
    }
}
