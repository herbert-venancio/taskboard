package objective.taskboard.followup.kpi;

import java.util.List;
import java.util.Optional;

import objective.taskboard.data.Worklog;

public class SubtaskWorklogDistributor {
    
    public void distributeWorklogs(IssueKpi issueKpi, List<Worklog> worklogs) {
        if(!issueKpi.firstStatus().isPresent())
            return;
        
        worklogs.stream().forEach(w -> {
            Optional<StatusTransition> status = findStatus(issueKpi.firstStatus().get(), w);
            status.ifPresent(s -> s.putWorklog(w));
        });
    }

    Optional<StatusTransition> findStatus(StatusTransition status, Worklog worklog){
        if (!status.isProgressingStatus()) {
            return status.flatNext((next) -> findStatus(next,worklog));
        } 
        
        boolean shouldProceedSearch = status.hasAnyNextThatReceivesWorklog(worklog);
         
        if(shouldProceedSearch) { 
            return status.flatNext((next) -> findStatus(next,worklog));
        }
        
        return Optional.of(status);
    }    

}
