package objective.taskboard.task;

import objective.taskboard.jira.data.WebHookBody;

public interface JiraEventProcessorFactory {

    JiraEventProcessor create(WebHookBody body, String projectKey);

}
