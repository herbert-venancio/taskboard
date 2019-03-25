package objective.taskboard.followup.kpi.touchtime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiDataService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KpiTouchTimeProperties;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.RangeUtils;

@Service
public class TouchTimeByWeekKpiStrategyFactory implements TouchTimeKpiStrategyFactory<TouchTimeByWeekKpiDataPoint> {

    private KpiTouchTimeProperties touchTimeProperties;
    private KpiDataService kpiDataService;
    private JiraProperties jiraProperties;

    @Autowired
    public TouchTimeByWeekKpiStrategyFactory(KpiTouchTimeProperties touchTimeProperties, KpiDataService kpiDataService,
            JiraProperties jiraProperties) {
        this.touchTimeProperties = touchTimeProperties;
        this.kpiDataService = kpiDataService;
        this.jiraProperties = jiraProperties;
    }

    @Override
    public TouchTimeByWeekKpiStrategy getStrategy(KpiLevel level, ProjectFilterConfiguration projectConfiguration, ZoneId timezone) {
        List<IssueKpi> issues = kpiDataService.getIssuesFromCurrentState(
                projectConfiguration.getProjectKey(), timezone, level);
        Range<LocalDate> projectRange = getRangeOrCry(projectConfiguration);

        if (level.equals(KpiLevel.SUBTASKS))
            return new TouchTimeByWeekKpiSubtaskStrategy(projectRange, timezone, issues, touchTimeProperties.getTouchTimeSubtaskConfigs());

        return new TouchTimeByWeekKpiProgressingStatusesStrategy(projectRange, timezone, issues,
                getProgressingStatuses(level));
    }

    private Range<LocalDate> getRangeOrCry(ProjectFilterConfiguration project) {
        Optional<LocalDate> opStartDate = project.getStartDate();
        Optional<LocalDate> opDeliveryDate = project.getDeliveryDate();
        if (!opStartDate.isPresent() || !opDeliveryDate.isPresent())
            throw new ProjectDatesNotConfiguredException();

        LocalDate startDate = opStartDate.get();
        LocalDate deliveryDate = opDeliveryDate.get();
        return RangeUtils.between(startDate, deliveryDate);
    }

    private List<String> getProgressingStatuses(KpiLevel level) {
        final List<String> allProgressingStatuses = touchTimeProperties.getProgressingStatuses();
        return level.filterProgressingStatuses(allProgressingStatuses, jiraProperties);
    }
}
