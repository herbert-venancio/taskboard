package objective.taskboard.followup.kpi.touchtime;

import java.time.ZoneId;
import java.util.List;

import org.springframework.stereotype.Service;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KpiTouchTimeProperties;
import objective.taskboard.jira.properties.JiraProperties;

@Service
public class TouchTimeByIssueKpiStrategyFactory implements TouchTimeKpiStrategyFactory<TouchTimeByIssueKpiDataPoint> {

    private KpiTouchTimeProperties touchTimeProperties;
    private IssueKpiService issueKpiService;
    private JiraProperties jiraProperties;

    public TouchTimeByIssueKpiStrategyFactory(KpiTouchTimeProperties touchTimeProperties, IssueKpiService issueKpiService,
            JiraProperties jiraProperties) {
        this.touchTimeProperties = touchTimeProperties;
        this.issueKpiService = issueKpiService;
        this.jiraProperties = jiraProperties;
    }

    @Override
    public TouchTimeByIssueKpiStrategy getStrategy(KpiLevel level, ProjectFilterConfiguration projectConfiguration, ZoneId timezone) {
        List<IssueKpi> issues = issueKpiService.getIssuesFromCurrentState(
                projectConfiguration.getProjectKey(), timezone, level);

        return new TouchTimeByIssueKpiStrategy(timezone, issues, getProgressingStatuses(level));
    }

    private List<String> getProgressingStatuses(KpiLevel level) {
        final List<String> allProgressingStatuses = touchTimeProperties.getProgressingStatuses();
        return level.filterProgressingStatuses(allProgressingStatuses, jiraProperties);
    }

}
