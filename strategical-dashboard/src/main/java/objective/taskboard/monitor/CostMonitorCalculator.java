package objective.taskboard.monitor;

import static java.util.Arrays.asList;
import static objective.taskboard.monitor.MonitorUtils.removeDecimal;

import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;
import objective.taskboard.followup.data.ProgressData;
import objective.taskboard.followup.data.ProgressDataPoint;
import objective.taskboard.monitor.StrategicalProjectDataSet.DataItem;
import objective.taskboard.monitor.StrategicalProjectDataSet.MonitorData;

@Component
class CostMonitorCalculator implements MonitorCalculator {
    private static final Logger log = LoggerFactory.getLogger(ScopeMonitorCalculator.class);
    public static final String CANT_CALCULATE_COST_WARNING = "Can't calculate Cost warning: Project hasn't risk percentage configured.";
    public static final String CANT_CALCULATE_COST_UNEXPECTED = "Can't calculate Cost: Unexpected error.";

    private final MonitorDataService monitoDataService;

    public CostMonitorCalculator(MonitorDataService monitorDataService) {
        this.monitoDataService = monitorDataService;
    }

    @Override
    public MonitorData calculate(ProjectFilterConfiguration project, ZoneId timezone) {
        MonitorData monitorCost = new MonitorData("Cost", "#icon-money-bag");

        DataItem expectedDataItem = new DataItem("(expected)", "Expected cost of the project represented as effort in hours.");
        DataItem warningDataItem = new DataItem("(warning)", "Range of effort in hours from the expected cost to expected cost plus risk.");
        DataItem actualDataItem = new DataItem("(actual)", "Actual project cost based on the current scope items complete.");

        try {
            monitorCost.statusDetails = "Gray is displayed when there is not enough data to measure project progress.";
            ProgressData progressData = monitoDataService.getProgressData(project, timezone);

            ProgressDataPoint actualProjection = progressData.actualProjection.get(0);
            double actualCost = actualProjection.sumEffortDone;
            double totalActualScope = actualProjection.sumEffortBacklog + actualProjection.sumEffortDone;

            ProgressDataPoint expected = progressData.expected.stream()
                    .filter(obj -> obj.date.equals(progressData.actualProjection.get(0).date))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException());

            double expectedCost = totalActualScope * expected.progress;
            double limitCost = project.getRiskPercentage() != null ? getLimitCost(project, expectedCost) : expectedCost;

            String status = verifyCostStatus(actualCost, expectedCost, limitCost);
            monitorCost.statusDetails = verifyCostStatusDetails(status);

            expectedDataItem.text = removeDecimal(expectedCost, "h");

            if (Double.compare(limitCost, expectedCost) == 0) {
                warningDataItem.text = CANT_CALCULATE_MESSAGE;
                warningDataItem.details = "Can't calculate Cost warning: Project hasn't risk percentage configured.";
            } else {
                warningDataItem.text =  removeDecimal(expectedCost, "h") + " - " + removeDecimal(limitCost, "h");
            }
            actualDataItem.text = removeDecimal(actualCost, "h");

            monitorCost.items = asList(expectedDataItem, warningDataItem, actualDataItem);

            monitorCost.status = status;
        } catch (ClusterNotConfiguredException | ProjectDatesNotConfiguredException e) { //NOSONAR
            monitorCost.items = asList(expectedDataItem, warningDataItem, actualDataItem);
            monitorCost = MonitorData.withError(monitorCost, "Can't calculate Cost: " +  e.getMessage());
        } catch (Exception e) { //NOSONAR
            log.error(e.getMessage(), e);

            monitorCost.items = asList(expectedDataItem, warningDataItem, actualDataItem);
            monitorCost = MonitorData.withError(monitorCost, "Can't calculate Cost: Unexpected error.");
        }

        return monitorCost;
    }

    private static String verifyCostStatus(double actualCost, double expectedCost, double limitCost) {
        String status = "normal";

        if (actualCost >= limitCost) {
            status = "danger";
        } else if (actualCost > expectedCost && actualCost < limitCost) {
            status = "alert";
        }
        return status;
    }

    private static String verifyCostStatusDetails(String status) {
        String statusDetails = "The actual cost is less than the beginning cost of the warning range.";

        if (status.equals("danger")) {
            statusDetails = "The actual cost is within the warning range.";
        } else if (status.equals("alert ")) {
            statusDetails = "The cost is more than the maximum cost of the warning range.";
        }

        return statusDetails;
    }
    private static double getLimitCost(ProjectFilterConfiguration project, double expectedCost) {
        return expectedCost + (expectedCost * project.getRiskPercentage().doubleValue());
    }
}
