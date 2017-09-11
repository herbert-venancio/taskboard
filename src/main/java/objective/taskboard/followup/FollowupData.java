package objective.taskboard.followup;

public class FollowupData {
    public final FromJiraDataSet fromJiraDs;
    public final AnalyticsTransitionsDataSet analyticsTransitionsDs;
    
    public FollowupData(FromJiraDataSet fromJiraDs, AnalyticsTransitionsDataSet analyticsTransitionsDs) {
        this.fromJiraDs = fromJiraDs;
        this.analyticsTransitionsDs = analyticsTransitionsDs;
    }
}
