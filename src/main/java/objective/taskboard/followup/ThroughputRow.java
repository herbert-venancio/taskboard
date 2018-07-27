package objective.taskboard.followup;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import objective.taskboard.utils.DateTimeUtils;

public class ThroughputRow implements TransitionDataRow {

    public final ZonedDateTime date;
    public final String type;
    public final Long count;

    public ThroughputRow(ZonedDateTime date, String type, Long count) {
        this.date = date;
        this.type = type;
        this.count = count;
    }

    @Override
    public List<String> getAsStringList() {
        return Arrays.asList(DateTimeUtils.toStringExcelFormat(date),type,count.toString());
    }
    
}
