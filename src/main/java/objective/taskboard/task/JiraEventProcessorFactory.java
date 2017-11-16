package objective.taskboard.task;

import java.util.Optional;

import objective.taskboard.jira.data.WebHookBody;

public interface JiraEventProcessorFactory {

    Optional<JiraEventProcessor> create(WebHookBody body, String projectKey);

}
