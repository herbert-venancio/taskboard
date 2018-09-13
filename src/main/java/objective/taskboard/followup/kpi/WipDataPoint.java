package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.Date;

public class WipDataPoint {
    public final Date date;
    public final String type;
    public final String status;
    public final Long count;

    public WipDataPoint(ZonedDateTime date, String type, String status, Long count) {
        this.date = Date.from(date.toInstant());
        this.type = type;
        this.status = status;
        this.count = count;
    }
}
