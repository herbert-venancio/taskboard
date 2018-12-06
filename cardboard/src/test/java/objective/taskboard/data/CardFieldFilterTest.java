package objective.taskboard.data;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toSet;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.ISSUE_TYPE_1_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.PROJECT_1_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.TEAM_1_VALUE;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.VALUE_NOT_REGISTERED;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.getFilterFieldValue;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.issueTypeFilterFieldsValuesAllSelected;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.projectFilterFieldsValuesAllSelected;
import static objective.taskboard.filterPreferences.CardFieldFilterUtils.teamFilterFieldsValuesAllSelected;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.data.CardFieldFilter.FieldSelector;
import objective.taskboard.data.Issue.CardTeam;

@RunWith(MockitoJUnitRunner.class)
public class CardFieldFilterTest {

    private CardFieldFilter subject;

    @Test
    public void fieldSelectorIssueType_givenAllValuesSelected_whenIssueHasARegisteredIssueType_thenIsSelected() {
        subject = new CardFieldFilter(FieldSelector.ISSUE_TYPE, issueTypeFilterFieldsValuesAllSelected());

        boolean isSelected = subject.isIssueSelected(issueMock(null, ISSUE_TYPE_1_VALUE, null));
        assertEquals(true, isSelected);
    }

    @Test
    public void fieldSelectorIssueType_givenAllValuesSelected_whenIssueHasANotRegisteredIssueType_thenIsNotSelected() {
        subject = new CardFieldFilter(FieldSelector.ISSUE_TYPE, issueTypeFilterFieldsValuesAllSelected());

        boolean isSelected = subject.isIssueSelected(issueMock(null, VALUE_NOT_REGISTERED, null));
        assertEquals(false, isSelected);
    }

    @Test
    public void fieldSelectorIssueType_givenOneIssueTypeValueNotSelected_whenIssueHasThisIssueType_thenIsNotSelected() {
        final String ISSUE_TYPE_NOT_SELECTED = ISSUE_TYPE_1_VALUE;

        subject = new CardFieldFilter(FieldSelector.ISSUE_TYPE, issueTypeFilterFieldsValuesAllSelected());

        FilterFieldValue filterValueToDeselect = getFilterFieldValue(subject, ISSUE_TYPE_NOT_SELECTED);
        filterValueToDeselect.setSelected(false);

        boolean isSelected = subject.isIssueSelected(issueMock(null, ISSUE_TYPE_NOT_SELECTED, null));
        assertEquals(false, isSelected);
    }

    @Test
    public void fieldSelectorProject_givenAllValuesSelected_whenIssueHasARegisteredProject_thenIsSelected() {
        subject = new CardFieldFilter(FieldSelector.PROJECT, projectFilterFieldsValuesAllSelected());

        boolean isSelected = subject.isIssueSelected(issueMock(PROJECT_1_VALUE, null, null));
        assertEquals(true, isSelected);
    }

    @Test
    public void fieldSelectorProject_givenAllValuesSelected_whenIssueHasANotRegisteredProject_thenIsNotSelected() {
        subject = new CardFieldFilter(FieldSelector.PROJECT, issueTypeFilterFieldsValuesAllSelected());

        boolean isSelected = subject.isIssueSelected(issueMock(VALUE_NOT_REGISTERED, null, null));
        assertEquals(false, isSelected);
    }

    @Test
    public void fieldSelectorProject_givenOneProjectValueNotSelected_whenIssueHasThisProject_thenIsNotSelected() {
        final String PROJECT_NOT_SELECTED = PROJECT_1_VALUE;

        subject = new CardFieldFilter(FieldSelector.PROJECT, projectFilterFieldsValuesAllSelected());

        FilterFieldValue filterValueToDeselect = getFilterFieldValue(subject, PROJECT_NOT_SELECTED);
        filterValueToDeselect.setSelected(false);

        boolean isSelected = subject.isIssueSelected(issueMock(PROJECT_NOT_SELECTED, null, null));
        assertEquals(false, isSelected);
    }

    @Test
    public void fieldSelectorTeam_givenAllValuesSelected_whenIssueHasAVisibleTeam_thenIsSelected() {
        subject = new CardFieldFilter(FieldSelector.TEAM, teamFilterFieldsValuesAllSelected());

        boolean isSelected = subject.isIssueSelected(issueMock(null, null, asList(TEAM_1_VALUE)));
        assertEquals(true, isSelected);
    }

    @Test
    public void fieldSelectorTeam_givenOneTeamValueNotSelected_whenIssueHasThisTeam_thenIsNotSelected() {
        final String TEAM_NOT_SELECTED = TEAM_1_VALUE;

        subject = new CardFieldFilter(FieldSelector.TEAM, teamFilterFieldsValuesAllSelected());

        FilterFieldValue filterValueToDeselect = getFilterFieldValue(subject, TEAM_NOT_SELECTED);
        filterValueToDeselect.setSelected(false);

        boolean isSelected = subject.isIssueSelected(issueMock(null, null, asList(TEAM_NOT_SELECTED)));
        assertEquals(false, isSelected);
    }

    @Test
    public void fieldSelectorTeam_givenAllValuesSelected_whenIssueHasOnlyInvisibleTeams_thenIsSelected() {
        final String TEAM_INVISIBLE = VALUE_NOT_REGISTERED;

        subject = new CardFieldFilter(FieldSelector.TEAM, teamFilterFieldsValuesAllSelected());

        boolean isSelected = subject.isIssueSelected(issueMock(null, null, asList(TEAM_INVISIBLE)));
        assertEquals(true, isSelected);
    }

    @Test
    public void fieldSelectorTeam_givenAllValuesSelected_whenIssueHasInvisibleAndVisibleTeams_thenIsSelected() {
        final String TEAM_INVISIBLE = VALUE_NOT_REGISTERED;

        subject = new CardFieldFilter(FieldSelector.TEAM, teamFilterFieldsValuesAllSelected());

        boolean isSelected = subject.isIssueSelected(issueMock(null, null, asList(TEAM_INVISIBLE, TEAM_1_VALUE)));
        assertEquals(true, isSelected);
    }

    @Test
    public void fieldSelectorTeam_givenOneTeamValueNotSelected_whenIssueHasThisTeamAndAInvisibleTeam_thenIsNotSelected() {
        final String TEAM_NOT_SELECTED = TEAM_1_VALUE;
        final String TEAM_INVISIBLE = VALUE_NOT_REGISTERED;

        subject = new CardFieldFilter(FieldSelector.TEAM, teamFilterFieldsValuesAllSelected());

        FilterFieldValue filterValueToDeselect = getFilterFieldValue(subject, TEAM_NOT_SELECTED);
        filterValueToDeselect.setSelected(false);

        boolean isSelected = subject.isIssueSelected(issueMock(null, null, asList(TEAM_NOT_SELECTED, TEAM_INVISIBLE)));
        assertEquals(false, isSelected);
    }

    private Issue issueMock(String projectKey, String type, List<String> teams) {
        Issue issue = mock(Issue.class);
        if (!isEmpty(projectKey))
            when(issue.getProjectKey()).thenReturn(projectKey);
        if (!isEmpty(type))
            when(issue.getType()).thenReturn(Long.valueOf(type));
        if (teams != null)
            when(issue.getTeams()).thenReturn(teams.stream().map(teamName -> new CardTeam(teamName, 0L)).collect(toSet()));
        return issue;
    }

}
