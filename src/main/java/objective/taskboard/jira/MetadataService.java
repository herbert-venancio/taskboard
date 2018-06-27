package objective.taskboard.jira;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.CimIssueType;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Priority;

import objective.taskboard.jira.data.Status;

@Service
public class MetadataService {

    @Autowired
    private MetadataCachedService cache;

    public Map<Long, IssueType> getIssueTypeMetadata() {
        return cache.getIssueTypeMetadata();
    }

    public Map<Long, IssueType> getIssueTypeMetadataAsLoggedInUser() {
        return cache.getIssueTypeMetadataAsLoggedInUser();
    }

    public Map<Long, Priority> getPrioritiesMetadata() {
        return cache.getPrioritiesMetadata();
    }

    public Map<Long, Status> getStatusesMetadata() {
        return cache.getStatusesMetadata();
    }

    public Map<Long, Status> getStatusesMetadataAsLoggedInUser() {
        return cache.getStatusesMetadataAsLoggedInUser();
    }

    public Map<String, IssuelinksType> getIssueLinksMetadata() {
        return cache.getIssueLinksMetadata();
    }

    public Map<Long, CimIssueType> getCreateIssueMetadata() {
        return cache.getCreateIssueMetadata();
    }

    public IssueType getIssueTypeById(Long id) {
        return Optional.ofNullable(getIssueTypeMetadata().get(id))
                .orElseThrow(() -> new IllegalArgumentException("There's no Issue Type with given ID: " + id));
    }

    public IssueType getIssueTypeByIdAsLoggedInUser(Long id) {
        return Optional.ofNullable(getIssueTypeMetadataAsLoggedInUser().get(id))
                .orElseThrow(() -> new IllegalArgumentException("There's no Issue Type with given ID: " + id));
    }

    public Status getStatusById(Long id) {
        Status status = getStatusesMetadata().get(id);
        if (status == null)
            throw new IllegalArgumentException("There's no Status with given ID: " + id);
        return status;
    }

    public Status getStatusByIdAsLoggedInUser(Long id) {
        Status status = getStatusesMetadataAsLoggedInUser().get(id);
        if (status == null)
            throw new IllegalArgumentException("There's no Status with given ID: " + id);
        return status;
    }

    public Long getIdOfStatusByName(String name) {
        Map<Long, Status> metadata = getStatusesMetadata();
        return metadata.entrySet().stream().filter(entry -> entry.getValue().name.equals(name)).findFirst().map(entry -> entry.getKey()).orElse(null);
    }
}
