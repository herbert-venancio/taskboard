package objective.taskboard.controller;

import static java.util.Optional.ofNullable;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.jira.client.JiraIssueDto;
import objective.taskboard.jira.client.JiraUserDto;
import objective.taskboard.jira.data.WebHookBody;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.task.IssueEventProcessScheduler;
import objective.taskboard.task.JiraEventProcessor;
import objective.taskboard.task.JiraEventProcessorFactory;

@RestController
@RequestMapping("webhook")
public class WebhookController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepository;

    @Autowired
    private IssueEventProcessScheduler webhookSchedule;

    @Autowired
    private List<JiraEventProcessorFactory> jiraEventProcessorFactories;

    @PostMapping("{projectKey}")
    public void webhook(@RequestBody WebHookBody body, @PathVariable("projectKey") String projectKey) {
        log.debug("Incoming Webhook request: type " + 
                body.webhookEvent.typeName +
                " / timestamp " + body.timestamp +
                " / issue: " +   ofNullable(body.issue).map(JiraIssueDto::getKey).orElse("N/A") +
                " / version: " + ofNullable(body.version).map(t->t.name).orElse("N/A") +
                " / user: " +    ofNullable(body.user).map(JiraUserDto::getName).orElse("N/A"));

        if(body.webhookEvent == null)
            return;

        if(!belongsToAnyProject(projectKey))
            return;

        for(JiraEventProcessorFactory factory : jiraEventProcessorFactories) {
            factory.create(body, projectKey)
                    .ifPresent(this::enqueue);
        }
    }

    private void enqueue(JiraEventProcessor eventProcessor) {
        webhookSchedule.add(eventProcessor);
    }

    protected boolean belongsToAnyProject(String projectKey) {
        return projectRepository.exists(projectKey);
    }
}
