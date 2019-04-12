package objective.taskboard.followup.kpi.filters;

import java.util.List;

import objective.taskboard.followup.kpi.IssueKpi;

public class StatusExcludedFromFollowupFilter implements KpiItemFilter {

    private List<String> statusExcludedFromFollowUp;
    
    public StatusExcludedFromFollowupFilter(List<String> statusExcludedFromFollowUp) {
        this.statusExcludedFromFollowUp = statusExcludedFromFollowUp;
    }

    @Override
    public boolean test(IssueKpi issue) {
        return issue.getLastTransitedStatus().map(
                status -> !statusExcludedFromFollowUp.contains(status)
                ).orElse(false);
    }

}
