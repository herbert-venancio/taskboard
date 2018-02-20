package objective.taskboard.utils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
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
        if(yyyymmdd == null)
            return null;
        return LocalDate.parse(yyyymmdd, DateTimeFormatter.ISO_LOCAL_DATE).atTime(0, 0, 0).atZone(timezone);
    }

    public static List<ZonedDateTime> parseDateList(String... yyyymmdd) {
        return Arrays.stream(yyyymmdd)
                .map(DateTimeUtils::parseDate)
                .collect(Collectors.toList());
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

    public static ZonedDateTime get(Date date, ZoneId zone) {
        return date == null ? null : get(date.getTime(), zone);
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

    public static String toDoubleExcelFormat(LocalDateTime date, boolean date1904) {
        if(date == null)
            return "";

        LocalDateTime from;

        // Excel has 2 date systems:
        // - https://support.microsoft.com/pt-br/help/214330/differences-between-the-1900-and-the-1904-date-system-in-excel
        if(date1904) {
            from = LocalDateTime.of(1904, 1, 1, 0, 0, 0);
        } else {
            from = LocalDateTime.of(1900, 1, 1, 0, 0, 0);
            // http://polymathprogrammer.com/2009/10/26/the-leap-year-1900-bug-in-excel/
            if(date.isBefore(LocalDateTime.of(1900, 3, 1, 0, 0, 0))) {
                from = from.minusDays(1);
            } else {
                from = from.minusDays(2);
            }
        }

        long millis = from.until(date, ChronoUnit.MILLIS);

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

    public static class LocalDateTimeStampSerializer extends com.fasterxml.jackson.databind.JsonSerializer<LocalDate> {

        public static final LocalDateTimeStampSerializer INSTANCE = new LocalDateTimeStampSerializer();

        protected LocalDateTimeStampSerializer(){}

        private static ZoneId getTimezone() {
            RequestAttributes ra = RequestContextHolder.getRequestAttributes();
            if (ra instanceof ServletRequestAttributes) {
                HttpServletRequest request = ((ServletRequestAttributes)ra).getRequest();
                String zoneId = request.getParameter("timezone");
                if(zoneId != null)
                    return determineTimeZoneId(zoneId);
            }
            return ZoneId.systemDefault();
        }

        @Override
        public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException {
            gen.writeNumber(value.atStartOfDay().atZone(getTimezone()).toInstant().toEpochMilli());
        }
    }

    public static Date parseStringToDate(String yyyymmdd) {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return (Date)formatter.parse(yyyymmdd);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date toDate(LocalDate finalProjectDate, ZoneId zone) {
        return Date.from(finalProjectDate.atStartOfDay().atZone(zone).toInstant());
    }
    
    public static LocalDate toLocalDate(Date date, ZoneId zone) {
        return date.toInstant().atZone(zone).toLocalDate();
    }

    public static boolean isValidDate(String yyyymmdd) {
        try {
            parseDate(yyyymmdd);
        } catch (DateTimeParseException e) {
            return false;
        }
        return true;
    }
}
