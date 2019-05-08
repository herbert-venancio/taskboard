package objective.taskboard.filterPreferences;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.ISSUE_TYPE_1_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.ISSUE_TYPE_2_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.PROJECT_1_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.TEAM_1_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.cardFieldFiltersAllValuesSelected;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.getFilterFieldValue;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.data.CardFieldFilter;
import objective.taskboard.data.CardFieldFilter.FieldSelector;
import objective.taskboard.data.FilterFieldValue;
import objective.taskboard.data.Issue;
import objective.taskboard.data.Issue.CardTeam;
import objective.taskboard.data.IssuesConfiguration;
import objective.taskboard.data.LaneConfiguration;
import objective.taskboard.data.StageConfiguration;
import objective.taskboard.data.StepConfiguration;
import objective.taskboard.database.TaskboardDatabaseService;
@RunWith(MockitoJUnitRunner.class)
public class CardFieldFilterServiceTest {

    private static final long ISSUES_CONFIGURATION_STATUS = 10L;

    private UserPreferencesService userPreferencesService = mock(UserPreferencesService.class);
    private TaskboardDatabaseService taskboardDatabaseService = mock(TaskboardDatabaseService.class);
    private CardFieldFilterProvider cardFieldFilterProvider = mock(CardFieldFilterProvider.class);

    private CardFieldFilterService subject;

    @Before
    public void setup() {
        when(cardFieldFilterProvider.getDefaultList()).thenAnswer(i -> cardFieldFiltersAllValuesSelected());

        subject = new CardFieldFilterService(cardFieldFilterProvider, userPreferencesService, taskboardDatabaseService);
    }

    @Test
    public void getFilterForLoggerUser_callNecessaryMethods() {
        subject.getFilterForLoggerUser();

        verify(cardFieldFilterProvider, times(1)).getDefaultList();
        verify(userPreferencesService, times(1)).applyLoggedUserPreferencesOnCardFieldFilter(any());
    }

    @Test
    public void getIssuesSelectedByLoggedUser_ifCardFieldFiltersNotAllowIssue_thenFilter() {
        final Long ISSUE_TYPE_TO_SHOW = Long.valueOf(ISSUE_TYPE_1_VALUE);
        final Long ISSUE_TYPE_TO_HIDE = Long.valueOf(ISSUE_TYPE_2_VALUE);
        final long ISSUE_STATUS_TO_SHOW = ISSUES_CONFIGURATION_STATUS;
        final String PROJECT_TO_SHOW = PROJECT_1_VALUE;
        final String TEAM_TO_SHOW = TEAM_1_VALUE;

        setupTaskboardDatabaseMock(issuesConfigurationMock(ISSUE_STATUS_TO_SHOW, ISSUE_TYPE_TO_SHOW));
        removeSelectionFromCardFieldFilter(FieldSelector.ISSUE_TYPE, ISSUE_TYPE_TO_HIDE.toString());

        List<Issue> issues = asList(
                issueMock("I-1", PROJECT_TO_SHOW, ISSUE_TYPE_TO_SHOW, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW),
                issueMock("I-2", PROJECT_TO_SHOW, ISSUE_TYPE_TO_SHOW, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW),
                issueMock("I-3", PROJECT_TO_SHOW, ISSUE_TYPE_TO_HIDE, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW),
                issueMock("I-4", PROJECT_TO_SHOW, ISSUE_TYPE_TO_HIDE, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW)
                );

        List<Issue> issuesSelectedByLoggedUser = subject.getIssuesSelectedByLoggedUser(issues);

        assertEquals(
                "[I-1, I-2]",
                issuesSelectedByLoggedUser.stream().map(i -> i.getIssueKey()).collect(toList()).toString()
                );
    }

    @Test
    public void getIssuesSelectedByLoggedUser_ifCardFieldFiltersIsEmpty_thenReturnNoIssues() {
        final Long ISSUE_TYPE_TO_SHOW = Long.valueOf(ISSUE_TYPE_1_VALUE);
        final Long ISSUE_STATUS_TO_SHOW = ISSUES_CONFIGURATION_STATUS;
        final String PROJECT_TO_SHOW = PROJECT_1_VALUE;
        final String TEAM_TO_SHOW = TEAM_1_VALUE;

        when(cardFieldFilterProvider.getDefaultList()).thenAnswer(i -> emptyList());

        setupTaskboardDatabaseMock(issuesConfigurationMock(ISSUE_STATUS_TO_SHOW, ISSUE_TYPE_TO_SHOW));

        List<Issue> issues = asList(
                issueMock("I-1", PROJECT_TO_SHOW, ISSUE_TYPE_TO_SHOW, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW),
                issueMock("I-2", PROJECT_TO_SHOW, ISSUE_TYPE_TO_SHOW, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW),
                issueMock("I-3", PROJECT_TO_SHOW, ISSUE_TYPE_TO_SHOW, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW),
                issueMock("I-4", PROJECT_TO_SHOW, ISSUE_TYPE_TO_SHOW, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW)
                );
        
        List<Issue> issuesSelectedByLoggedUser = subject.getIssuesSelectedByLoggedUser(issues);

        assertEquals(0, issuesSelectedByLoggedUser.size());
    }

