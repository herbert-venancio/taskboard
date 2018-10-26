package objective.taskboard.cluster.algorithm;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import objective.taskboard.data.Issue;
import objective.taskboard.jira.properties.JiraProperties;

public class ClusterAlgorithmContext {

    public final ClusterAlgorithmRequest request;
    public final Map<Long, List<JiraProperties.BallparkMapping>> ballparkMappings;
    public final List<Issue> allIssues;
    public final Map<String, Issue> allIssuesMap;

    public ClusterAlgorithmContext(ClusterAlgorithmRequest request, Map<Long, List<JiraProperties.BallparkMapping>> ballparkMappings, List<Issue> allIssues) {
        this.request = request;
        this.ballparkMappings = ballparkMappings;
        this.allIssues = allIssues;
        this.allIssuesMap = allIssues.stream()
                .collect(Collectors.toMap(Issue::getIssueKey, Function.identity()));
    }
}
