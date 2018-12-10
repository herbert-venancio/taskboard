package objective.taskboard.followup;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Iterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.Range;

import objective.taskboard.followup.FollowUpTimeline;
import objective.taskboard.utils.RangeUtils;

public class WeekRangeNormalizer {

    public static Range<ZonedDateTime> normalizeWeekRange(FollowUpTimeline timeline, Range<ZonedDateTime> dataSetDateRange, ZoneId zone) {
        ZonedDateTime baseStartDate = timeline.getStart().map(d -> d.atStartOfDay(zone)).orElse(dataSetDateRange.getMinimum());
        ZonedDateTime baseEndDate = timeline.getEnd().map(d -> d.atStartOfDay(zone)).orElse(dataSetDateRange.getMaximum());
        
        if (baseStartDate.isAfter(baseEndDate))
            throw new IllegalArgumentException("Start date is after end date!");

        ZonedDateTime normalizedStartDate = findPrevious(baseStartDate,DayOfWeek.SUNDAY);
        ZonedDateTime normalizedEndDate = findNext(baseEndDate,DayOfWeek.SATURDAY);
        
        return RangeUtils.between(normalizedStartDate, normalizedEndDate);
    }
    
    public static Stream<Range<LocalDate>> splitByWeek(Range<LocalDate> range, DayOfWeek startOfWeek, DayOfWeek endOfWeek){
    	Iterable<Range<LocalDate>> it = () -> new Iterator<Range<LocalDate>>() {
            LocalDate rangeIndex = range.getMinimum();

            @Override
            public boolean hasNext() {
                return rangeIndex.isBefore(range.getMaximum());
            }

            @Override
            public Range<LocalDate> next() {
                LocalDate startOfRange = rangeIndex;
                LocalDate endOfRange = rangeIndex.with(TemporalAdjusters.next(endOfWeek));

                if(!endOfRange.isBefore(range.getMaximum())) {
                    endOfRange = range.getMaximum();
                }
                rangeIndex = rangeIndex.with(TemporalAdjusters.next(startOfWeek));
                return RangeUtils.between(startOfRange, endOfRange);
            }
        };

        return StreamSupport.stream(it.spliterator(), false);
    }

    private static ZonedDateTime findNext(ZonedDateTime baseEndDate, DayOfWeek endOfWeek) {
        return baseEndDate.with(TemporalAdjusters.nextOrSame(endOfWeek));
    }
    
    private static ZonedDateTime findPrevious(ZonedDateTime baseStartDate, DayOfWeek startOfWeek) {
        return baseStartDate.with(TemporalAdjusters.previousOrSame(startOfWeek));
    }

}
