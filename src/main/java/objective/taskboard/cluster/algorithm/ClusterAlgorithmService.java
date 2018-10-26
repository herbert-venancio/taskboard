package objective.taskboard.cluster.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.properties.JiraProperties;

@Service
public class ClusterAlgorithmService {

    private ExecutorService executorService;

    private JiraProperties jiraProperties;

    private IssueBufferService issueBufferService;

    @Autowired
    public ClusterAlgorithmService(ExecutorService executorService, JiraProperties jiraProperties, IssueBufferService issueBufferService) {
        this.executorService = executorService;
        this.jiraProperties = jiraProperties;
        this.issueBufferService = issueBufferService;
    }

    private AtomicLong nextId = new AtomicLong(0L);
    private Map<Long, ClusterAlgorithmExecution> executions = Collections.synchronizedMap(new LinkedHashMap<>());

    public List<ClusterAlgorithmExecution> getExecutions() {
        return new ArrayList<>(executions.values());
    }

    public Optional<ClusterAlgorithmExecution> getExecution(long id) {
        return Optional.ofNullable(executions.get(id));
    }

    public ClusterAlgorithmExecution startExecution(ClusterAlgorithmRequest request) {
        ClusterAlgorithmExecution execution = createExecution(request);
        execution.start();
        return execution;
    }

    public ClusterAlgorithmExecution createExecution(ClusterAlgorithmRequest request) {
        long id = nextId.incrementAndGet();
        ClusterAlgorithmExecution execution = new ClusterAlgorithmExecution(executorService, id, createContext(request));
        executions.put(id, execution);
        return execution;
    }

    private ClusterAlgorithmContext createContext(ClusterAlgorithmRequest request) {
        return new ClusterAlgorithmContext(
                request
                , jiraProperties.getFollowup().getBallparkMappings()
                , issueBufferService.getAllIssues());
    }

    public Optional<ClusterAlgorithmExecution> stopExecution(long id) {
        return getExecution(id)
                .map(exec -> {
                    exec.cancel();
                    return exec;
                });
    }

    public Optional<ClusterAlgorithmExecution> deleteExecution(long id) {
        ClusterAlgorithmExecution execution = executions.remove(id);
        if(execution != null)
            execution.cancel();

        return Optional.ofNullable(execution);
    }
}
