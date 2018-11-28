package objective.taskboard.followup;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedList;
import java.util.List;

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
     
    public static Range<LocalDate> normalizeWeekRange(Range<LocalDate> range, DayOfWeek startOfWeek, DayOfWeek endOfWeek){
        LocalDate normalizedStartDate = findPrevious(range.getMinimum(), startOfWeek);
        LocalDate normalizedEndDate = findNext(range.getMaximum(),endOfWeek);
        
        return RangeUtils.between(normalizedStartDate, normalizedEndDate);
    }
    public static List<Range<LocalDate>> splitByWeek(Range<LocalDate> range,DayOfWeek startOfWeek,DayOfWeek endOfWeek){
        List<Range<LocalDate>> weeks = new LinkedList<>();
        
        LocalDate startOfRange = range.getMinimum();
        LocalDate rangeIndex = startOfRange;
        while(rangeIndex.isBefore(range.getMaximum()) ) {
            
            if(rangeIndex.getDayOfWeek().equals(endOfWeek)) {
                weeks.add(RangeUtils.between(startOfRange, rangeIndex));
                rangeIndex = rangeIndex.with(TemporalAdjusters.nextOrSame(startOfWeek));
                startOfRange = rangeIndex;
                continue;
            }
            
            rangeIndex = rangeIndex.plusDays(1);
        }
        
        weeks.add(RangeUtils.between(startOfRange, rangeIndex));
        
        return weeks;
    }

    private static ZonedDateTime findNext(ZonedDateTime baseEndDate, DayOfWeek endOfWeek) {
        return baseEndDate.with(TemporalAdjusters.nextOrSame(endOfWeek));
    }
    
    private static LocalDate findNext(LocalDate baseEndDate, DayOfWeek startOfWeek) {
        return baseEndDate.with(TemporalAdjusters.nextOrSame(startOfWeek));
    }

    private static ZonedDateTime findPrevious(ZonedDateTime baseStartDate, DayOfWeek startOfWeek) {
        return baseStartDate.with(TemporalAdjusters.previousOrSame(startOfWeek));
    }
    
    private static LocalDate findPrevious(LocalDate baseStartDate, DayOfWeek startOfWeek) {
        return baseStartDate.with(TemporalAdjusters.previousOrSame(startOfWeek));
    }
}
