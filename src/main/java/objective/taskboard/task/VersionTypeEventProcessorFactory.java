package objective.taskboard.task;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;

import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.JiraServiceException;
import objective.taskboard.jira.data.Version;
import objective.taskboard.jira.data.WebHookBody;
import objective.taskboard.jira.data.WebhookEvent;

@Component
public class VersionTypeEventProcessorFactory extends BaseJiraEventProcessorFactory implements JiraEventProcessorFactory {

    @Autowired
    private JiraProperties jiraProperties;

    @Autowired
    private IssueBufferService issueBufferService;

    @Autowired
    private JiraService jiraBean;

    @Override
    public JiraEventProcessor create(WebHookBody body, String projectKey) {
        if (!belongsToAnyProject(projectKey))
            return null;

        if (body.webhookEvent.category != WebhookEvent.Category.VERSION)
            return null;

        if (!isReleaseConfigured())
            return null;

        return new VersionTypeEventProcessor(body.webhookEvent, projectKey, body.version);
    }

    private boolean isReleaseConfigured() {
        return !jiraProperties.getCustomfield().getRelease().getId().isEmpty();
    }

    private class VersionTypeEventProcessor implements JiraEventProcessor {

        public final WebhookEvent webHook;
        public final String projectKey;
        public final Version version;

        public VersionTypeEventProcessor(WebhookEvent webHook, String projectKey, Version version) {
            this.webHook = webHook;
            this.projectKey = projectKey;
            this.version = version;
        }

        @Override
        public String getDescription() {
            return "webhook: " + webHook.typeName + " project: " + projectKey;
        }

        @Override
        public void processEvent() {
            fetchIssues(version)
                    .forEach(issueKey -> issueBufferService.updateByEvent(webHook, projectKey, issueKey, null));
        }

        private Stream<String> fetchIssues(Version version) {
            return issueBufferService.getAllIssues().stream()
                    .filter(issue -> version.id.equals(issue.getReleaseId())
                                    || (issue.getParentCard().isPresent()
                                    && version.id.equals(issue.getParentCard().get().getReleaseId())
                            )
                    ).map(issue -> {
                        try {
                            return Optional.of(jiraBean.getIssueByKeyAsMaster(issue.getIssueKey()));
                        } catch (JiraServiceException ex) {
                            if (ex.getCause() instanceof RestClientException) {
                                RestClientException cause = (RestClientException) ex.getCause();
                                if (cause.getStatusCode().isPresent() && cause.getStatusCode().get() == 404)
                                    return Optional.<Issue>empty();
                            }
                            throw ex;
                        }
                    })
                    .filter(Optional::isPresent)
                    .map(issue -> issue.get().getKey());
        }
    }
}
