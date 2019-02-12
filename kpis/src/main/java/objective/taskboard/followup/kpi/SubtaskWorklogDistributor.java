package objective.taskboard.followup.kpi;

import java.util.List;
import java.util.Optional;

public class SubtaskWorklogDistributor {

    public void distributeWorklogs(IssueKpi issueKpi, List<ZonedWorklog> zonedWorklogs) {
        if(!issueKpi.firstStatus().isPresent())
            return;

        zonedWorklogs.stream().forEach(w -> {
            Optional<StatusTransition> status = findStatus(issueKpi.firstStatus().get(), w);
            status.ifPresent(s -> s.putWorklog(w));
        });
    }

    Optional<StatusTransition> findStatus(StatusTransition status, ZonedWorklog w){
        if (!status.isProgressingStatus()) {
            return status.flatNext((next) -> findStatus(next,w));
        }

        boolean shouldProceedSearch = status.hasAnyNextThatReceivesWorklog(w);

        if(shouldProceedSearch) {
            return status.flatNext((next) -> findStatus(next,w));
        }

        return Optional.of(status);
    }

}
