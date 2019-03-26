package objective.taskboard.followup.kpi.touchtime;

import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiDataService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KpiTouchTimeProperties;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.IssueType.IssueTypeDetails;

@Service
public class TouchTimeByIssueKpiStrategyFactory implements TouchTimeKpiStrategyFactory<TouchTimeByIssueKpiDataPoint> {

    private KpiTouchTimeProperties touchTimeProperties;
    private KpiDataService kpiDataService;
    private JiraProperties jiraProperties;
    private MetadataService metadataService;

    @Autowired
    public TouchTimeByIssueKpiStrategyFactory(
            KpiTouchTimeProperties touchTimeProperties,
            KpiDataService kpiDataService,
            JiraProperties jiraProperties,
            MetadataService metadataService) {
        this.touchTimeProperties = touchTimeProperties;
        this.kpiDataService = kpiDataService;
        this.jiraProperties = jiraProperties;
        this.metadataService = metadataService;
    }

    @Override
    public TouchTimeByIssueKpiStrategy getStrategy(KpiLevel level, ProjectFilterConfiguration projectConfiguration, ZoneId timezone) {
        List<IssueKpi> issues = kpiDataService.getIssuesFromCurrentProjectRange(
                projectConfiguration.getProjectKey(), timezone, level);

        Set<JiraIssueTypeDto> subtaskTypes = jiraProperties.getIssuetype().getSubtasks().stream()
                .map(IssueTypeDetails::getId)
                .map(metadataService::getIssueTypeById)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (level.equals(KpiLevel.SUBTASKS))
            return new TouchTimeByIssueGroupedByProgressingStatusesKpiStrategy(timezone, issues, getProgressingStatuses(level));

        return new TouchTimeByIssueGroupedBySubtaskTypeKpiStrategy(timezone, issues, getProgressingStatuses(level), subtaskTypes);
    }

    private List<String> getProgressingStatuses(KpiLevel level) {
        final List<String> allProgressingStatuses = touchTimeProperties.getProgressingStatuses();
        return level.filterProgressingStatuses(allProgressingStatuses, jiraProperties);
    }

}
