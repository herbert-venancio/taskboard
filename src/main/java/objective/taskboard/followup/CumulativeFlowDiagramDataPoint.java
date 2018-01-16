package objective.taskboard.followup;

import java.time.ZonedDateTime;
import java.util.Date;

public class CumulativeFlowDiagramDataPoint {

    public final String type;
    public final Date date;
    public final int count;

    public CumulativeFlowDiagramDataPoint(String type, ZonedDateTime date, int count) {
        this.type = type;
        this.count = count;
        this.date = Date.from(date.toInstant());
    }

    @Override
    public String toString() {
        return "CumulativeFlowDiagramDataPoint [type=" + type + ", date=" + date + ", count=" + count + "]";
    }
}
