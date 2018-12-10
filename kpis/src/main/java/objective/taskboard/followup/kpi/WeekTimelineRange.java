package objective.taskboard.followup.kpi;

import java.time.LocalDate;

import org.apache.commons.lang3.Range;

public class WeekTimelineRange implements ProjectTimelineRange{
    
    private Range<LocalDate> weekRange;
    
    public WeekTimelineRange(Range<LocalDate> weekRange) {
        this.weekRange = weekRange;
    }

    @Override
    public boolean isWithinRange(Range<LocalDate> otherRange) {
        
        return otherRange.isOverlappedBy(weekRange);
    }

}
