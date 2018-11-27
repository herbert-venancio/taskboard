package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.Date;

public class WipDataPoint {
    public final Date date;
    public final String issueType;
    public final String issueStatus;
    public final double average;

    public WipDataPoint(ZonedDateTime date, String type, String status, double average) {
        this.date = Date.from(date.toInstant());
        this.issueType = type;
        this.issueStatus = status;
        this.average = average;
    }
}
