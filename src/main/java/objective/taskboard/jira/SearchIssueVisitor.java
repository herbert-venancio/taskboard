package objective.taskboard.jira;

import com.atlassian.jira.rest.client.api.domain.Issue;

public interface SearchIssueVisitor {
    public void processIssue(Issue issue);

    default void complete(){}
}
