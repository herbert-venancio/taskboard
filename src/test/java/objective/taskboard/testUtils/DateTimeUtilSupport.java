package objective.taskboard.testUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class DateTimeUtilSupport {
    public static Date date(int year, int month, int date) {
        return new Date(ZonedDateTime.of(year, month, date, 0, 0, 0, 0, ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
    
    public static Date date(int year, int month, int date, ZoneId zone) {
        return new Date(ZonedDateTime.of(year, month, date, 0, 0, 0, 0, zone).toInstant().toEpochMilli());
    }
    
    public static Date date(int year, int month, int date, int hour, int min) {
        return new Date(ZonedDateTime.of(year, month, date, hour, min, 0, 0, ZoneId.systemDefault()).toInstant().toEpochMilli());
    }
    
    
    public static LocalDate localDate(int year, int month, int date) {
        return ZonedDateTime.of(year, month, date, 0, 0, 0, 0, ZoneId.systemDefault()).toLocalDate();
    }
    
    public static LocalDate localDate(int year, int month, int date, ZoneId zone) {
        return ZonedDateTime.of(year, month, date, 0, 0, 0, 0, zone).toLocalDate();
    }
    
    public static Date date(LocalDate d, ZoneId zone) {
        return Date.from(d.atStartOfDay().atZone(zone).toInstant());
    }
    
    public static LocalDate localDate(Date date, ZoneId zone) {
        return date.toInstant().atZone(zone).toLocalDate();
    }
}
