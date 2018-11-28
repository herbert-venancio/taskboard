package objective.taskboard.followup.kpi;

import java.time.LocalDate;

import org.apache.commons.lang3.Range;

public interface ProjectTimelineRange {
    
    public boolean isWithinRange(Range<LocalDate> otherRange);

}
