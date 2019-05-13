package objective.taskboard.monitor;

import static objective.taskboard.monitor.MonitorUtils.removeDecimal;

import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import objective.taskboard.config.CacheConfiguration;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;
import objective.taskboard.followup.data.FollowupProgressCalculator;
import objective.taskboard.followup.data.ProgressData;
import objective.taskboard.followup.data.ProgressDataPoint;
import objective.taskboard.monitor.StrategicalProjectDataSet.MonitorData;
import objective.taskboard.monitor.StrategicalProjectDataSet.Status;

@Component
class CostMonitorCalculator implements MonitorCalculator {
    private static final Logger log = LoggerFactory.getLogger(ScopeMonitorCalculator.class);
    public static final String CANT_CALCULATE_COST_WARNING = "Can't calculate Cost warning: Project hasn't risk percentage configured.";
    public static final String CANT_CALCULATE_COST_UNEXPECTED = "Can't calculate Cost: Unexpected error.";

    private final FollowupProgressCalculator progressCalculator;

    @Autowired
    public CostMonitorCalculator(FollowupProgressCalculator progressCalculator) {
        this.progressCalculator = progressCalculator;
    }

    @Override
    @Cacheable(value = CacheConfiguration.STRATEGICAL_DASHBOARD, key="{'cost', #project.getProjectKey(), #timezone}")
    public MonitorData calculate(ProjectFilterConfiguration project, ZoneId timezone) {
        MonitorDataBuilder monitorCost = MonitorData.builder()
                .withLabel("Cost")
                .withIcon("#icon-money-bag")
                .withStatusDetails("Gray is displayed when there is not enough data to measure project progress.");

        monitorCost
            .expectedDetails("Expected cost of the project represented as effort in hours.")
            .warningDetails("Range of effort in hours from the expected cost to expected cost plus risk.")
            .actualDetails("Actual project cost based on the current scope items complete.");

        try {
            ProgressData progressData = progressCalculator.calculateWithExpectedProjection(timezone, project.getProjectKey(), project.getProjectionTimespan());

            ProgressDataPoint actualProjection = progressData.actualProjection.get(0);
            double actualCost = actualProjection.sumEffortDone;
            double totalActualScope = actualProjection.sumEffortBacklog + actualProjection.sumEffortDone;

            ProgressDataPoint expected = progressData.expected.stream()
                    .filter(obj -> obj.date.equals(progressData.actualProjection.get(0).date))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException());

            double expectedCost = totalActualScope * expected.progress;
            double limitCost = project.getRiskPercentage() != null ? getLimitCost(project, expectedCost) : expectedCost;

            Status statusEnum = verifyCostStatus(actualCost, expectedCost, limitCost);

            monitorCost.withStatusDetails(verifyCostStatusDetails(statusEnum));
            monitorCost.expectedValue(removeDecimal(expectedCost, "h"));

            if (Double.compare(limitCost, expectedCost) == 0) {
                monitorCost.warningValue(CANT_CALCULATE_MESSAGE);
                monitorCost.warningDetails(CANT_CALCULATE_COST_WARNING);
            } else {
                monitorCost.warningValue(removeDecimal(expectedCost, "h") + " - " + removeDecimal(limitCost, "h"));
            }

            monitorCost.actualValue(removeDecimal(actualCost, "h"));
            monitorCost.withStatus(statusEnum.status());
        } catch (ClusterNotConfiguredException | ProjectDatesNotConfiguredException e) { //NOSONAR
            return monitorCost.withError("Can't calculate Cost: " +  e.getMessage());
        } catch (Exception e) { //NOSONAR
            log.error(e.getMessage(), e);
            return monitorCost.withError(CANT_CALCULATE_COST_UNEXPECTED);
        }

        return monitorCost.build();
    }

    private static Status verifyCostStatus(double actualCost, double expectedCost, double limitCost) {
        if (actualCost >= limitCost) {
            return StrategicalProjectDataSet.Status.DANGER;
        } else if (actualCost > expectedCost) {
            return StrategicalProjectDataSet.Status.ALERT;
       }

        return StrategicalProjectDataSet.Status.NORMAL;
    }

    private static String verifyCostStatusDetails(Status status) {
        switch(status) {
            case DANGER:
                return "The actual cost is within the warning range.";
            case ALERT:
                return "The cost is more than the maximum cost of the warning range.";
            default:
                return "The actual cost is less than the beginning cost of the warning range.";
        }
    }
    private static double getLimitCost(ProjectFilterConfiguration project, double expectedCost) {
        return expectedCost + (expectedCost * project.getRiskPercentage().doubleValue());
    }
}
