package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.Optional;

public class NoDateStatusTransition implements StatusTransitionChain {
    
    private String status;
    private StatusTransitionChain next;

    public NoDateStatusTransition(String status, StatusTransitionChain next) {
        this.status = status;
        this.next = next;
    }

    @Override
    public Optional<StatusTransitionChain> givenDate(ZonedDateTime date) {
        return next.givenDate(date);
    }

    @Override
    public boolean isWithinDate(ZonedDateTime date) {
        return next.isWithinDate(date);
    }

    @Override
    public boolean isStatus(String status) {
        return this.status.equals(status);
    }
    
 
}
