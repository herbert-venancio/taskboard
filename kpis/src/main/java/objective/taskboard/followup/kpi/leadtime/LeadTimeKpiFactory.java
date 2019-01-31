package objective.taskboard.followup.kpi.leadtime;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.StatusTransitionChain;

public class LeadTimeKpiFactory {

    private Map<KpiLevel, Set<String>> leadStatusMap;
    private ZoneId timezone;

    public LeadTimeKpiFactory(Map<KpiLevel, Set<String>> leadStatusMap, ZoneId timezone) {
        this.leadStatusMap = leadStatusMap;
        this.timezone = timezone;
    }

    public LeadTimeKpi create(IssueKpi issue) {
        String issueKey = issue.getIssueKey();
        String issueType = issue.getIssueTypeName();
        StatusTransitionChain statuses = issue.getStatusChain(timezone);
        String lastStatus = statuses.getCurrentStatusName();
        Set<String> leadStatusesNames = leadStatusMap.get(issue.getLevel());
        StatusTransitionChain leadStatuses = statuses.getStatusSubChain(leadStatusesNames);
        Optional<Instant> startDate = leadStatuses.getMinimumDate().map(ZonedDateTime::toInstant);
        Optional<Instant> endDate = leadStatuses.getExitDate().map(ZonedDateTime::toInstant);
        long leadTime = leadStatuses.getDurationInDaysEndDateIncluded();
        return new LeadTimeKpi(issueKey, issueType, startDate, endDate, leadTime, lastStatus);
    }

}
