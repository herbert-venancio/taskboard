package objective.taskboard.followup;

import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
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
import objective.taskboard.utils.RangeUtils;

@RunWith(MockitoJUnitRunner.class)
public class WeekRangeNormalizerTest {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private FollowUpTimeline timeline;

    @Test
    public void normalizeWeekRange_whenTimelineStartsOnSundayAndEndsOnSaturday_thenNoRoundingNeeded() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-04", "2018-09-21");
        Range<ZonedDateTime> timelineDateRange = createDateRange("2018-09-09", "2018-09-15");
    
        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY);
        assertDayOfWeekBoundaries(timelineDateRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
    
        mockTimeline(timelineDateRange);
    
        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);
    
        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-09", "2018-09-15");
    
        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
    }

    @Test
    public void normalizeWeekRange_whenTimelineStartsOnTuesdayAndEndsOnFriday_thenRoundingDownStartDateToSundayAndRoundingUpEndDateToSaturday() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-04", "2018-09-21");
        Range<ZonedDateTime> timelineDateRange = createDateRange("2018-09-04", "2018-09-21");

        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY);
        assertDayOfWeekBoundaries(timelineDateRange, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY);

        mockTimeline(timelineDateRange);

        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);

        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-02", "2018-09-22");

        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
    }

    @Test
    public void normalizeWeekRange_whenEmptyTimeline_thenRoundingDownDataSetStartDateToSundayAndRoundingUpDataSetEndDateToSaturday() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-04", "2018-09-21");
        
        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY);

        final Optional<LocalDate> timelineStartDate = Optional.empty();
        final Optional<LocalDate> timelineEndDate = Optional.empty();

        mockTimeline(timelineStartDate, timelineEndDate);

        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);

        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-02", "2018-09-22");

        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);        
    }

    @Test
    public void normalizeWeekRange_whenTimelineEmptyStartDateAndEndsOnSaturday_thenRoundingDownDataSetStartDateToSunday() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-04", "2018-09-21");
        
        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY);

        final Optional<LocalDate> timelineStartDate = Optional.empty();
        final Optional<LocalDate> timelineEndDate = Optional.of(LocalDate.parse("2018-09-15"));
        assertThat(timelineEndDate.get().getDayOfWeek(), is(DayOfWeek.SATURDAY));
        
        mockTimeline(timelineStartDate, timelineEndDate);

        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);

        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-02", "2018-09-15");

        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
    }

    @Test
    public void normalizeWeekRange_whenTimelineStartsOnSundayAndEmptyEndDate_thenRoundingUpDataSetEndDateToSaturday() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-04", "2018-09-21");
        
        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.TUESDAY, DayOfWeek.FRIDAY);

        final Optional<LocalDate> timelineStartDate = Optional.of(LocalDate.parse("2018-09-09"));
        final Optional<LocalDate> timelineEndDate = Optional.empty();
        assertThat(timelineStartDate.get().getDayOfWeek(), is(DayOfWeek.SUNDAY));
        
        mockTimeline(timelineStartDate, timelineEndDate);

        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);

        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-09", "2018-09-22");

        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
    }

    @Test
    public void normalizeWeekRange_whenEmptyTimelineAndDataSetStartsOnSundayAndEndsOnSaturday_thenNoRoundingNeeded() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-09", "2018-09-22");
        
        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);

        final Optional<LocalDate> timelineStartDate = Optional.empty();
        final Optional<LocalDate> timelineEndDate = Optional.empty();
        
        mockTimeline(timelineStartDate, timelineEndDate);

        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);

        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-09", "2018-09-22");

        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
    }

    @Test
    public void normalizeWeekRange_whenTimelineAndDataSetRangesFromSundayToSaturday_thenNoRoundingNeededAndRespectsTimelineRange() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-09", "2018-09-22");
        Range<ZonedDateTime> timelineDateRange = createDateRange("2018-09-02", "2018-09-29");

        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
        assertDayOfWeekBoundaries(timelineDateRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);

        mockTimeline(timelineDateRange);

        Range<ZonedDateTime> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);

        Range<ZonedDateTime> expectedWeekRange = createDateRange("2018-09-02", "2018-09-29");

        assertTrue(actualWeekRange.equals(expectedWeekRange));
        assertDayOfWeekBoundaries(actualWeekRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
        assertThat(actualWeekRange.getMinimum().getDayOfMonth(), is(2));
        assertThat(actualWeekRange.getMaximum().getDayOfMonth(), is(29));
    }

    @Test
    public void normalizeWeekRange_whenTimelineStartDateAfterTimelineEndDate_thenError() {
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-09", "2018-09-22");
        
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
        Range<ZonedDateTime> dataSetDateRange = createDateRange("2018-09-09", "2018-09-22");
        
        assertDayOfWeekBoundaries(dataSetDateRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);

        Optional<LocalDate> timelineStartDate = Optional.of(LocalDate.parse("2018-09-24"));
        assertThat(timelineStartDate.get().getDayOfWeek(), is(DayOfWeek.MONDAY));
        Optional<LocalDate> timelineEndDate = Optional.empty();

        mockTimeline(timelineStartDate, timelineEndDate);

        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Start date is after end date!");

        WeekRangeNormalizer.normalizeWeekRange(timeline, dataSetDateRange, ZONE_ID);
    }
    
    @Test
    public void normalizeWeekRange_simpleRange() {
        Range<LocalDate> dataSetDateRange = createLocalDateRange("2018-09-04", "2018-09-21");
    
        assertLocalDateRange(dataSetDateRange)
            .startsWith(TUESDAY).endsWith(FRIDAY);
   
        Range<LocalDate> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(dataSetDateRange, DayOfWeek.SUNDAY,DayOfWeek.SATURDAY);

        assertLocalDateRange(actualWeekRange)
            .startsAt("2018-09-02").endsAt("2018-09-22")
            .startsWith(SUNDAY).endsWith(SATURDAY);
        
    }
    
    @Test
    public void normalizeWeekRange_simpleRange_alreadyOnBoundaries() {
        Range<LocalDate> dataSetDateRange = createLocalDateRange("2018-09-02", "2018-09-22");
        assertLocalDateRange(dataSetDateRange)
            .startsWith(SUNDAY).endsWith(SATURDAY);

        Range<LocalDate> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(dataSetDateRange, DayOfWeek.SUNDAY,DayOfWeek.SATURDAY);
    
        assertLocalDateRange(actualWeekRange)
            .startsAt("2018-09-02").endsAt("2018-09-22")
            .startsWith(SUNDAY).endsWith(SATURDAY);
    }
    
    @Test
    public void normalizeWeekRange_simpleRange_shorterWeek() {
        Range<LocalDate> dataSetDateRange = createLocalDateRange("2018-09-04", "2018-09-20");
        assertLocalDateRange(dataSetDateRange)
            .startsWith(TUESDAY).endsWith(THURSDAY);
        
        Range<LocalDate> actualWeekRange = WeekRangeNormalizer.normalizeWeekRange(dataSetDateRange, DayOfWeek.MONDAY,DayOfWeek.FRIDAY);
    
        assertLocalDateRange(actualWeekRange)
            .startsAt("2018-09-03").endsAt("2018-09-21")
            .startsWith(MONDAY).endsWith(FRIDAY);
    }
    
    @Test
    public void splitByNormalizedWeek_happyDay(){
        Range<LocalDate> dateRange = createLocalDateRange("2018-09-23","2018-09-29");
        assertLocalDateRange(dateRange).startsWith(SUNDAY).endsWith(SATURDAY);
        
        List<Range<LocalDate>> ranges = WeekRangeNormalizer.splitByWeek(dateRange, SUNDAY, SATURDAY);
        assertThat(ranges.size(),is(1));
        assertLocalDateRange(ranges.get(0))
            .startsAt("2018-09-23").endsAt("2018-09-29")
            .startsWith(SUNDAY).endsWith(SATURDAY);
    }
    
    @Test
    public void splitByNormalizedWeek_twoWeeks(){
        Range<LocalDate> dateRange = createLocalDateRange("2018-09-23","2018-10-06");
        assertLocalDateRange(dateRange).startsWith(SUNDAY).endsWith(SATURDAY);
        
        List<Range<LocalDate>> ranges = WeekRangeNormalizer.splitByWeek(dateRange, SUNDAY, SATURDAY);
        assertThat(ranges.size(),is(2));
        assertLocalDateRange(ranges.get(0))
            .startsAt("2018-09-23").endsAt("2018-09-29")
            .startsWith(SUNDAY).endsWith(SATURDAY);
        assertLocalDateRange(ranges.get(1))
            .startsAt("2018-09-30").endsAt("2018-10-06")
            .startsWith(SUNDAY).endsWith(SATURDAY);
    }
    
    @Test
    public void splitByNormalizedWeek_brokenWeek(){
        Range<LocalDate> dateRange = createLocalDateRange("2018-09-20","2018-09-26");
        assertLocalDateRange(dateRange).startsWith(THURSDAY).endsWith(WEDNESDAY);
        
        List<Range<LocalDate>> ranges = WeekRangeNormalizer.splitByWeek(dateRange, SUNDAY, SATURDAY);
        assertThat(ranges.size(),is(2));
        assertLocalDateRange(ranges.get(0))
            .startsAt("2018-09-20").endsAt("2018-09-22")
            .startsWith(THURSDAY).endsWith(SATURDAY);
        assertLocalDateRange(ranges.get(1))
            .startsAt("2018-09-23").endsAt("2018-09-26")
            .startsWith(SUNDAY).endsWith(WEDNESDAY);
    }
    
    @Test
    public void splitByNormalizedWeek_brokenWeek_withMoreWeeks(){
        Range<LocalDate> dateRange = createLocalDateRange("2018-09-20","2018-10-02");
        assertLocalDateRange(dateRange).startsWith(THURSDAY).endsWith(TUESDAY);
        
        List<Range<LocalDate>> ranges = WeekRangeNormalizer.splitByWeek(dateRange, SUNDAY, SATURDAY);
        assertThat(ranges.size(),is(3));
        assertLocalDateRange(ranges.get(0))
            .startsAt("2018-09-20").endsAt("2018-09-22")
            .startsWith(THURSDAY).endsWith(SATURDAY);
        assertLocalDateRange(ranges.get(1))
            .startsAt("2018-09-23").endsAt("2018-09-29")
            .startsWith(SUNDAY).endsWith(SATURDAY);
        assertLocalDateRange(ranges.get(2))
            .startsAt("2018-09-30").endsAt("2018-10-02")
            .startsWith(SUNDAY).endsWith(TUESDAY);
    }
    
    private LocalDateRangeAsserter assertLocalDateRange(Range<LocalDate> range) {
        return new LocalDateRangeAsserter(range);
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

    private Range<ZonedDateTime> createDateRange(String startDate, String endDate) {
        ZonedDateTime dataSetStartDate = DateTimeUtils.parseDateTime(startDate);
        ZonedDateTime dataSetEndDate = DateTimeUtils.parseDateTime(endDate);
        return RangeUtils.between(dataSetStartDate, dataSetEndDate);
    }
    
    private Range<LocalDate> createLocalDateRange(String startDate, String endDate){
        LocalDate dataSetStartDate = LocalDate.parse(startDate);
        LocalDate dataSetEndDate = LocalDate.parse(endDate);
        return RangeUtils.between(dataSetStartDate, dataSetEndDate);
    }

    private void assertDayOfWeekBoundaries(Range<ZonedDateTime> dateRange, DayOfWeek startDayOfWeek, DayOfWeek endDayOfWeek) {
        assertThat(dateRange.getMinimum().getDayOfWeek(), is(startDayOfWeek));
        assertThat(dateRange.getMaximum().getDayOfWeek(), is(endDayOfWeek));
    }
    
    private class LocalDateRangeAsserter {
         private Range<LocalDate> range;
         
         private LocalDateRangeAsserter(Range<LocalDate> range) {
             this.range = range;
         }
         
         private LocalDateRangeAsserter startsAt(String date) {
             assertThat(range.getMinimum(),is(LocalDate.parse(date)));
             return this;
         }
         
         private LocalDateRangeAsserter endsAt(String date) {
             assertThat(range.getMaximum(),is(LocalDate.parse(date)));
             return this;
         }
         
         private LocalDateRangeAsserter startsWith(DayOfWeek startOfWeek) {
             assertThat(range.getMinimum().getDayOfWeek(), is(startOfWeek));
             return this;
         }
         
         private LocalDateRangeAsserter endsWith(DayOfWeek endOfWeek) {
             assertThat(range.getMaximum().getDayOfWeek(), is(endOfWeek));
             return this;
         }
    }

}
