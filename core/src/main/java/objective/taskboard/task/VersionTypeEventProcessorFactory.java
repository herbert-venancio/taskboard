package objective.taskboard.task;

import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.data.Issue;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.data.Version;
import objective.taskboard.jira.data.WebHookBody;
import objective.taskboard.jira.data.WebhookEvent;
import objective.taskboard.jira.properties.JiraProperties;

@Component
public class VersionTypeEventProcessorFactory implements JiraEventProcessorFactory {

    @Autowired
    private JiraProperties jiraProperties;

    @Autowired
    private IssueBufferService issueBufferService;

    @Override
    public Optional<JiraEventProcessor> create(WebHookBody body, String projectKey) {
        if (body.webhookEvent.category != WebhookEvent.Category.VERSION)
            return Optional.empty();

        if (!isReleaseConfigured())
            return Optional.empty();

        return Optional.of(new VersionTypeEventProcessor(body.webhookEvent, projectKey, body.version));
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
            issueBufferService.notifyProjectUpdate(projectKey);
            fetchIssues(version)
                    .forEach(issue -> issueBufferService.notifyIssueUpdate(issue));
        }

        private Stream<Issue> fetchIssues(Version version) {
            return issueBufferService.getAllIssues().stream()
                    .filter(issue -> version.id.equals(issue.getReleaseId()));
        }
    }
}
