package objective.taskboard.jira;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.issueBuffer.CardRepo;
import objective.taskboard.jira.client.JiraIssueDto;

@Service
public class JiraIssueService {

    @Autowired
    private JiraSearchService jiraSearchService;

    @Autowired
    private JiraIssueJqlBuilderService jqlService;

    public Optional<JiraIssueDto> searchIssueByKey(final String key) {
        final AtomicReference<JiraIssueDto> foundIssue = new AtomicReference<>();
        searchIssues(foundIssue::set, "key IN (" + key + ")", "subtasks");

        return Optional.ofNullable(foundIssue.get());
    }

    public void searchAllProjectIssues(SearchIssueVisitor visitor, CardRepo cardsRepo) {
        jiraSearchService.searchIssues(jqlService.projectsJql(cardsRepo), visitor);
    }

    private void searchIssues(SearchIssueVisitor visitor, String additionalJqlCondition, String... additionalFields) {
        String jql = jqlService.projectsSqlWithoutTimeConstraint();
        if (additionalJqlCondition != null)
            jql = "(" + additionalJqlCondition + ") AND " + jql;

        jiraSearchService.searchIssues(jql, visitor, additionalFields);
    }
}
