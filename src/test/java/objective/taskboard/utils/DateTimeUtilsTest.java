package objective.taskboard.utils;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.equalTo;
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
}
