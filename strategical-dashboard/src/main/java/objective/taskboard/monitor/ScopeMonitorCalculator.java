package objective.taskboard.monitor;

import static java.util.Arrays.asList;
import static objective.taskboard.monitor.MonitorUtils.removeDecimal;

import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;
import objective.taskboard.followup.data.ProgressData;
import objective.taskboard.followup.data.ProgressDataPoint;
import objective.taskboard.monitor.StrategicalProjectDataSet.DataItem;
import objective.taskboard.monitor.StrategicalProjectDataSet.MonitorData;

@Component
class ScopeMonitorCalculator implements MonitorCalculator {
    private static final Logger log = LoggerFactory.getLogger(ScopeMonitorCalculator.class);
    public static final String CANT_CALCULATE_SCOPE_WARNING = "Can't calculate Scope warning: Project hasn't risk percentage configured.";
    public static final String CANT_CALCULATE_SCOPE_UNEXPECTED = "Can't calculate Scope: Unexpected error.";

    private final MonitorDataService monitorDataService;

    @Autowired
    public ScopeMonitorCalculator(MonitorDataService monitorDataService) {
        this.monitorDataService = monitorDataService;
    }

    @Override
    public MonitorData calculate(ProjectFilterConfiguration project, ZoneId timezone) {
        MonitorData monitorScope = new MonitorData("Scope", "#icon-list");

        DataItem expectedDataItem = new DataItem("(expected)", "Expected percentage of scope progress.");
        DataItem warningDataItem = new DataItem("(warning)", "Range of percentages starting with the expected scope progress minus risk and ends up with the expected scope progress.");
        DataItem actualDataItem = new DataItem("(actual)", "Percentage of current scope progress.");

        try {
            monitorScope.statusDetails = "Gray is displayed when there is not enough data to measure project progress.";
 
            ProgressData progressData = monitorDataService.getProgressData(project, timezone);

            double actualScope = progressData.actualProjection.get(0).progress * 100;
            ProgressDataPoint expected = progressData.expected.stream()
                    .filter(progressDataPoint -> progressDataPoint.date.equals(progressData.actualProjection.get(0).date))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException());

            double expectedScope = expected.progress * 100;
            double riskPercentage = project.getRiskPercentage() != null ? project.getRiskPercentage().doubleValue() : 0.0;
            double expectedScopeWithRisk = getExpectedScope(expectedScope, riskPercentage);

            String status = verifyScopeStatus(actualScope, expectedScope, expectedScopeWithRisk);
            monitorScope.statusDetails = verifyScopeStatusDetails(status);

            expectedDataItem.text = removeDecimal(expectedScope, "%");

            if (Double.compare(expectedScopeWithRisk, expectedScope) == 0) {
                warningDataItem.text = CANT_CALCULATE_MESSAGE;
                warningDataItem.details = "Can't calculate Scope warning: Project hasn't risk percentage configured.";
            } else {
                warningDataItem.text = removeDecimal(expectedScopeWithRisk, "%") + " - " + removeDecimal(expectedScope, "%");
            }

            monitorScope.status = status;

            actualDataItem.text = removeDecimal(actualScope, "%");

            monitorScope.items = asList(expectedDataItem, warningDataItem, actualDataItem);
        } catch (ClusterNotConfiguredException | ProjectDatesNotConfiguredException e) { //NOSONAR
            monitorScope.items = asList(expectedDataItem, warningDataItem, actualDataItem);
            monitorScope = MonitorData.withError(monitorScope, "Can't calculate Scope: " +  e.getMessage());
        } catch (Exception e) { //NOSONAR
            log.error(e.getMessage(), e);

            monitorScope.items = asList(expectedDataItem, warningDataItem, actualDataItem);
            monitorScope = MonitorData.withError(monitorScope, "Can't calculate Scope: Unexpected error.");
        }

        return monitorScope;
    }

    private static String verifyScopeStatus(double actualScope, double expectedScope, double expectedScopeWithRisk) {
        if (actualScope > expectedScopeWithRisk && actualScope < expectedScope)
            return "alert";
        else if (actualScope <= expectedScopeWithRisk)
            return "danger";
        return "normal";
    }

    private static String verifyScopeStatusDetails(String status) {
        String statusDetail = "The actual percentage of scope progress is over the warning range.";
        
        if (status.equals("alert")) {
            statusDetail = "The actual percentage of scope progress is within the warning range.";
        } else if (status.equals("danger")) {
            statusDetail = "The scope progress is less than the initial percentage of the warning range.";
        }

        return statusDetail;
    }
    private static double getExpectedScope(double expectedScope, double riskPercentage) {
        return expectedScope - (expectedScope * riskPercentage);
    }

}
