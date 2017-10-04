package objective.taskboard.utils;


import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

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

    public static ZonedDateTime get(org.joda.time.DateTime dateTime) {
        return get(dateTime, ZoneId.systemDefault());
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
