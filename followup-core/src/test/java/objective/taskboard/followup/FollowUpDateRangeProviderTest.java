package objective.taskboard.followup;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.FollowUpDateRangeProvider.FollowUpProjectDataRangeDTO;
import objective.taskboard.jira.FrontEndMessageException;
import objective.taskboard.jira.ProjectService;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpDateRangeProviderTest {

    private static final String PROJECT_KEY = "TEST";
    private static final LocalDate START_DATE = LocalDate.of(2017, 01, 01);
    private static final LocalDate DELIVERY_DATE = LocalDate.of(2018, 01, 01);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private FollowUpDateRangeProvider subject;

    @Test
    public void whenGetDateRange_ifTheProjectExists_thenReturnTheValue() {
        ProjectFilterConfiguration taskboardProject = new ProjectFilterConfiguration(PROJECT_KEY, 1L);
        taskboardProject.setStartDate(START_DATE);
        taskboardProject.setDeliveryDate(DELIVERY_DATE);

        when(projectService.getTaskboardProject(PROJECT_KEY)).thenReturn(Optional.of(taskboardProject));

        FollowUpProjectDataRangeDTO dateRangeData = subject.getDateRangeData(PROJECT_KEY);
        assertEquals(PROJECT_KEY, dateRangeData.projectKey);
        assertEquals(START_DATE, dateRangeData.startDate);
        assertEquals(DELIVERY_DATE, dateRangeData.deliveryDate);
    }

    @Test
    public void whenGetDateRange_ifTheProjectDoentExist_thenThrowException() {
        when(projectService.getTaskboardProject(PROJECT_KEY)).thenReturn(Optional.empty());

        expectedException.expect(FrontEndMessageException.class);
        expectedException.expectMessage("Project " + PROJECT_KEY + " doesn't exist");

        subject.getDateRangeData(PROJECT_KEY);
    }

}
