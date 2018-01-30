package objective.taskboard.followup;

import static java.util.Arrays.asList;
import static objective.taskboard.followup.FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
import static objective.taskboard.followup.FromJiraRowService.BUG;
import static objective.taskboard.followup.FromJiraRowService.INTANGIBLE;
import static objective.taskboard.followup.FromJiraRowService.NEW_SCOPE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.jira.JiraProperties;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.data.Status;

@RunWith(MockitoJUnitRunner.class)
public class FromJiraRowServiceTest {

    private static final String WRONG_VALUE = "Wrong value";

    private static final long DONE_ID = 1L;
    private static final String DONE_NAME = "Done";

    private static final long CANCELED_ID = 2L;
    private static final String CANCELED_NAME = "Canceled";

    private FromJiraDataRow row;

    @Mock
    private JiraProperties jiraProperties;

    @Mock
    private MetadataService metadataService;

    @InjectMocks
    private FromJiraRowService subject;

    @Before
    public void setup() {
        when(jiraProperties.getStatusesCompletedIds()).thenReturn(asList(DONE_ID));
        when(jiraProperties.getStatusesCanceledIds()).thenReturn(asList(CANCELED_ID));
        when(metadataService.getStatusById(DONE_ID)).thenReturn(new Status(DONE_ID, DONE_NAME, null));
        when(metadataService.getStatusById(CANCELED_ID)).thenReturn(new Status(CANCELED_ID, CANCELED_NAME, null));
    }

    @Test
    public void givenSubtaskClassOfServiceEqualsIntengible_returnIsIntangibleTrue_elseReturnFalse() {
        // Demand
        row = new FromJiraDataRow();
        row.demandClassOfService = INTANGIBLE;
        assertTrue(subject.isIntangible(row));

        row.demandClassOfService = WRONG_VALUE;
        assertFalse(subject.isIntangible(row));

        // Task
        row = new FromJiraDataRow();
        row.taskClassOfService = INTANGIBLE;
        assertTrue(subject.isIntangible(row));

        row.subtaskClassOfService = WRONG_VALUE;
        assertFalse(subject.isIntangible(row));

        // Subtask
        row = new FromJiraDataRow();
        row.subtaskClassOfService = INTANGIBLE;
        assertTrue(subject.isIntangible(row));

        row.subtaskClassOfService = WRONG_VALUE;
        assertFalse(subject.isIntangible(row));
    }

    @Test
    public void givenLabelsContainingNewScope_returnIsNewScopeTrue_elseReturnFalse() {
        // Demand
        row = new FromJiraDataRow();
        row.demandLabels = NEW_SCOPE;
        assertTrue(subject.isNewScope(row));

        row.demandLabels = asString(WRONG_VALUE, NEW_SCOPE, WRONG_VALUE);
        assertTrue(subject.isNewScope(row));

        row.demandLabels = asString(WRONG_VALUE, WRONG_VALUE);
        assertFalse(subject.isNewScope(row));

        // Task
        row = new FromJiraDataRow();
        row.taskLabels = NEW_SCOPE;
        assertTrue(subject.isNewScope(row));

        row.taskLabels = asString(WRONG_VALUE, NEW_SCOPE, WRONG_VALUE);
        assertTrue(subject.isNewScope(row));

        row.taskLabels = asString(WRONG_VALUE, WRONG_VALUE);
        assertFalse(subject.isNewScope(row));

        // Demand
        row = new FromJiraDataRow();
        row.subtaskLabels = NEW_SCOPE;
        assertTrue(subject.isNewScope(row));

        row.subtaskLabels = asString(WRONG_VALUE, NEW_SCOPE, WRONG_VALUE);
        assertTrue(subject.isNewScope(row));

        row.subtaskLabels = asString(WRONG_VALUE, WRONG_VALUE);
        assertFalse(subject.isNewScope(row));
    }

    @Test
    public void givenTaskTypeBug_returnIsReworkTrue_elseReturnFalse() {
        row = new FromJiraDataRow();
        row.taskType = BUG;
        assertTrue(subject.isRework(row));

        row.taskType = WRONG_VALUE;
        assertFalse(subject.isRework(row));
    }

    @Test
    public void givenStatusNameDoneOrCanceled_returnIsBaselineDoneTrue_elseReturnFalse() {
        // Demand
        row = new FromJiraDataRow();
        row.demandStatus = DONE_NAME;
        assertTrue(subject.isBaselineDone(row));

        row = new FromJiraDataRow();
        row.demandStatus = CANCELED_NAME;
        assertTrue(subject.isBaselineDone(row));

        row.demandStatus = WRONG_VALUE;
        assertFalse(subject.isBaselineDone(row));

        // Task
        row = new FromJiraDataRow();
        row.taskStatus = DONE_NAME;
        assertTrue(subject.isBaselineDone(row));

        row = new FromJiraDataRow();
        row.taskStatus = CANCELED_NAME;
        assertTrue(subject.isBaselineDone(row));

        row.taskStatus = WRONG_VALUE;
        assertFalse(subject.isBaselineDone(row));

        // Subtask
        row = new FromJiraDataRow();
        row.subtaskStatus = DONE_NAME;
        assertTrue(subject.isBaselineDone(row));

        row = new FromJiraDataRow();
        row.subtaskStatus = CANCELED_NAME;
        assertTrue(subject.isBaselineDone(row));

        row.subtaskStatus = WRONG_VALUE;
        assertFalse(subject.isBaselineDone(row));
    }

    @Test
    public void givenNoOtherType_returnIsBaselineBacklogTrue_elseReturnFalse() {
        // isBaselineBacklog
        row = new FromJiraDataRow();
        assertTrue(subject.isBaselineBacklog(row));

        // isIntangible
        row = new FromJiraDataRow();
        row.demandClassOfService = INTANGIBLE;
        assertFalse(subject.isBaselineBacklog(row));

        // isNewScope
        row = new FromJiraDataRow();
        row.demandLabels = NEW_SCOPE;
        assertFalse(subject.isBaselineBacklog(row));

        // isRework
        row = new FromJiraDataRow();
        row.taskType = BUG;
        assertFalse(subject.isBaselineBacklog(row));

        // isBaselineDone
        row = new FromJiraDataRow();
        row.demandStatus = DONE_NAME;
        assertFalse(subject.isBaselineBacklog(row));
    }

    @Test
    public void givenQueryTypeSubtaskPlan_returnIsPlannedTrue_elseReturnFalse() {
        row = new FromJiraDataRow();
        row.queryType = QUERY_TYPE_SUBTASK_PLAN;
        assertTrue(subject.isPlanned(row));

        row = new FromJiraDataRow();
        row.queryType = WRONG_VALUE;
        assertFalse(subject.isPlanned(row));
    }

    @Test
    public void givenQueryTypeSubtaskPlan_returnIsBallparkFalse_elseReturnTrue() {
        row = new FromJiraDataRow();
        row.queryType = QUERY_TYPE_SUBTASK_PLAN;
        assertFalse(subject.isBallpark(row));

        row = new FromJiraDataRow();
        row.queryType = WRONG_VALUE;
        assertTrue(subject.isBallpark(row));
    }

    private String asString(String... values) {
        return String.join(",", values);
    }

}
