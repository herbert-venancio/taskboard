package objective.taskboard.followup.kpi;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import org.apache.commons.lang3.Range;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.utils.RangeUtils;

public class ProjectRangeByConfiguration implements ProjectTimelineRange{
    
    private Optional<LocalDate> startDate;
    private Optional<LocalDate> endDate;
    
    public ProjectRangeByConfiguration(ProjectFilterConfiguration project) {
        this.startDate = project.getStartDate();
        this.endDate = project.getDeliveryDate();
    }

    @Override
    public boolean isWithinRange(Range<ZonedDateTime> otherRange) {
        Range<LocalDate> transformed = RangeUtils.between(otherRange.getMinimum().toLocalDate(), otherRange.getMaximum().toLocalDate());
        return isTheBeginingOnRange(transformed) && isTheEndOnRange(transformed);
    }
       
    private boolean isTheBeginingOnRange(Range<LocalDate> range) {
        return startDate.map(date ->  !range.getMinimum().isBefore(date) ||  !range.getMaximum().isBefore(date)).orElse(true);
    }
    
    private boolean isTheEndOnRange(Range<LocalDate> range) {
        return endDate.map(date ->  !range.getMinimum().isAfter(date) || !range.getMaximum().isAfter(date)).orElse(true);
    }

}
