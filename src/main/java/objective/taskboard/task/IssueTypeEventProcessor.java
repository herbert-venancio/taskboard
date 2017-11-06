package objective.taskboard.task;

import com.atlassian.jira.rest.client.api.domain.Issue;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.issueBuffer.WebhookEvent;
import objective.taskboard.jira.JiraIssueService;
import objective.taskboard.jira.WebhookSubtaskCreatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IssueTypeEventProcessor implements IssueEventProcessor {

    @Autowired
    private JiraIssueService jiraIssueService;

    @Autowired
    private IssueBufferService issueBufferService;

    @Autowired
    private WebhookSubtaskCreatorService webhookSubtaskCreatorService;

    @Override
    public boolean processEvent(IssueEvent item) {
        if(item.event.category == WebhookEvent.Category.ISSUE) {
            Optional<Issue> issue = fetchIssue(item);
            if (issue.isPresent()) {
                com.atlassian.jira.rest.client.api.domain.Issue jiraIssue = issue.get();
                webhookSubtaskCreatorService.createSubtaskOnTransition(jiraIssue, item.eventData != null ? item.eventData.changelog : null);
            }
            issueBufferService.updateByEvent(item.event, item.issueKey, issue);
            return true;
        }
        return false;
    }

    private Optional<Issue> fetchIssue(IssueEvent item) {
        if (item.event == WebhookEvent.ISSUE_DELETED)
            return Optional.empty();
        return jiraIssueService.searchIssueByKey(item.issueKey);
    }
}
