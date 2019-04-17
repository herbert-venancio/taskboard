package objective.taskboard.followup.kpi.services;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.FollowUpSnapshot;
import objective.taskboard.followup.FollowUpSnapshotService;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.filters.KpiFilterService;
import objective.taskboard.followup.kpi.filters.KpiItemFilter;
import objective.taskboard.followup.kpi.filters.ProjectRangeByConfiguration;
import objective.taskboard.followup.kpi.filters.ProjectTimelineRange;
import objective.taskboard.followup.kpi.filters.WithinRangeFilter;
import objective.taskboard.jira.ProjectService;

@Service
public class KpiDataService {
    
    private IssueKpiService issueKpiService;
    
    private FollowUpSnapshotService followupSnapshotService;
    
    private ProjectService projectService;
    
    private KpiFilterService filterService;

    @Autowired
    public KpiDataService(
            IssueKpiService issueKpiService, 
            FollowUpSnapshotService followupSnapshotService,
            ProjectService projectService, 
            KpiFilterService filterService) {
        this.issueKpiService = issueKpiService;
        this.followupSnapshotService = followupSnapshotService;
        this.projectService = projectService;
        this.filterService = filterService;
    }
    
    public List<IssueKpi> getIssuesFromCurrentStateWithDefaultFilters(String projectKey, ZoneId timezone, KpiLevel kpiLevel){
        List<IssueKpi> allIssues = issueKpiService.getIssuesFromCurrentState(projectKey, timezone);
        return allIssues.stream().filter(getDefaultFilters(kpiLevel)).collect(Collectors.toList());
    }
    
    private Predicate<IssueKpi> getDefaultFilters(KpiLevel level){
        KpiItemFilter statusFilter = filterService.getFilterStatusExcludedFromFollowup();
        KpiItemFilter levelFilter = filterService.getLevelFilter(level);
        
        return Stream.of(statusFilter,levelFilter).reduce(w -> true, KpiItemFilter::concat);
    }
    
    public List<IssueKpi> getIssuesFromCurrentProjectRange(String projectKey, ZoneId timezone, KpiLevel kpiLevel){
        List<IssueKpi> allIssues = getIssuesFromCurrentStateWithDefaultFilters(projectKey, timezone, kpiLevel);
        ProjectFilterConfiguration project =  projectService.getTaskboardProjectOrCry(projectKey);
        ProjectTimelineRange range = new ProjectRangeByConfiguration(project);
        
        return allIssues.stream().filter(new WithinRangeFilter(timezone, range)).collect(Collectors.toList());
    }

    public FollowUpSnapshot getSnapshotFromCurrentState(String projectKey,ZoneId timezone) {
        return followupSnapshotService.getFromCurrentState(timezone, projectKey);
    }

    public FollowUpSnapshot getSnapshot(String projectKey, ZoneId timezone, Optional<LocalDate> date) {
        return followupSnapshotService.get(date,timezone, projectKey);
    }

    public List<IssueKpi> getIssuesFromAnalyticDataSet(Optional<AnalyticsTransitionsDataSet> analyticTransitionDs) {
        return issueKpiService.getIssues(analyticTransitionDs);
    }
    
}
