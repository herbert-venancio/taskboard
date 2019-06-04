package objective.taskboard.timeline;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.jira.FrontEndMessageException;

@Service
public class FollowUpDateRangeProvider {

    private final DashboardTimelineProvider dashboardTimelineProvider;

    @Autowired
    public FollowUpDateRangeProvider(DashboardTimelineProvider dashboardTimelineProvider) {
        this.dashboardTimelineProvider = dashboardTimelineProvider;
    }

    public FollowUpProjectDataRangeDTO getDateRangeData(String projectKey) {
        DashboardTimeline timeline = dashboardTimelineProvider.getDashboardTimelineForProject(projectKey);
        if (!timeline.hasBothDates())
            throw new FrontEndMessageException("No \"Start Date\" or \"Delivery Date\" configuration found for project " + projectKey + ".");
        
        return new FollowUpProjectDataRangeDTO(projectKey, timeline);
    }

    public static class FollowUpProjectDataRangeDTO {
        public final String projectKey;
        public final LocalDate startDate;
        public final LocalDate deliveryDate;
        public FollowUpProjectDataRangeDTO(String projectKey, DashboardTimeline timeline) {
            this.projectKey = projectKey;
            this.startDate = timeline.startDate.orElse(null);
            this.deliveryDate = timeline.endDate.orElse(null);
        }
    }

}
