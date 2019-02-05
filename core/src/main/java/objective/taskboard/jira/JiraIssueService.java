package objective.taskboard.jira;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.issueBuffer.CardRepo;
import objective.taskboard.jira.client.JiraEditIssue;
import objective.taskboard.jira.endpoint.JiraEndpointAsLoggedInUser;

@Service
public class JiraIssueService {

    @Autowired
    private JiraSearchService jiraSearchService;

    @Autowired
    private JiraIssueJqlBuilderService jqlService;

    @Autowired
    private JiraEndpointAsLoggedInUser jiraEndpointAsUser;

    public void searchAllProjectIssues(SearchIssueVisitor visitor, CardRepo cardsRepo) {
        jiraSearchService.searchIssues(jqlService.projectsJql(cardsRepo), visitor);
    }

    public void SearchAllIssuesForProjects(SearchIssueVisitor visitor, List<String> projects){
        jiraSearchService.searchIssues(jqlService.projectsFromListWithoutTimeConstraint(projects), visitor);
    }

    public JiraEditIssue getIssueMetadata(String issueKey) {
        return jiraEndpointAsUser.request(JiraEditIssue.Service.class)
                .getIssueMetadata(issueKey);
    }
}
