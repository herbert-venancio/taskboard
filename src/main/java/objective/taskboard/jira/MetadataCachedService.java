package objective.taskboard.jira;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Status;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;

@Service
public class MetadataCachedService {

    @Autowired
    private JiraEndpointAsMaster jiraEndpointAsMaster;

    @Cacheable("issueTypeMetadata")
    public Map<Long, IssueType> getIssueTypeMetadata() throws InterruptedException, ExecutionException {
        return loadIssueTypes();
    }

    @Cacheable("prioritiesMetadata")
    public Map<Long, Priority> getPrioritiesMetadata() throws InterruptedException, ExecutionException {
        return loadPriorities();
    }

    @Cacheable("statusesMetadata")
    public Map<Long, Status> getStatusesMetadata() throws InterruptedException, ExecutionException {
        return loadStatuses();
    }

    @Cacheable(CacheConfiguration.ISSUE_LINKS_METADATA)
    public Map<String, IssuelinksType> getIssueLinksMetadata() {
        return loadIssueLinks();
    }

    private Map<Long, IssueType> loadIssueTypes() throws InterruptedException, ExecutionException {
        Iterable<IssueType> issueTypes = jiraEndpointAsMaster.executeRequest(client -> client.getMetadataClient().getIssueTypes());
        return newArrayList(issueTypes).stream().collect(Collectors.toMap(IssueType::getId, t -> t));
    }

    private Map<Long, Priority> loadPriorities() throws InterruptedException, ExecutionException {
        Iterable<Priority> priorities = jiraEndpointAsMaster.executeRequest(client -> client.getMetadataClient().getPriorities());
        return newArrayList(priorities).stream().collect(Collectors.toMap(Priority::getId, t -> t));
    }

    private Map<Long, Status> loadStatuses() throws InterruptedException, ExecutionException {
        Iterable<Status> statuses = jiraEndpointAsMaster.executeRequest(client -> client.getMetadataClient().getStatuses());
        return newArrayList(statuses).stream().collect(Collectors.toMap(Status::getId, t -> t));
    }

    private Map<String, IssuelinksType> loadIssueLinks() {
        Iterable<IssuelinksType> links = jiraEndpointAsMaster.executeRequest(client -> client.getMetadataClient().getIssueLinkTypes());
        return newArrayList(links).stream().collect(Collectors.toMap(IssuelinksType::getId, l -> l));
    }

}
