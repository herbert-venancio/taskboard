package objective.taskboard.followup.kpi;

import java.time.ZoneId;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.followup.FollowUpSnapshot;
import objective.taskboard.followup.FollowUpSnapshotService;

@Service
public class KpiDataService {
    
    private IssueKpiService issueKpiService;
    
    private FollowUpSnapshotService followupSnapshotService;

    @Autowired
    public KpiDataService(IssueKpiService issueKpiService, FollowUpSnapshotService followupSnapshotService) {
        this.issueKpiService = issueKpiService;
        this.followupSnapshotService = followupSnapshotService;
    }
    
    public List<IssueKpi> getIssuesFromCurrentState(String projectKey, ZoneId timezone, KpiLevel kpiLevel){
        return issueKpiService.getIssuesFromCurrentState(projectKey, timezone, kpiLevel);
    }

    public FollowUpSnapshot getSnapshotFromCurrentState(ZoneId timezone,String projectKey) {
        return followupSnapshotService.getFromCurrentState(timezone, projectKey);
    }
    
}
