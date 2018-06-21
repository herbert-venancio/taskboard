package objective.taskboard.jira;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toMap;
import static objective.taskboard.utils.StreamUtils.returnFirstMerger;
import static objective.taskboard.utils.StreamUtils.streamOf;

import java.time.ZoneId;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;
import com.atlassian.jira.rest.client.api.domain.CimProject;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Priority;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.jira.data.JiraTimezone;
import objective.taskboard.jira.data.Status;
import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;

@Service
public class MetadataCachedService {

    @Autowired
    private JiraEndpointAsMaster jiraEndpointAsMaster;

    @Cacheable(CacheConfiguration.ISSUE_TYPE_METADATA)
    public Map<Long, IssueType> getIssueTypeMetadata() {
        return loadIssueTypes();
    }

    @Cacheable(CacheConfiguration.PRIORITIES_METADATA)
    public Map<Long, Priority> getPrioritiesMetadata() {
        return loadPriorities();
    }

    @Cacheable(CacheConfiguration.STATUSES_METADATA)
    public Map<Long, Status> getStatusesMetadata() {
        return loadStatuses();
    }

    @Cacheable(CacheConfiguration.ISSUE_LINKS_METADATA)
    public Map<String, IssuelinksType> getIssueLinksMetadata() {
        return loadIssueLinks();
    }

    @Cacheable(CacheConfiguration.CREATE_ISSUE_METADATA)
    public Map<Long, CimIssueType> getCreateIssueMetadata() {
        return loadCreateIssueMetadata();
    }

    @Cacheable(CacheConfiguration.JIRA_TIME_ZONE)
    public ZoneId getJiraTimeZone() {
        return ZoneId.of(jiraEndpointAsMaster.request(JiraTimezone.Service.class).get().timeZone);
    }

    private Map<Long, IssueType> loadIssueTypes() {
        Iterable<IssueType> issueTypes = jiraEndpointAsMaster.executeRequest(client -> client.getMetadataClient().getIssueTypes());
        return newArrayList(issueTypes).stream().collect(Collectors.toMap(IssueType::getId, t -> t));
    }

    private Map<Long, Priority> loadPriorities() {
        Iterable<Priority> priorities = jiraEndpointAsMaster.executeRequest(client -> client.getMetadataClient().getPriorities());
        return newArrayList(priorities).stream().collect(Collectors.toMap(Priority::getId, t -> t));
    }

    private Map<Long, Status> loadStatuses() {
        return jiraEndpointAsMaster.request(Status.Service.class).all()
                .stream()
                .collect(Collectors.toMap(t -> t.id, t -> t));
    }

    private Map<Long, CimIssueType> loadCreateIssueMetadata() {
        GetCreateIssueMetadataOptions options = new GetCreateIssueMetadataOptionsBuilder()
                .withExpandedIssueTypesFields()
                .build();

        Iterable<CimProject> projects = jiraEndpointAsMaster
                .executeRequest(client -> client.getIssueClient().getCreateIssueMetadata(options));

        return streamOf(projects)
                .flatMap(p -> streamOf(p.getIssueTypes()))
                .collect(toMap(CimIssueType::getId, Function.identity(), returnFirstMerger()));
    }

    private Map<String, IssuelinksType> loadIssueLinks() {
        Iterable<IssuelinksType> links = jiraEndpointAsMaster.executeRequest(client -> client.getMetadataClient().getIssueLinkTypes());
        return newArrayList(links).stream().collect(Collectors.toMap(IssuelinksType::getId, l -> l));
    }

}
