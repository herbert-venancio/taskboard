package objective.taskboard.followup;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.utils.DateTimeUtils;

@RunWith(MockitoJUnitRunner.class)
public class WeekRangeNormalizerTest {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private FollowUpTimeline timeline;

    @Test
    public void normalizeWeekRange_whenTimelineStartsOnSundayAndEndsOnSaturday_thenNoRoundingNeeded() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-04", "2018-09-21", ZONE_ID);
        Range<ZonedDateTime> timelineDateRange = createDateRange("2018-09-09", "2018-09-15", ZONE_ID);
    
        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY);
        assertDayOfWeekBoundaries(timelineDateRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
    
        mockTimeline(timelineDateRange);
    
        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);
    
        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-09", "2018-09-15", ZONE_ID);
    
        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
    }

    @Test
    public void normalizeWeekRange_whenTimelineStartsOnTuesdayAndEndsOnFriday_thenRoundingDownStartDateToSundayAndRoundingUpEndDateToSaturday() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-04", "2018-09-21", ZONE_ID);
        Range<ZonedDateTime> timelineDateRange = createDateRange("2018-09-04", "2018-09-21", ZONE_ID);

        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY);
        assertDayOfWeekBoundaries(timelineDateRange, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY);

        mockTimeline(timelineDateRange);

        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);

        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-02", "2018-09-22", ZONE_ID);

        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
    }

    @Test
    public void normalizeWeekRange_whenEmptyTimeline_thenRoundingDownDataSetStartDateToSundayAndRoundingUpDataSetEndDateToSaturday() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-04", "2018-09-21", ZONE_ID);
        
        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY);

        final Optional<LocalDate> timelineStartDate = Optional.empty();
        final Optional<LocalDate> timelineEndDate = Optional.empty();

        mockTimeline(timelineStartDate, timelineEndDate);

        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);

        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-02", "2018-09-22", ZONE_ID);

        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);        
    }

    @Test
    public void normalizeWeekRange_whenTimelineEmptyStartDateAndEndsOnSaturday_thenRoundingDownDataSetStartDateToSunday() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-04", "2018-09-21", ZONE_ID);
        
        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY);

        final Optional<LocalDate> timelineStartDate = Optional.empty();
        final Optional<LocalDate> timelineEndDate = Optional.of(LocalDate.parse("2018-09-15"));
        assertThat(timelineEndDate.get().getDayOfWeek(), is(DayOfWeek.SATURDAY));
        
        mockTimeline(timelineStartDate, timelineEndDate);

        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);

        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-02", "2018-09-15", ZONE_ID);

        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
    }

    @Test
    public void normalizeWeekRange_whenTimelineStartsOnSundayAndEmptyEndDate_thenRoundingUpDataSetEndDateToSaturday() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-04", "2018-09-21", ZONE_ID);
        
        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY);

        final Optional<LocalDate> timelineStartDate = Optional.of(LocalDate.parse("2018-09-09"));
        final Optional<LocalDate> timelineEndDate = Optional.empty();
        assertThat(timelineStartDate.get().getDayOfWeek(), is(DayOfWeek.SUNDAY));
        
        mockTimeline(timelineStartDate, timelineEndDate);

        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);

        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-09", "2018-09-22", ZONE_ID);

        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
    }

    @Test
    public void normalizeWeekRange_whenEmptyTimelineAndDataSetStartsOnSundayAndEndsOnSaturday_thenNoRoundingNeeded() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-09", "2018-09-22", ZONE_ID);
        
        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);

        final Optional<LocalDate> timelineStartDate = Optional.empty();
        final Optional<LocalDate> timelineEndDate = Optional.empty();
        
        mockTimeline(timelineStartDate, timelineEndDate);

        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);

        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-09", "2018-09-22", ZONE_ID);

        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
    }

    @Test
    public void normalizeWeekRange_whenTimelineAndDataSetRangesFromSundayToSaturday_thenNoRoundingNeededAndRespectsTimelineRange() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-09", "2018-09-22", ZONE_ID);
        Range<ZonedDateTime> timelineDateRange = createDateRange("2018-09-02", "2018-09-29", ZONE_ID);

        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
        assertDayOfWeekBoundaries(timelineDateRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);

        mockTimeline(timelineDateRange);

        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);

        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-02", "2018-09-29", ZONE_ID);

        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
        assertThat(actualWeekRange.getMinimum().getDayOfMonth(), is(2));
        assertThat(actualWeekRange.getMaximum().getDayOfMonth(), is(29));
    }

    @Test
    public void normalizeWeekRange_whenTimelineStartDateAfterTimelineEndDate_thenError() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-09", "2018-09-22", ZONE_ID);
        
        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);

        Optional<LocalDate> timelineStartDate = Optional.of(LocalDate.parse("2018-09-16"));
        assertThat(timelineStartDate.get().getDayOfWeek(), is(DayOfWeek.SUNDAY));
        Optional<LocalDate> timelineEndDate = Optional.of(LocalDate.parse("2018-09-10"));
        assertThat(timelineEndDate.get().getDayOfWeek(), is(DayOfWeek.MONDAY));

        mockTimeline(timelineStartDate, timelineEndDate);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Start date is after end date!");

        WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);
    }

    @Test
    public void normalizeWeekRange_whenTimelineStartDateAfterDataSetEndDateAndTimelineEndDateEmpty__thenError() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-09", "2018-09-22", ZONE_ID);
        
        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);

        Optional<LocalDate> timelineStartDate = Optional.of(LocalDate.parse("2018-09-24"));
        assertThat(timelineStartDate.get().getDayOfWeek(), is(DayOfWeek.MONDAY));
        Optional<LocalDate> timelineEndDate = Optional.empty();

        mockTimeline(timelineStartDate, timelineEndDate);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Start date is after end date!");

        WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);
    }

    private void mockTimeline(Range<ZonedDateTime> timelineDateRange) {
        Optional<LocalDate> timelineStartDate = Optional.of(timelineDateRange.getMinimum().toLocalDate());
        Optional<LocalDate> timelineEndDate = Optional.of(timelineDateRange.getMaximum().toLocalDate());
    
        mockTimeline(timelineStartDate, timelineEndDate);
    }
    
    private void mockTimeline(Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        Mockito.when(timeline.getStart()).thenReturn(startDate);
        Mockito.when(timeline.getEnd()).thenReturn(endDate);
    }

    private Range<ZonedDateTime> createDateRange(String startDate, String endDate, ZoneId zone) {
        ZonedDateTime dataSetStartDate = LocalDate.parse(startDate).atStartOfDay(zone);
        ZonedDateTime dataSetEndDate = LocalDate.parse(endDate).atStartOfDay(zone);
        return DateTimeUtils.range(dataSetStartDate, dataSetEndDate);
    }

    private void assertDayOfWeekBoundaries(Range<ZonedDateTime> dateRange, DayOfWeek startDayOfWeek, DayOfWeek endDayOfWeek) {
        assertThat(dateRange.getMinimum().getDayOfWeek(), is(startDayOfWeek));
        assertThat(dateRange.getMaximum().getDayOfWeek(), is(endDayOfWeek));
    }

}
