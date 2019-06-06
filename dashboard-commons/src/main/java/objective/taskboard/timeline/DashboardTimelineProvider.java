package objective.taskboard.timeline;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.springframework.stereotype.Service;

import objective.taskboard.configuration.DashboardConfiguration;
import objective.taskboard.configuration.DashboardConfigurationService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.utils.Clock;

@Service
public class DashboardTimelineProvider {

    private final DashboardConfigurationService dashboardConfigurationService;
    private final Clock clock;
    private final ProjectService projectService;

    public DashboardTimelineProvider(DashboardConfigurationService dashboardConfigurationService, ProjectService projectService, Clock clock) {
        this.dashboardConfigurationService = dashboardConfigurationService;
        this.projectService = projectService;
        this.clock = clock;
    }

    public DashboardTimeline getDashboardTimelineForProject(String projectKey) {    	
    	return dashboardConfigurationService.retrieveConfiguration(projectKey)
    			.map(this::getDashboardTimelineFromDashboardConfiguration)
    			.orElseGet(() -> getDashboardTimelineFromProjectService(projectKey));
    }

    private DashboardTimeline getDashboardTimelineFromDashboardConfiguration(DashboardConfiguration dashboardConfiguration) {
        LocalDate today = LocalDateTime.ofInstant(clock.now(), ZoneOffset.UTC).toLocalDate();
        int daysToDisplay = dashboardConfiguration.getTimelineDaysToDisplay();
        LocalDate startDate = calculateStartDateTodayIncluded(today, daysToDisplay);
        return new DashboardTimeline(Optional.of(startDate), Optional.of(today));
    }

    private DashboardTimeline getDashboardTimelineFromProjectService(String projectKey) {
        return projectService.getTaskboardProject(projectKey)
        		.map(p -> new DashboardTimeline(p.getStartDate(), p.getDeliveryDate()))
        		.orElseGet(() -> new DashboardTimeline(Optional.empty(), Optional.empty()));
    }

    private LocalDate calculateStartDateTodayIncluded(LocalDate today, int daysToSubtract) {
        return today.minusDays(daysToSubtract - 1L);
    }

}
