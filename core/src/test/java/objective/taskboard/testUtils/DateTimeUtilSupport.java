package objective.taskboard.testUtils;

import java.time.Instant;
import static objective.taskboard.utils.DateTimeUtils.parseStringToDate;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateTimeUtilSupport {
    public static Date date(int year, int month, int date) {
        return date(year, month, date, ZoneId.systemDefault());
    }
    
    public static Date date(int year, int month, int date, ZoneId zone) {
        return date(year, month, date, 0, 0, zone);
    }
    
    public static Date date(int year, int month, int date, int hour, int min) {
        return date(year, month, date, hour, min, ZoneId.systemDefault());
    }

    public static Date date(int year, int month, int date, int hour, int min, ZoneId zone) {
        return Date.from(ZonedDateTime.of(year, month, date, hour, min, 0, 0, zone).toInstant());
    }
    
    public static Date date(LocalDate d, ZoneId zone) {
        return Date.from(d.atStartOfDay().atZone(zone).toInstant());
    }
    
    public static long parseDateStringAsMilliseconds(String date) {
        return parseStringToDate(date).getTime();
    }

    public static Instant getInstant(String date, ZoneId zone) {
        return LocalDate.parse(date).atStartOfDay(zone).toInstant();
    }
}
