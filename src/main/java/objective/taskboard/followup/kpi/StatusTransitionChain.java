package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.Optional;

public interface StatusTransitionChain {

    Optional<StatusTransitionChain> givenDate(ZonedDateTime date);

    boolean isWithinDate(ZonedDateTime date);

    boolean isStatus(String status);

    StatusTransitionChain find(String status);
    
    Optional<ZonedDateTime> getDate();
    
}
