package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface StatusTransitionChain {

    Optional<StatusTransitionChain> givenDate(ZonedDateTime date);

    boolean isWithinDate(ZonedDateTime date);

    boolean isStatus(String status);

    StatusTransitionChain find(String status);
    
    Optional<ZonedDateTime> getDate();
    
    public static StatusTransitionChain create(String status, ZonedDateTime date, StatusTransitionChain next) {
        return (date == null) ? new NoDateStatusTransition(status,next) : new StatusTransition(status, date,next);
    }
    
}
