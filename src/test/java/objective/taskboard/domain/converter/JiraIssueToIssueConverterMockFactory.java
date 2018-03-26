package objective.taskboard.domain.converter;

import static java.util.Collections.emptyList;
import static objective.taskboard.domain.converter.IssueFieldsExtractor.convertWorklog;
import static objective.taskboard.domain.converter.IssueFieldsExtractor.extractChangelog;
import static objective.taskboard.domain.converter.IssueFieldsExtractor.extractComponents;
import static objective.taskboard.domain.converter.IssueFieldsExtractor.extractLabels;
import static objective.taskboard.domain.converter.IssueFieldsExtractor.extractRealParent;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willCallRealMethod;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

import org.springframework.beans.factory.FactoryBean;

import objective.taskboard.data.IssueScratch;
import objective.taskboard.data.TaskboardTimeTracking;
import objective.taskboard.jira.client.JiraIssueDto;

public class JiraIssueToIssueConverterMockFactory implements FactoryBean<JiraIssueToIssueConverter> {

    @Override
    public JiraIssueToIssueConverter getObject() throws Exception {
        JiraIssueToIssueConverter jiraIssueToIssueConverter = mock(JiraIssueToIssueConverter.class);
        willAnswer(invocation -> {
            JiraIssueDto jiraIssue = invocation.getArgumentAt(0, JiraIssueDto.class);
            ParentProvider provider = invocation.getArgumentAt(1, ParentProvider.class);

            String parentKey = extractRealParent(jiraIssue);

            IssueScratch scratch = new IssueScratch(
                    jiraIssue.getId(),
                    jiraIssue.getKey(),
                    jiraIssue.getProject().getKey(),
                    jiraIssue.getProject().getName(),
                    jiraIssue.getIssueType().getId(),
                    defaultIfNull(jiraIssue.getSummary(), ""),
                    jiraIssue.getStatus().getId(),
                    0L,
                    parentKey,
                    emptyList(),
                    jiraIssue.getAssignee() != null ? jiraIssue.getAssignee().getName() : "",
                    jiraIssue.getPriority() != null ? jiraIssue.getPriority().getId() : 0L,
                    jiraIssue.getDueDate() != null ? jiraIssue.getDueDate().toDate() : null,
                    jiraIssue.getCreationDate().getMillis(),
                    jiraIssue.getUpdateDate() != null ? jiraIssue.getUpdateDate().toDate() : jiraIssue.getCreationDate().toDate(),
                    defaultIfNull(jiraIssue.getDescription(), ""),
                    "",
                    extractLabels(jiraIssue),
                    extractComponents(jiraIssue),
                    false,
                    null,
                    null,
                    null,
                    TaskboardTimeTracking.fromJira(jiraIssue.getTimeTracking()),
                    jiraIssue.getReporter() != null ? jiraIssue.getReporter().getName() : null,
                    null,
                    null,
                    null,
                    extractChangelog(jiraIssue),
                    convertWorklog(jiraIssue.getWorklogs()));
            return jiraIssueToIssueConverter.createIssueFromScratch(scratch, provider);
        }).given(jiraIssueToIssueConverter).convertSingleIssue(any(), any());
        willCallRealMethod().given(jiraIssueToIssueConverter).createIssueFromScratch(any(), any());

        return jiraIssueToIssueConverter;
    }

    @Override
    public Class<?> getObjectType() {
        return JiraIssueToIssueConverter.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
