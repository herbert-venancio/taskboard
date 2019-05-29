package objective.taskboard.timeline;

import java.time.LocalDate;
import java.util.Optional;

public class DashboardTimeline {

    public final Optional<LocalDate> startDate;
    public final Optional<LocalDate> endDate;

    public DashboardTimeline(Optional<LocalDate> startDate, Optional<LocalDate> endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public boolean hasBothDates() {
        return startDate.isPresent() && endDate.isPresent();
    }

}
