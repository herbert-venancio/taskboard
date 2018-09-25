package objective.taskboard.followup;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

import org.apache.commons.lang3.Range;

import objective.taskboard.utils.DateTimeUtils;

public class WeekRangeNormalizer {

    public static Range<ZonedDateTime> normalizeWeekRange(FollowUpTimeline timeline, Range<ZonedDateTime> dataSetDateRange, ZoneId zone) {
        ZonedDateTime baseStartDate = timeline.getStart().map(d -> d.atStartOfDay(zone)).orElse(dataSetDateRange.getMinimum());
        ZonedDateTime baseEndDate = timeline.getEnd().map(d -> d.atStartOfDay(zone)).orElse(dataSetDateRange.getMaximum());
        
        if (baseStartDate.isAfter(baseEndDate))
            throw new IllegalArgumentException("Start date is after end date!");

        ZonedDateTime normalizedStartDate = findPreviousSunday(baseStartDate);
        ZonedDateTime normalizedEndDate = findNextSaturday(baseEndDate);
        
        return DateTimeUtils.range(normalizedStartDate, normalizedEndDate);
    }

    private static ZonedDateTime findNextSaturday(ZonedDateTime baseEndDate) {
        return baseEndDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
    }

    private static ZonedDateTime findPreviousSunday(ZonedDateTime baseStartDate) {
        return baseStartDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    }
}
