package objective.taskboard.followup.kpi.touchtime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;

import org.apache.commons.lang3.Range;

import objective.taskboard.followup.kpi.IssueKpi;

public class TouchTimeKpiWeekRange implements Comparable<TouchTimeKpiWeekRange> {
    private Range<LocalDate> range;
    private ZoneId timezone;

    public TouchTimeKpiWeekRange(Range<LocalDate> weekRange, ZoneId timezone) {
        this.range = weekRange;
        this.timezone = timezone;
    }

    public boolean overlaps(Range<LocalDate> otherRange) {
        return this.range.isOverlappedBy(otherRange);
    }

    public Range<LocalDate> getRange() {
        return range;
    }

    public ZonedDateTime getFirstDay() {
        return range.getMinimum().atStartOfDay(timezone).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    }

    public ZonedDateTime getLastDay() {
        return range.getMaximum().atStartOfDay(timezone);
    }

    public ZoneId getTimezone() {
        return timezone;
    }

    public boolean progressOverlaps(IssueKpi issueKpi) {
        return issueKpi.getDateRangeBasedOnProgressingStatuses(this.getTimezone())
                .map(this::overlaps).orElse(false);
    }

    @Override
    public String toString() {
        return "Week" + range;
    }

    @Override
    public int compareTo(TouchTimeKpiWeekRange other) {
        return this.getFirstDay().compareTo(other.getFirstDay());
    }
}
