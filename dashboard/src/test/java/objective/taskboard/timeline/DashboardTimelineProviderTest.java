package objective.taskboard.timeline;

import static java.time.Month.FEBRUARY;
import static java.time.Month.JANUARY;

import java.time.LocalDate;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import objective.taskboard.configuration.DashboardConfiguration;
import objective.taskboard.configuration.DashboardConfigurationService;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.testUtils.FixedClock;

public class DashboardTimelineProviderTest {

    private static final String DEFAULT_PROJECT_KEY = "TEST";
    private static final String TODAY = "2019-02-01";
    private static final FixedClock TODAY_FIXED_CLOCK = todayFixedClock();
    
    private DashboardTimelineProvider subject;
    private DashboardConfigurationService dashboardConfigurationService = Mockito.mock(DashboardConfigurationService.class);
    private ProjectService projectService = Mockito.mock(ProjectService.class);
    private ProjectFilterConfiguration defaultProjectFilterConfiguration;
    
    private static FixedClock todayFixedClock() {
        FixedClock fixedClock = new FixedClock();
        fixedClock.setNow(String.format("%sT00:00:00Z", TODAY));
        return fixedClock;
    }
    
    @Before
    public void setUp() {
        defaultProjectFilterConfiguration = new ProjectFilterConfiguration(DEFAULT_PROJECT_KEY, 0L);
        subject = new DashboardTimelineProvider(dashboardConfigurationService, projectService, TODAY_FIXED_CLOCK);
    }
    
    @Test
    public void givenThirtyDaysToDisplayConfigured_whenGetDashboardTimeline_thenUseConfigurationToCalculteTimelineRange() {
        DashboardConfiguration thirtyDaysToDisplayConfiguration = new DashboardConfiguration();
        thirtyDaysToDisplayConfiguration.setTimelineDaysToDisplay(30);
        mockDashboardConfigurationServiceToReturn(thirtyDaysToDisplayConfiguration);
        
        DashboardTimeline timeline = subject.getDashboardTimelineForProject(DEFAULT_PROJECT_KEY);
        
        Assertions.assertThat(timeline.startDate).contains(LocalDate.of(2019, JANUARY, 3));
        Assertions.assertThat(timeline.endDate).contains(LocalDate.of(2019, FEBRUARY, 1));
    }

    @Test
    public void givenTenDaysToDisplayConfigured_whenGetDashboardTimeline_thenUseConfigurationToCalculteTimelineRange() {
        DashboardConfiguration tenDaysToDisplayConfiguration = new DashboardConfiguration();
        tenDaysToDisplayConfiguration.setTimelineDaysToDisplay(10);
        mockDashboardConfigurationServiceToReturn(tenDaysToDisplayConfiguration);
        
        DashboardTimeline timeline = subject.getDashboardTimelineForProject(DEFAULT_PROJECT_KEY);
        
        Assertions.assertThat(timeline.startDate).contains(LocalDate.of(2019, JANUARY, 23));
        Assertions.assertThat(timeline.endDate).contains(LocalDate.of(2019, FEBRUARY, 1));
    }
    
    @Test
    public void givenMissingDashboardConfigurationAndHasNoDatesConfiguredToProject_whenGetDashboardTimeline_thenEmptyTimeline() {
        mockDashboardConfigurationServiceToReturnEmptyConfiguration();
        
        mockProjectServiceToReturnDefaultProjectConfiguration();
        
        DashboardTimeline timeline = subject.getDashboardTimelineForProject(DEFAULT_PROJECT_KEY);
        
        Assertions.assertThat(timeline.startDate).isEmpty();
        Assertions.assertThat(timeline.endDate).isEmpty();
    }

