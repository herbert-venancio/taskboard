package objective.taskboard.jira;

import static java.util.stream.Collectors.toMap;
import static objective.taskboard.utils.StreamUtils.returnFirstMerger;

import java.time.ZoneId;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.config.LoggedInUserKeyGenerator;
import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.client.JiraLinkTypeDto;
import objective.taskboard.jira.client.JiraPriorityDto;
import objective.taskboard.jira.data.JiraTimezone;
import objective.taskboard.jira.data.Status;
import objective.taskboard.jira.endpoint.AuthorizedJiraEndpoint;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;
import objective.taskboard.jira.endpoint.JiraEndpointAsMaster;

@Service
public class MetadataCachedService {

    @Autowired
    private JiraEndpointAsMaster jiraEndpointAsMaster;

    @Autowired
    private JiraEndpointAsLoggedInUser jiraEndpointAsLoggedInUser;

    @Cacheable(CacheConfiguration.ISSUE_TYPE_METADATA)
    public Map<Long, JiraIssueTypeDto> getIssueTypeMetadata() {
        return loadIssueTypes(jiraEndpointAsMaster);
    }

    @Cacheable(cacheNames=CacheConfiguration.ISSUE_TYPE_METADATA, keyGenerator=LoggedInUserKeyGenerator.NAME)
    public Map<Long, JiraIssueTypeDto> getIssueTypeMetadataAsLoggedInUser() {
        return loadIssueTypes(jiraEndpointAsLoggedInUser);
    }

    @Cacheable(CacheConfiguration.PRIORITIES_METADATA)
    public Map<Long, JiraPriorityDto> getPrioritiesMetadata() {
        return loadPriorities();
    }

    @Cacheable(CacheConfiguration.STATUSES_METADATA)
    public Map<Long, Status> getStatusesMetadata() {
        return loadStatuses(jiraEndpointAsMaster);
    }

    @Cacheable(cacheNames=CacheConfiguration.STATUSES_METADATA, keyGenerator=LoggedInUserKeyGenerator.NAME)
    public Map<Long, Status> getStatusesMetadataAsLoggedInUser() {
        return loadStatuses(jiraEndpointAsLoggedInUser);
    }

    @Cacheable(CacheConfiguration.ISSUE_LINKS_METADATA)
    public Map<String, JiraLinkTypeDto> getIssueLinksMetadata() {
        return loadIssueLinks();
    }

    @Cacheable(CacheConfiguration.CREATE_ISSUE_METADATA)
    public Map<Long, JiraCreateIssue.IssueTypeMetadata> getCreateIssueMetadata() {
        return loadCreateIssueMetadata();
    }

    @Cacheable(CacheConfiguration.JIRA_TIME_ZONE)
    public ZoneId getJiraTimeZone() {
        return ZoneId.of(jiraEndpointAsMaster.request(JiraTimezone.Service.class).get().timeZone);
    }

    private Map<Long, JiraIssueTypeDto> loadIssueTypes(AuthorizedJiraEndpoint jiraEndpoint) {
        return jiraEndpoint.request(JiraIssueTypeDto.Service.class).all()
                .stream()
                .collect(toMap(JiraIssueTypeDto::getId, t -> t));
    }

    private Map<Long, JiraPriorityDto> loadPriorities() {
        return jiraEndpointAsMaster.request(JiraPriorityDto.Service.class).all()
                .stream()
                .collect(toMap(JiraPriorityDto::getId, t -> t));
    }

    private Map<Long, Status> loadStatuses(AuthorizedJiraEndpoint jiraEndpoint) {
        return jiraEndpoint.request(Status.Service.class).all()
                .stream()
                .collect(toMap(t -> t.id, t -> t));
    }

    private Map<Long, JiraCreateIssue.IssueTypeMetadata> loadCreateIssueMetadata() {
        return jiraEndpointAsMaster.request(JiraCreateIssue.Service.class).all()
                .projects.stream()
                .flatMap(p -> p.issueTypes.stream())
                .collect(toMap(issueType -> issueType.id, Function.identity(), returnFirstMerger()));
    }

    private Map<String, JiraLinkTypeDto> loadIssueLinks() {
        return jiraEndpointAsMaster.request(JiraLinkTypeDto.Service.class).all()
                .issueLinkTypes.stream()
                .collect(toMap(l -> l.id, l -> l));
    }

}
