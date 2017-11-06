package objective.taskboard.task;

import objective.taskboard.controller.WebhookController;
import objective.taskboard.issueBuffer.WebhookEvent;

public interface IssueEventProcessor {

    boolean processEvent(IssueEvent event);

    class IssueEvent {
        public final WebhookEvent event;
        public final String projectKey;
        public final String issueKey;
        public final WebhookController.WebhookBody eventData;

        public IssueEvent(WebhookEvent event, String projectKey, String issueKey, WebhookController.WebhookBody eventData) {
            this.event = event;
            this.projectKey = projectKey;
            this.issueKey = issueKey;
            this.eventData = eventData;
        }
    }
}
