package objective.taskboard.followup;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import objective.taskboard.utils.DateTimeUtils;

public class WipRow implements TransitionDataRow{
    
    public final ZonedDateTime date;
    public final String type;
    public final String status;
    public final Long count;

    public WipRow(ZonedDateTime date, String type, String status, Long count) {
        this.date = date;
        this.type = type;
        this.status = status;
        this.count = count;
    }

    @Override
    public List<String> getAsStringList() {
        return Arrays.asList(DateTimeUtils.toStringExcelFormat(date),type,status,count.toString());
    }

}
