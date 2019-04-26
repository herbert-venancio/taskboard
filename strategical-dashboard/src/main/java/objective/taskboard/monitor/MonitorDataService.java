package objective.taskboard.monitor;

import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.budget.BudgetChartCalculator;
import objective.taskboard.followup.budget.BudgetChartData;
import objective.taskboard.followup.data.FollowupProgressCalculator;
import objective.taskboard.followup.data.ProgressData;

@Component
@CacheConfig(cacheNames = CacheConfiguration.DASHBOARD_MONITORS)
class MonitorDataService {

    private final FollowupProgressCalculator progressCalculator;
    private final BudgetChartCalculator budgetChartCalculator;

    @Autowired
    public MonitorDataService(
            FollowupProgressCalculator progressCalculator,
            BudgetChartCalculator budgetChartCalculator) {
        this.progressCalculator = progressCalculator;
        this.budgetChartCalculator = budgetChartCalculator;
    }

    @Cacheable(key = "'ProgressData' + #project.getProjectKey() + #timezone.toString()")
    public ProgressData getProgressData(ProjectFilterConfiguration project, ZoneId timezone) {
        return progressCalculator.calculateWithExpectedProjection(timezone, project.getProjectKey(), project.getProjectionTimespan());
    }

    @Cacheable(key = "'BudgetChartData' + #project.getProjectKey() + #timezone.toString()")
    public BudgetChartData getBudgetChartData(ProjectFilterConfiguration project, ZoneId timezone) {
        return budgetChartCalculator.calculate(timezone, project);
    }

}
