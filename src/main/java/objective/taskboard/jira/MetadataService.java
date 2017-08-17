package objective.taskboard.jira;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Status;

@Service
public class MetadataService {

    @Autowired
    private MetadataCachedService cache;

    public Map<Long, IssueType> getIssueTypeMetadata() throws InterruptedException, ExecutionException {
        return cache.getIssueTypeMetadata();
    }

    public Map<Long, Priority> getPrioritiesMetadata() throws InterruptedException, ExecutionException {
        return cache.getPrioritiesMetadata();
    }

    public Map<Long, Status> getStatusesMetadata() throws InterruptedException, ExecutionException {
        return cache.getStatusesMetadata();
    }

    public Map<String, IssuelinksType> getIssueLinksMetadata() {
        return cache.getIssueLinksMetadata();
    }

    public Status getStatusById(Long id) {
        try {
            Status status = getStatusesMetadata().get(id);
            if (status == null)
                throw new IllegalArgumentException("There's no Status with given ID: " + id);
            return status;
        } catch (InterruptedException | ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }

}
