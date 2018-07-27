package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.Optional;

public class StatusTransition implements StatusTransitionChain{
    
    public final String status;
    public final ZonedDateTime date;
    private StatusTransitionChain next;

    public StatusTransition(String status, ZonedDateTime date, StatusTransitionChain next) {
        this.status = status;
        this.date = date;
        this.next = next;
    }
    
    @Override
    public Optional<StatusTransitionChain> givenDate(ZonedDateTime date) {
        
        if(next.isWithinDate(date))
            return next.givenDate(date);
       
        if(this.isWithinDate(date))
            return Optional.of(this);
        
       return Optional.empty();
    }

    @Override
    public boolean isWithinDate(ZonedDateTime date) {
        return !this.date.toLocalDate().isAfter(date.toLocalDate());
    }

    @Override
    public boolean isStatus(String status) {
        return this.status.equals(status);
    }

    @Override
    public StatusTransitionChain find(String status) {
        return this.status.equals(status) ? this : next.find(status);
    }
    
    @Override
    public Optional<ZonedDateTime> getDate() {
        return Optional.of(date);
    }
  
}
