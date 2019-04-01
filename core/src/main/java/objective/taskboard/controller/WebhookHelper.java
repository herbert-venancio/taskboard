package objective.taskboard.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.data.Issue;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.client.ChangelogItemDto;
import objective.taskboard.jira.data.WebHookBody;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;


@Component
public class WebhookHelper {

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepository;

    @Autowired
    private IssueBufferService issueBufferService;

    public boolean belongsToAnyProject(String projectKey) {
        return projectRepository.exists(projectKey);
    }

    public Optional<Issue> fetchOldIssue(WebHookBody.Changelog changelog) {
        return changelog.items.stream()
                .filter(change -> "key".equalsIgnoreCase(change.getField()))
                .findFirst()
                .map(ChangelogItemDto::getFromString)
                .flatMap(oldIssueKey -> Optional.ofNullable(issueBufferService.getIssueByKey(oldIssueKey)));
    }

    public boolean changedIssueKeyWasBuffered(WebHookBody.Changelog changelog) {
        return fetchOldIssue(changelog)
                .isPresent();
    }
}
