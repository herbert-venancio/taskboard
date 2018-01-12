package objective.taskboard.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import objective.taskboard.testUtils.DateTimeUtilSupport;
import org.junit.Test;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.TimeZone;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class DateTimeUtilsTest {

    @Test
    public void parseDate() {
        assertThat(DateTimeUtils.parseDate("2020-01-01"), equalTo(ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault())));
    }

    @Test
    public void parseDateTime() {
        assertThat(DateTimeUtils.parseDateTime("2020-01-01", "12:34:56"), equalTo(ZonedDateTime.of(2020, 1, 1, 12, 34, 56, 0, ZoneId.systemDefault())));
    }

    @Test
    public void roundDown() {
        // given
        ZonedDateTime date = ZonedDateTime.of(2020, 1, 1, 12, 34, 56, 789, ZoneId.systemDefault());
        ZonedDateTime expected = ZonedDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneId.systemDefault());

        // when
        date = DateTimeUtils.roundDown(date);

        // then
        assertThat(date, equalTo(expected));
    }

    @Test
    public void roundUp() {
        // given
        ZonedDateTime date = ZonedDateTime.of(2020, 1, 1, 12, 34, 56, 789, ZoneId.systemDefault());
        ZonedDateTime expected = ZonedDateTime.of(2020, 1, 1, 23, 59, 59, 999999999, ZoneId.systemDefault());

        // when
        date = DateTimeUtils.roundUp(date);

        // then
        assertThat(date, equalTo(expected));
    }

    @Test
    public void determineTimeZoneId() {
        // given
        String validZoneId1 = "Asia/Riyadh";
        String validZoneId2 = "Atlantic/Reykjavik";
        String invalidZoneId = "INVALID_ZONE_ID";
        String nullZoneId = null;

        // when
        ZoneId returnOfValidTimeZoneId1 = DateTimeUtils.determineTimeZoneId(validZoneId1);
        ZoneId returnOfValidTimeZoneId2 = DateTimeUtils.determineTimeZoneId(validZoneId2);
        ZoneId returnOfInvalidTimeZoneId = DateTimeUtils.determineTimeZoneId(invalidZoneId);
        ZoneId returnOfNullTimeZoneId = DateTimeUtils.determineTimeZoneId(nullZoneId);
        ZoneId systemDefault = TimeZone.getDefault().toZoneId();

        //then
        assertThat(returnOfValidTimeZoneId1.getId(), equalTo(validZoneId1));
        assertThat(returnOfValidTimeZoneId2.getId(), equalTo(validZoneId2));
        assertThat(returnOfInvalidTimeZoneId, equalTo(systemDefault));
        assertThat(returnOfNullTimeZoneId, equalTo(systemDefault));
    }

    @Test
    public void toStringExcelFormat() {
        // given
        ZonedDateTime date1 = ZonedDateTime.of(2020, 12, 31, 0, 0, 0, 0, ZoneId.systemDefault());
        ZonedDateTime date2 = ZonedDateTime.of(2020, 12, 31, 12, 34, 56, 789, ZoneId.systemDefault());
        ZonedDateTime date3 = ZonedDateTime.of(2020, 12, 31, 23, 59, 59, 999999999, ZoneId.systemDefault());

        // when
        String subject1 = DateTimeUtils.toStringExcelFormat(date1);
        String subject2 = DateTimeUtils.toStringExcelFormat(date2);
        String subject3 = DateTimeUtils.toStringExcelFormat(date3);

        // then
        assertThat(subject1, equalTo("2020-12-31 00:00:00"));
        assertThat(subject2, equalTo("2020-12-31 12:34:56"));
        assertThat(subject3, equalTo("2020-12-31 23:59:59"));
    }

    @Test
    public void toDoubleExcelFormat() {
        boolean EXCEL_1900_DATE_SYSTEM = false;
        boolean EXCEL_1904_DATE_SYSTEM = true;

        // given
        ZonedDateTime date1 = ZonedDateTime.of(2017, 9, 20, 10, 05, 48, 0, ZoneOffset.UTC);
        ZonedDateTime date2 = ZonedDateTime.of(2001, 1, 1, 1, 1, 1, 0, ZoneOffset.UTC);
        ZonedDateTime date3 = ZonedDateTime.of(1900, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime date4 = ZonedDateTime.of(2021, 9, 21, 10, 5, 48, 0, ZoneOffset.UTC);
        ZonedDateTime date5 = ZonedDateTime.of(2005, 1, 2, 1, 1, 1, 0, ZoneOffset.UTC);
        ZonedDateTime date6 = ZonedDateTime.of(1904, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC);

        // when
        String actual1 = DateTimeUtils.toDoubleExcelFormat(date1, EXCEL_1900_DATE_SYSTEM);
        String actual2 = DateTimeUtils.toDoubleExcelFormat(date2, EXCEL_1900_DATE_SYSTEM);
        String actual3 = DateTimeUtils.toDoubleExcelFormat(date3, EXCEL_1900_DATE_SYSTEM);
        String actual4 = DateTimeUtils.toDoubleExcelFormat(date4, EXCEL_1904_DATE_SYSTEM);
        String actual5 = DateTimeUtils.toDoubleExcelFormat(date5, EXCEL_1904_DATE_SYSTEM);
        String actual6 = DateTimeUtils.toDoubleExcelFormat(date6, EXCEL_1904_DATE_SYSTEM);

        // then
        assertThat(Double.valueOf(actual1), closeTo(Double.parseDouble("42998.420694444445"), 0.000001));
        assertThat(Double.valueOf(actual2), closeTo(Double.parseDouble("36892.042372685188"), 0.000001));
        assertThat(Double.valueOf(actual3), closeTo(Double.parseDouble("1"), 0.000001));
        assertThat(Double.valueOf(actual4), closeTo(Double.parseDouble("42998.420694444445"), 0.000001));
        assertThat(Double.valueOf(actual5), closeTo(Double.parseDouble("36892.042372685188"), 0.000001));
        assertThat(Double.valueOf(actual6), closeTo(Double.parseDouble("1"), 0.000001));
    }

    @Test
    public void toDoubleExcelFormat1900() {
        try (Scanner dates = new Scanner(DateTimeUtilsTest.class.getResourceAsStream("excel-input-dates-1900.csv"))) {
            try (Scanner expecteds = new Scanner(DateTimeUtilsTest.class.getResourceAsStream("excel-dates-expected.csv"))) {
                List<String> failedConversions = new ArrayList<>();
                while (dates.hasNext()) {
                    ZonedDateTime date = DateTimeUtils.parseDate(dates.next(), ZoneOffset.ofHours(0));
                    String actual = DateTimeUtils.toDoubleExcelFormat(date, false);
                    double expected = expecteds.nextDouble();
                    try {
                        assertThat(Double.valueOf(actual), closeTo(expected, 0.000001));
                    } catch (AssertionError e) {
                        failedConversions.add(date.toLocalDateTime().toString() + " -> expected " + expected + " actual " + actual);
                    }
                }
                assertThat(String.join("\n", failedConversions), failedConversions, is(emptyList()));
            }
        }
    }

    @Test
    public void toDoubleExcelFormat1904() {
        try (Scanner dates = new Scanner(DateTimeUtilsTest.class.getResourceAsStream("excel-input-dates-1904.csv"))) {
            try (Scanner expecteds = new Scanner(DateTimeUtilsTest.class.getResourceAsStream("excel-dates-expected.csv"))) {
                List<String> failedConversions = new ArrayList<>();
                while (dates.hasNext()) {
                    ZonedDateTime date = DateTimeUtils.parseDate(dates.next(), ZoneOffset.ofHours(0));
                    String actual = DateTimeUtils.toDoubleExcelFormat(date, true);
                    double expected = expecteds.nextDouble();
                    try {
                        assertThat(Double.valueOf(actual), closeTo(expected, 0.000001));
                    } catch (AssertionError e) {
                        failedConversions.add(date.toLocalDateTime().toString() + " -> expected " + expected + " actual " + actual);
                    }
                }
                assertThat(String.join("\n", failedConversions), failedConversions, is(emptyList()));
            }
        }
    }

    @Test
    public void whenUsingLocalDateSerializer_resultShouldBeEqualToDateSerializer() throws ParseException, JsonProcessingException {
        // given
        ObjectMapper defaultMapper = new ObjectMapper();

        ObjectMapper customMapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(LocalDate.class, new DateTimeUtils.LocalDateTimeStampSerializer());
        customMapper.registerModule(module);

        Date date = DateTimeUtilSupport.date(2000, 1, 1);
        LocalDate localDate = LocalDate.of(2000, 1, 1);

        // when
        long expected = Long.parseLong(defaultMapper.writeValueAsString(date));
        long actual = Long.parseLong(customMapper.writeValueAsString(localDate));

        // then
        assertEquals(expected, actual);
    }
}
