package objective.taskboard.followup.kpi.touchtime;

import java.time.ZoneId;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.kpi.KpiLevel;

interface TouchTimeKpiStrategyFactory<T> {
    TouchTimeKpiStrategy<T> getStrategy(KpiLevel kpiLevel, ProjectFilterConfiguration projectConfiguration, ZoneId timezone);
}
