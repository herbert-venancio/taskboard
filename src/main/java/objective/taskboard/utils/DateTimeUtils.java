package objective.taskboard.utils;


import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class DateTimeUtils {

    public static ZonedDateTime parseDate(String yyyymmdd) {
        return parseDate(yyyymmdd, ZoneId.systemDefault());
    }

    public static ZonedDateTime parseDate(String yyyymmdd, ZoneId timezone) {
        return LocalDate.parse(yyyymmdd, DateTimeFormatter.ISO_LOCAL_DATE).atTime(0, 0, 0).atZone(timezone);
    }

    public static ZonedDateTime parseDateTime(String yyyymmdd, String hhmmss) {
        return parseDateTime(yyyymmdd, hhmmss, ZoneId.systemDefault());
    }

    public static ZonedDateTime parseDateTime(String yyyymmdd, String hhmmss, ZoneId timezone) {
        return LocalDateTime.parse(yyyymmdd + "T" + hhmmss, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(timezone);
    }

    public static ZonedDateTime get(org.joda.time.DateTime dt) {
        if (dt == null)
            return null;
        return ZonedDateTime.of(
                dt.getYear(),
                dt.getMonthOfYear(),
                dt.getDayOfMonth(),
                dt.getHourOfDay(),
                dt.getMinuteOfHour(),
                dt.getSecondOfMinute(),
                dt.getMillisOfSecond() * 1_000_000,
                ZoneId.of(dt.getZone().getID(), ZoneId.SHORT_IDS));
    }

    public static ZonedDateTime get(org.joda.time.DateTime dateTime, ZoneId zone) {
        return get(dateTime.getMillis(), zone);
    }
    
    public static ZonedDateTime get(long milliseconds, ZoneId zone) {
        return Instant.ofEpochMilli(milliseconds).atZone(zone);
    }

    public static ZonedDateTime get(ZonedDateTime dateTime, ZoneId zone) {
        return dateTime.withZoneSameInstant(zone);
    }

    public static ZonedDateTime roundDown(ZonedDateTime date) {
        ChronoField field = ChronoField.NANO_OF_DAY;
        return date.with(field, field.range().getMinimum());
    }

    public static ZonedDateTime roundUp(ZonedDateTime date) {
        ChronoField field = ChronoField.NANO_OF_DAY;
        return date.with(field, field.range().getMaximum());
    }

    public static ZoneId determineTimeZoneId(String zoneId) {
        try {
            return ZoneId.of(zoneId);
        } catch (Exception e) {
            return ZoneId.systemDefault();
        }
    }

    public static String toStringExcelFormat(ZonedDateTime date) {
        if(date == null)
            return "";

        return DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss").format(date);
    }

    public static String toDoubleExcelFormat(ZonedDateTime date, boolean date1904) {
        if(date == null)
            return "";

        LocalDateTime from;
        LocalDateTime to = date.toLocalDateTime();

        // Excel has 2 date systems:
        // - https://support.microsoft.com/pt-br/help/214330/differences-between-the-1900-and-the-1904-date-system-in-excel
        if(date1904) {
            from = LocalDateTime.of(1904, 1, 1, 0, 0, 0);
        } else {
            from = LocalDateTime.of(1900, 1, 1, 0, 0, 0);
            // http://polymathprogrammer.com/2009/10/26/the-leap-year-1900-bug-in-excel/
            if(to.isBefore(LocalDateTime.of(1900, 3, 1, 0, 0, 0))) {
                from = from.minusDays(1);
            } else {
                from = from.minusDays(2);
            }
        }

        long millis = from.until(to, ChronoUnit.MILLIS);

        // excel hours are represented as a fraction of a day
        double dayMillis = 24.0 * 60.0 * 60.0 * 1000.0;

        return Double.toString(millis / dayMillis);
    }

    public static class ZonedDateTimeAdapter implements JsonSerializer<ZonedDateTime>, JsonDeserializer<ZonedDateTime> {
        @Override
        public JsonElement serialize(ZonedDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString());
        }

        @Override
        public ZonedDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return ZonedDateTime.parse(json.getAsString());
        }
    }
}
