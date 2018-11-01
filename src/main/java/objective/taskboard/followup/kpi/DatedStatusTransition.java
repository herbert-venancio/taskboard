package objective.taskboard.followup.kpi;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

import objective.taskboard.data.Worklog;
import objective.taskboard.utils.DateTimeUtils;

public class DatedStatusTransition extends StatusTransition {
    
    public final ZonedDateTime date;

    public DatedStatusTransition(String status, ZonedDateTime date, boolean isProgressingStatus,Optional<StatusTransition> next) {
        super(status,isProgressingStatus,next);
        this.date = date;
    }
    
    @Override
    public Optional<DatedStatusTransition> findWithTransition(String status) {
        return this.status.equalsIgnoreCase(status) ? Optional.of(this) : super.findWithTransition(status);
    }
    
    @Override
    public Optional<StatusTransition> givenDate(ZonedDateTime date) {
        
        if(next.isPresent() && next.get().isWithinDate(date))
            return next.get().givenDate(date);
       
        if(this.isWithinDate(date))
            return Optional.of(this);
        
       return Optional.empty();
    }

    @Override
    public boolean isWithinDate(ZonedDateTime date) {
        return !this.date.toLocalDate().isAfter(date.toLocalDate());
    }
    
    public ZonedDateTime getDate() {
        return date;
    }
    
    protected boolean isOnDate(Worklog worklog) { 
        ZonedDateTime worklogZonedDateTime = DateTimeUtils.get(worklog.started,date.getZone()); 
        if(worklogZonedDateTime == null) 
            return false; 
         
        LocalDate worklogDate = worklogZonedDateTime.toLocalDate(); 
         
        return worklogDate.equals(date.toLocalDate()) || worklogDate.isAfter(date.toLocalDate()); 
    } 
    
    public Optional<DatedStatusTransition> withDate() {
        return Optional.of(this);
    }
}
