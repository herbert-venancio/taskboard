package objective.taskboard.followup.kpi.touchTime;

class TouchTimeDataPoint {
    public final String issueKey;
    public final String issueType;
    public final String issueStatus;
    public final double effortInHours;
    
    public TouchTimeDataPoint(String issueKey, String issueType, String issueStatus, double effortInHours) {
        this.issueKey = issueKey;
        this.issueType = issueType;
        this.issueStatus = issueStatus;
        this.effortInHours = effortInHours;
    }
}