    @Test
    public void getIssuesSelectedByLoggedUser_ifIssuesConfigurationNotExistsForAnIssue_thenFilter() {
        final Long ISSUE_TYPE_TO_SHOW = Long.valueOf(ISSUE_TYPE_1_VALUE);
        final Long ISSUE_TYPE_TO_HIDE = Long.valueOf(ISSUE_TYPE_2_VALUE);
        final Long ISSUE_STATUS_TO_SHOW = ISSUES_CONFIGURATION_STATUS;
        final String PROJECT_TO_SHOW = PROJECT_1_VALUE;
        final String TEAM_TO_SHOW = TEAM_1_VALUE;

        setupTaskboardDatabaseMock(issuesConfigurationMock(ISSUE_STATUS_TO_SHOW, ISSUE_TYPE_TO_SHOW));

        List<Issue> issues = asList(
                issueMock("I-1", PROJECT_TO_SHOW, ISSUE_TYPE_TO_HIDE, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW),
                issueMock("I-2", PROJECT_TO_SHOW, ISSUE_TYPE_TO_HIDE, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW),
                issueMock("I-3", PROJECT_TO_SHOW, ISSUE_TYPE_TO_SHOW, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW),
                issueMock("I-4", PROJECT_TO_SHOW, ISSUE_TYPE_TO_SHOW, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW)
                );

        List<Issue> issuesSelectedByLoggedUser = subject.getIssuesSelectedByLoggedUser(issues);

        assertEquals(
                "[I-3, I-4]",
                issuesSelectedByLoggedUser.stream().map(i -> i.getIssueKey()).collect(toList()).toString()
                );
    }

    @Test
    public void getIssuesSelectedByLoggedUser_ifIssuesHaveTheNecessaryValues_returnAllIssues() {
        final Long ISSUE_TYPE_TO_SHOW = Long.valueOf(ISSUE_TYPE_1_VALUE);
        final Long ISSUE_STATUS_TO_SHOW = ISSUES_CONFIGURATION_STATUS;
        final String PROJECT_TO_SHOW = PROJECT_1_VALUE;
        final String TEAM_TO_SHOW = TEAM_1_VALUE;

        setupTaskboardDatabaseMock(issuesConfigurationMock(ISSUE_STATUS_TO_SHOW, ISSUE_TYPE_TO_SHOW));

        List<Issue> issues = asList(
                issueMock("I-1", PROJECT_TO_SHOW, ISSUE_TYPE_TO_SHOW, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW),
                issueMock("I-2", PROJECT_TO_SHOW, ISSUE_TYPE_TO_SHOW, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW),
                issueMock("I-3", PROJECT_TO_SHOW, ISSUE_TYPE_TO_SHOW, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW),
                issueMock("I-4", PROJECT_TO_SHOW, ISSUE_TYPE_TO_SHOW, ISSUE_STATUS_TO_SHOW, TEAM_TO_SHOW)
                );

        List<Issue> issuesSelectedByLoggedUser = subject.getIssuesSelectedByLoggedUser(issues);

        assertEquals(
                "[I-1, I-2, I-3, I-4]",
                issuesSelectedByLoggedUser.stream().map(i -> i.getIssueKey()).collect(toList()).toString()
                );
    }

    private void removeSelectionFromCardFieldFilter(FieldSelector fieldSelector, String value) {
        List<CardFieldFilter> filters = cardFieldFiltersAllValuesSelected();
        FilterFieldValue filterValueToHide = getFilterFieldValue(filters, fieldSelector, value);
        filterValueToHide.setSelected(false);
        when(cardFieldFilterProvider.getDefaultList()).thenReturn(filters);
    }

    private void setupTaskboardDatabaseMock(IssuesConfiguration... issuesConfiguration) {
        StepConfiguration stepConf = mock(StepConfiguration.class);
        when(stepConf.getIssuesConfiguration()).thenReturn(asList(issuesConfiguration));

        StageConfiguration stageConf = mock(StageConfiguration.class);
        when(stageConf.getSteps()).thenReturn(asList(stepConf));

        LaneConfiguration laneConf = mock(LaneConfiguration.class);
        when(laneConf.getStages()).thenReturn(asList(stageConf));

        when(taskboardDatabaseService.laneConfiguration()).thenReturn(asList(laneConf));
    }

    private static IssuesConfiguration issuesConfigurationMock(Long status, Long issueType) {
        IssuesConfiguration issuesConf = mock(IssuesConfiguration.class);
        when(issuesConf.matches(any(Issue.class))).thenCallRealMethod();
        when(issuesConf.getStatus()).thenReturn(status);
        when(issuesConf.getIssueType()).thenReturn(Long.valueOf(issueType));
        return issuesConf;
    }

    private Issue issueMock(String issueKey, String projectKey, long type, long status, String team) {
        Issue issue = mock(Issue.class);
        when(issue.getIssueKey()).thenReturn(issueKey);
        when(issue.getProjectKey()).thenReturn(projectKey);
        when(issue.getType()).thenReturn(type);
        when(issue.getStatus()).thenReturn(status);
        when(issue.getTeams()).thenReturn(Stream.of(team).map(teamName -> new CardTeam(teamName, 0L)).collect(toSet()));
        return issue;
    }

}
