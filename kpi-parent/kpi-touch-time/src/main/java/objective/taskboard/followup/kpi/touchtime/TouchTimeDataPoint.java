package objective.taskboard.followup.kpi.touchtime;

import java.time.Instant;

class TouchTimeDataPoint {
    public final String issueKey;
    public final String issueType;
    public final String issueStatus;
    public final double effortInHours;
    public final Instant startProgressingDate;
    public final Instant endProgressingDate;
    
    public TouchTimeDataPoint(String issueKey, String issueType, String issueStatus, double effortInHours, Instant startProgressingDate, Instant endProgressingDate) {
        this.issueKey = issueKey;
        this.issueType = issueType;
        this.issueStatus = issueStatus;
        this.effortInHours = effortInHours;
        this.startProgressingDate = startProgressingDate;
        this.endProgressingDate = endProgressingDate;
    }
}