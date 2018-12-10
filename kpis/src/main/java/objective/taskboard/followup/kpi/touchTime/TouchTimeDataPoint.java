package objective.taskboard.followup.kpi.touchTime;

import java.time.Instant;
import java.time.ZonedDateTime;

class TouchTimeDataPoint {
    public final String issueKey;
    public final String issueType;
    public final String issueStatus;
    public final double effortInHours;
    public final Instant startProgressingDate;
    public final Instant endProgressingDate;
    
    public TouchTimeDataPoint(String issueKey, String issueType, String issueStatus, double effortInHours, ZonedDateTime startProgressingDate, ZonedDateTime endProgressingDate) {
        this.issueKey = issueKey;
        this.issueType = issueType;
        this.issueStatus = issueStatus;
        this.effortInHours = effortInHours;
        this.startProgressingDate = startProgressingDate.toInstant();
        this.endProgressingDate = endProgressingDate.toInstant();
    }
}