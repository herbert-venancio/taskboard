package objective.taskboard.timeline;

import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.jira.FrontEndMessageException;
import objective.taskboard.timeline.FollowUpDateRangeProvider.FollowUpProjectDataRangeDTO;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpDateRangeProviderTest {

    private static final String PROJECT_KEY = "TEST";
    private static final LocalDate START_DATE = LocalDate.of(2017, 01, 01);
    private static final LocalDate DELIVERY_DATE = LocalDate.of(2018, 01, 01);

    private DashboardTimelineProvider dashboardTimelineProvider = Mockito.mock(DashboardTimelineProvider.class);

    private FollowUpDateRangeProvider subject;

    @Before
    public void setUp() {
        subject = new FollowUpDateRangeProvider(dashboardTimelineProvider);
    }

    @Test
    public void givenNonEmptyTimeline_whenGetDateRange_thenHappyPath() {
        mockDashboardTimelineProviderToReturnNonEmptyTimeline();

        FollowUpProjectDataRangeDTO dateRangeData = subject.getDateRangeData(PROJECT_KEY);
        Assertions.assertThat(dateRangeData.projectKey).isEqualTo(PROJECT_KEY);
        Assertions.assertThat(dateRangeData.startDate).isEqualTo(START_DATE);
        Assertions.assertThat(dateRangeData.deliveryDate).isEqualTo(DELIVERY_DATE);
    }

    @Test
    public void givenEmptyTimeline_whenGetDateRange_thenThrowException() {
        mockDashboardTimelineProviderToReturnEmptyTimeline();

        Assertions.assertThatExceptionOfType(FrontEndMessageException.class)
            .isThrownBy(() -> subject.getDateRangeData(PROJECT_KEY))
            .withMessage("No \"Start Date\" or \"Delivery Date\" configuration found for project " + PROJECT_KEY + ".");
    }

    @Test
    public void givenTimelineHasOnlyStartDate_whenGetDateRange_thenThrowException() {
        mockDashboardTimelineProviderToReturnStartDateOnlyTimeline();

        Assertions.assertThatExceptionOfType(FrontEndMessageException.class)
            .isThrownBy(() -> subject.getDateRangeData(PROJECT_KEY))
            .withMessage("No \"Start Date\" or \"Delivery Date\" configuration found for project " + PROJECT_KEY + ".");
    }
    
    @Test
    public void givenTimelineHasOnlyEndDate_whenGetDateRange_thenThrowException() {
        mockDashboardTimelineProviderToReturnEndDateOnlyTimeline();

        Assertions.assertThatExceptionOfType(FrontEndMessageException.class)
            .isThrownBy(() -> subject.getDateRangeData(PROJECT_KEY))
            .withMessage("No \"Start Date\" or \"Delivery Date\" configuration found for project " + PROJECT_KEY + ".");
    }
    private void mockDashboardTimelineProviderToReturnNonEmptyTimeline() {
        DashboardTimeline timeline = new DashboardTimeline(Optional.of(START_DATE), Optional.of(DELIVERY_DATE));
        when(dashboardTimelineProvider.getDashboardTimelineForProject(PROJECT_KEY)).thenReturn(timeline);
    }

    private void mockDashboardTimelineProviderToReturnEmptyTimeline() {
        DashboardTimeline timeline = new DashboardTimeline(Optional.empty(), Optional.empty());
        when(dashboardTimelineProvider.getDashboardTimelineForProject(PROJECT_KEY)).thenReturn(timeline);
    }
    
    private void mockDashboardTimelineProviderToReturnStartDateOnlyTimeline() {
        DashboardTimeline timeline = new DashboardTimeline(Optional.of(START_DATE), Optional.empty());
        when(dashboardTimelineProvider.getDashboardTimelineForProject(PROJECT_KEY)).thenReturn(timeline);
    }
    
    private void mockDashboardTimelineProviderToReturnEndDateOnlyTimeline() {
        DashboardTimeline timeline = new DashboardTimeline(Optional.empty(), Optional.of(DELIVERY_DATE));
        when(dashboardTimelineProvider.getDashboardTimelineForProject(PROJECT_KEY)).thenReturn(timeline);
    }
}
