package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.Optional;

public class StatusTransition {
    
    protected final String status;
    protected final Optional<StatusTransition> next;

    public StatusTransition(String status, Optional<StatusTransition> next) {
        this.status = status;
        this.next = next;
    }

    public Optional<StatusTransition> givenDate(ZonedDateTime date) {
        return next.map(n-> n.givenDate(date)).orElse(Optional.empty());
    }

    public boolean isWithinDate(ZonedDateTime date) {
        return next.map(n -> n.isWithinDate(date)).orElse(false);
    }

    public boolean isStatus(String status) {
        return this.status.equals(status);
    }

    public Optional<DatedStatusTransition> findWithTransition(String status) {
        return next.map(n -> n.findWithTransition(status)).orElse(Optional.empty());
    }

}