    @Test
    public void givenMissingDashboardConfigurationAndMissingProject_whenGetDashboardTimeline_thenEmptyTimeline() {
        mockDashboardConfigurationServiceToReturnEmptyConfiguration();
        mockProjectServiceForMissingProject();
    
        DashboardTimeline timeline = subject.getDashboardTimelineForProject(DEFAULT_PROJECT_KEY);
        
        Assertions.assertThat(timeline.startDate).isEmpty();
        Assertions.assertThat(timeline.endDate).isEmpty();
    }

    @Test
    public void givenMissingDashboardConfigurationAndHasOnlyStartDateConfiguredToProject_whenGetDashboardTimeline_thenTimelineStartDatePresentAndEndDateEmpty() {
        mockDashboardConfigurationServiceToReturnEmptyConfiguration();
        defaultProjectFilterConfiguration.setStartDate(LocalDate.of(2019, JANUARY, 1));
        mockProjectServiceToReturnDefaultProjectConfiguration();

        DashboardTimeline timeline = subject.getDashboardTimelineForProject(DEFAULT_PROJECT_KEY);
        
        Assertions.assertThat(timeline.startDate).contains(LocalDate.of(2019, JANUARY, 1));
        Assertions.assertThat(timeline.endDate).isEmpty();
    }

    @Test
    public void givenMissingDashboardConfigurationAndHasOnlyDeliveryDateConfiguredToProject_whenGetDashboardTimeline_thenTimelineStartDateEmptyAndEndDatePresent() {
        mockDashboardConfigurationServiceToReturnEmptyConfiguration();
        defaultProjectFilterConfiguration.setDeliveryDate(LocalDate.of(2019, JANUARY, 1));
        mockProjectServiceToReturnDefaultProjectConfiguration();

        DashboardTimeline timeline = subject.getDashboardTimelineForProject(DEFAULT_PROJECT_KEY);
        
        Assertions.assertThat(timeline.startDate).isEmpty();
        Assertions.assertThat(timeline.endDate).contains(LocalDate.of(2019, JANUARY, 1));
    }
    
    @Test
    public void givenMissingDashboardConfigurationAndHasDatesConfiguredToProject_whenGetDashboardTimeline_thenTimelineBothDatesPresent() {
        mockDashboardConfigurationServiceToReturnEmptyConfiguration();
        defaultProjectFilterConfiguration.setStartDate(LocalDate.of(2019, JANUARY, 1));
        defaultProjectFilterConfiguration.setDeliveryDate(LocalDate.of(2019, FEBRUARY, 1));
        mockProjectServiceToReturnDefaultProjectConfiguration();

        DashboardTimeline timeline = subject.getDashboardTimelineForProject(DEFAULT_PROJECT_KEY);
        
        Assertions.assertThat(timeline.startDate).contains(LocalDate.of(2019, JANUARY, 1));
        Assertions.assertThat(timeline.endDate).contains(LocalDate.of(2019, FEBRUARY, 1));
    }
    
    private void mockDashboardConfigurationServiceToReturnEmptyConfiguration() {
        mockDashboardConfigurationServiceToReturn(Optional.empty());
    }

    private void mockDashboardConfigurationServiceToReturn(DashboardConfiguration dashboardConfiguration) {
        mockDashboardConfigurationServiceToReturn(Optional.of(dashboardConfiguration));
    }
    
    private void mockDashboardConfigurationServiceToReturn(Optional<DashboardConfiguration> dashboardConfiguration) {
        Mockito.when(dashboardConfigurationService.retrieveConfiguration(DEFAULT_PROJECT_KEY)).thenReturn(dashboardConfiguration);
    }

    private void mockProjectServiceToReturnDefaultProjectConfiguration() {
        Mockito.when(projectService.getTaskboardProject(DEFAULT_PROJECT_KEY)).thenReturn(Optional.of(defaultProjectFilterConfiguration));
    }

    private void mockProjectServiceForMissingProject() {
        Mockito.when(projectService.getTaskboardProject(DEFAULT_PROJECT_KEY)).thenReturn(Optional.empty());
    }
    
}
