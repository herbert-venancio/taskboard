package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;

import org.apache.commons.lang3.Range;

public interface ProjectTimelineRange {
    
    public boolean isWithinRange(Range<ZonedDateTime> otherRange);

}
