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
class ScopeMonitorCalculator implements MonitorCalculator {
    private static final Logger log = LoggerFactory.getLogger(ScopeMonitorCalculator.class);
    public static final String CANT_CALCULATE_SCOPE_WARNING = "Can't calculate Scope warning: Project hasn't risk percentage configured.";
    public static final String CANT_CALCULATE_SCOPE_UNEXPECTED = "Can't calculate Scope: Unexpected error.";

    private final FollowupProgressCalculator progressCalculator;

    @Autowired
    public ScopeMonitorCalculator(FollowupProgressCalculator progressCalculator) {
        this.progressCalculator = progressCalculator;
    }

    @Override
    @Cacheable(value = CacheConfiguration.STRATEGICAL_DASHBOARD, key="{'scope', #project.getProjectKey(), #timezone}")
    public MonitorData calculate(ProjectFilterConfiguration project, ZoneId timezone) {
        MonitorDataBuilder monitorScope = MonitorData.builder()
                .withLabel("Scope")
                .withIcon("#icon-list")
                .withStatusDetails("Gray is displayed when there is not enough data to measure project progress.");

        monitorScope
            .expectedDetails("Expected percentage of scope progress.")
            .warningDetails("Range of percentages starting with the expected scope progress minus risk and ends up with the expected scope progress.")
            .actualDetails("Percentage of current scope progress.");

        try {
            ProgressData progressData = progressCalculator.calculateWithExpectedProjection(timezone, project.getProjectKey(), project.getProjectionTimespan());

            double actualScope = progressData.actualProjection.get(0).progress * 100;
            ProgressDataPoint expected = progressData.expected.stream()
                    .filter(progressDataPoint -> progressDataPoint.date.equals(progressData.actualProjection.get(0).date))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException());

            double expectedScope = expected.progress * 100;
            double riskPercentage = project.getRiskPercentage() != null ? project.getRiskPercentage().doubleValue() : 0.0;
            double expectedScopeWithRisk = getExpectedScope(expectedScope, riskPercentage);

            Status statusEnum = verifyScopeStatus(actualScope, expectedScope, expectedScopeWithRisk);

            monitorScope.withStatusDetails(verifyScopeStatusDetails(statusEnum));
            monitorScope.expectedValue(removeDecimal(expectedScope, "%"));

            if (Double.compare(expectedScopeWithRisk, expectedScope) == 0) {
                monitorScope.warningValue(CANT_CALCULATE_MESSAGE);
                monitorScope.warningDetails(CANT_CALCULATE_SCOPE_WARNING);
            } else {
                monitorScope.warningValue(removeDecimal(expectedScopeWithRisk, "%") + " - " + removeDecimal(expectedScope, "%"));
            }

            monitorScope.withStatus(statusEnum.status());
            monitorScope.actualValue(removeDecimal(actualScope, "%"));
        } catch (ClusterNotConfiguredException | ProjectDatesNotConfiguredException e) { //NOSONAR
            return  monitorScope.withError("Can't calculate Scope: " +  e.getMessage());
        } catch (Exception e) { //NOSONAR
            log.error(e.getMessage(), e);
            return monitorScope.withError(CANT_CALCULATE_SCOPE_UNEXPECTED);
        }

        return monitorScope.build();
    }

    private static Status verifyScopeStatus(double actualScope, double expectedScope, double expectedScopeWithRisk) {
        if (actualScope > expectedScopeWithRisk && actualScope < expectedScope)
            return StrategicalProjectDataSet.Status.ALERT;
        else if (actualScope <= expectedScopeWithRisk)
            return StrategicalProjectDataSet.Status.DANGER;

        return StrategicalProjectDataSet.Status.NORMAL;
    }

    private static String verifyScopeStatusDetails(Status status) {
        switch(status) {
            case ALERT:
                return "The actual percentage of scope progress is over the warning range.";
            case DANGER:
                return "The scope progress is less than the initial percentage of the warning range.";
            default:
                return "The actual percentage of scope progress is over the warning range.";
        }
    }

    private static double getExpectedScope(double expectedScope, double riskPercentage) {
        return expectedScope - (expectedScope * riskPercentage);
    }

}
