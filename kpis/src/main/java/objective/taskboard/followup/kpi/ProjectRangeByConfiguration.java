package objective.taskboard.followup.kpi;

import java.time.LocalDate;
import java.util.Optional;

import org.apache.commons.lang3.Range;

import objective.taskboard.domain.ProjectFilterConfiguration;

public class ProjectRangeByConfiguration implements ProjectTimelineRange{

    private Optional<LocalDate> startDate;
    private Optional<LocalDate> endDate;

    public ProjectRangeByConfiguration(ProjectFilterConfiguration project) {
        this.startDate = project.getStartDate();
        this.endDate = project.getDeliveryDate();
    }

    @Override
    public boolean isWithinRange(Range<LocalDate> otherRange) {
        return isTheBeginingOnRange(otherRange) && isTheEndOnRange(otherRange);
    }

    private boolean isTheBeginingOnRange(Range<LocalDate> range) {
        return startDate.map(date ->  !range.getMinimum().isBefore(date) ||  !range.getMaximum().isBefore(date)).orElse(true);
    }

    private boolean isTheEndOnRange(Range<LocalDate> range) {
        return endDate.map(date ->  !range.getMinimum().isAfter(date) || !range.getMaximum().isAfter(date)).orElse(true);
    }

    @Override
    public String toString() {
        return "ProjectRangeByConfiguration [startDate=" + startDate + ", endDate=" + endDate + "]";
    }

}
