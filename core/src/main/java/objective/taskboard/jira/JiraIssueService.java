package objective.taskboard.jira;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.issueBuffer.CardRepo;

@Service
public class JiraIssueService {

    @Autowired
    private JiraSearchService jiraSearchService;

    @Autowired
    private JiraIssueJqlBuilderService jqlService;

    public void searchAllProjectIssues(SearchIssueVisitor visitor, CardRepo cardsRepo) {
        jiraSearchService.searchIssues(jqlService.projectsJql(cardsRepo), visitor);
    }
}
