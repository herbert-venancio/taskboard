package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.Date;

public class ThroughputDataPoint {
    public final Date date;
    public final String issueType;
    public final Long count;

    public ThroughputDataPoint(ZonedDateTime date, String issueType, Long count) {
        this.date = Date.from(date.toInstant());
        this.issueType = issueType;
        this.count = count;
    }
}
