package objective.taskboard.utils;

import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class DateTimeUtilsTest {

    @Test
    public void parseDate() {
        assertThat(DateTimeUtils.parseDate("2020-01-01"), equalTo(ZonedDateTime.of(2020, 01, 01, 0, 0, 0, 0, ZoneId.systemDefault())));
    }

    @Test
    public void parseDateTime() {
        assertThat(DateTimeUtils.parseDateTime("2020-01-01", "12:34:56"), equalTo(ZonedDateTime.of(2020, 01, 01, 12, 34, 56, 0, ZoneId.systemDefault())));
    }

    @Test
    public void roundDown() {
        // given
        ZonedDateTime date = ZonedDateTime.of(2020, 01, 01, 12, 34, 56, 789, ZoneId.systemDefault());
        ZonedDateTime expected = ZonedDateTime.of(2020, 01, 01, 0, 0, 0, 0, ZoneId.systemDefault());

        // when
        date = DateTimeUtils.roundDown(date);

        // then
        assertThat(date, equalTo(expected));
    }

    @Test
    public void roundUp() {
        // given
        ZonedDateTime date = ZonedDateTime.of(2020, 01, 01, 12, 34, 56, 789, ZoneId.systemDefault());
        ZonedDateTime expected = ZonedDateTime.of(2020, 01, 01, 23, 59, 59, 999999999, ZoneId.systemDefault());

        // when
        date = DateTimeUtils.roundUp(date);

        // then
        assertThat(date, equalTo(expected));
    }
}
