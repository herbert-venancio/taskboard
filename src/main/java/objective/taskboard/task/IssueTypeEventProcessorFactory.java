package objective.taskboard.task;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.domain.Issue;

import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.JiraIssueService;
import objective.taskboard.jira.WebhookSubtaskCreatorService;
import objective.taskboard.jira.data.WebHookBody;
import objective.taskboard.jira.data.WebhookEvent;

@Component
public class IssueTypeEventProcessorFactory extends BaseJiraEventProcessorFactory implements JiraEventProcessorFactory {

    @Autowired
    private JiraIssueService jiraIssueService;

    @Autowired
    private IssueBufferService issueBufferService;

    @Autowired
    private WebhookSubtaskCreatorService webhookSubtaskCreatorService;

    @Override
    public JiraEventProcessor create(WebHookBody body, String projectKey) {
        if(!belongsToAnyProject(projectKey))
            return null;

        if(!belongsToAnyIssueTypeFilter(getIssueTypeIdOrNull(body)))
            return null;

        if(body.webhookEvent.category != WebhookEvent.Category.ISSUE)
            return null;

        String issueKey = getIssueKey(body);
        return new IssueTypeEventProcessor(body.webhookEvent, issueKey, body.changelog);
    }

    private String getIssueKey(WebHookBody body) {
        return (String) body.issue.get("key");
    }

    private Long getIssueTypeIdOrNull(WebHookBody body) {
        if(!body.issue.containsKey("issuetype"))
            return null;

        @SuppressWarnings("unchecked")
		Map<String, Object> issueType = (Map<String, Object>) body.issue.get("issuetype");
        return issueType == null ? null : Long.parseLong((String) issueType.get("id"));
    }

    private class IssueTypeEventProcessor implements JiraEventProcessor {

        private final WebhookEvent webHook;
        private final String issueKey;
        public final WebHookBody.Changelog changelog;

        private IssueTypeEventProcessor(WebhookEvent webHook, String issueKey, WebHookBody.Changelog changelog) {
            this.webHook = webHook;
            this.issueKey = issueKey;
            this.changelog = changelog;
        }

        @Override
        public String getDescription() {
            return "webhook: " + webHook.typeName + " issue: " + issueKey;
        }

        @Override
        public void processEvent() {
            Optional<Issue> issue = fetchIssue();
            if (issue.isPresent()) {
                com.atlassian.jira.rest.client.api.domain.Issue jiraIssue = issue.get();
                webhookSubtaskCreatorService.createSubtaskOnTransition(jiraIssue, changelog);
            }
            issueBufferService.updateByEvent(webHook, null, issueKey, issue);
        }

        private Optional<Issue> fetchIssue() {
            if (webHook == WebhookEvent.ISSUE_DELETED)
                return Optional.empty();
            return jiraIssueService.searchIssueByKey(issueKey);
        }
    }
}
