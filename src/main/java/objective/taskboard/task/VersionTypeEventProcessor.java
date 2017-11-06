package objective.taskboard.task;

import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.Issue;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.issueBuffer.WebhookEvent;
import objective.taskboard.jira.JiraService;
import objective.taskboard.jira.JiraServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.stream.Stream;

import static objective.taskboard.config.CacheConfiguration.PROJECTS;

@Component
public class VersionTypeEventProcessor implements IssueEventProcessor {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private IssueBufferService issueBufferService;

    @Autowired
    private JiraService jiraBean;

    @Override
    public boolean processEvent(IssueEvent item) {
        if (item.event.category == WebhookEvent.Category.VERSION) {
            cacheManager.getCache(PROJECTS).clear();
            fetchIssues(item)
                    .forEach(issue -> issueBufferService.updateByEvent(item.event, issue.get().getKey(), issue));
            return true;
        }
        return false;
    }

    private Stream<Optional<Issue>> fetchIssues(IssueEvent item) {
        return issueBufferService.getAllIssues().stream()
                .filter(issue -> item.eventData.version.id.equals(issue.getVersionId())
                        || (issue.getParentCard().isPresent()
                            && item.eventData.version.id.equals(issue.getParentCard().get().getVersionId())
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
                .filter(Optional::isPresent);
    }
}
