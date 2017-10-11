package objective.taskboard.testUtils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateTimeUtilSupport {
    public static Date date(int year, int month, int date) {
        return new Date(ZonedDateTime.of(year, month, date, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
    
    public static Date date(int year, int month, int date, int hour, int min) {
        return new Date(ZonedDateTime.of(year, month, date, hour, min, 0, 0, ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
}
