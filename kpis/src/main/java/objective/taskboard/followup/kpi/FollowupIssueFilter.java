package objective.taskboard.followup.kpi;

import java.util.function.Predicate;

import objective.taskboard.data.Issue;
import objective.taskboard.jira.properties.JiraProperties;

public class FollowupIssueFilter implements Predicate<Issue>{
    
    private JiraProperties jiraProperties;
    private String projectKey;
    
    public FollowupIssueFilter(JiraProperties jiraProperties, String projectKey) {
        this.jiraProperties = jiraProperties;
        this.projectKey = projectKey;
    }

    private boolean isAllowedStatus(long status) {
            return !jiraProperties.getFollowup().getStatusExcludedFromFollowup().contains(status);
        }

    @Override
    public boolean test(Issue issue) {
        return projectKey.equals(issue.getProjectKey()) && isAllowedStatus(issue.getStatus());
    }

}
