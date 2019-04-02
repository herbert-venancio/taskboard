package objective.taskboard.followup.kpi.filters;

import java.time.LocalDate;

import org.apache.commons.lang3.Range;

public interface ProjectTimelineRange {
    
    public boolean isWithinRange(Range<LocalDate> otherRange);

}
