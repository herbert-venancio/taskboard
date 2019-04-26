package objective.taskboard.monitor;

import static objective.taskboard.monitor.MonitorCalculator.CANT_CALCULATE_MESSAGE;
import static objective.taskboard.monitor.MonitorCalculatorDSL.assertMonitorError;
import static objective.taskboard.monitor.ProgressDataPointBuilder.progressDataPoint;
import static objective.taskboard.monitor.ScopeMonitorCalculator.CANT_CALCULATE_SCOPE_UNEXPECTED;
import static objective.taskboard.monitor.ScopeMonitorCalculator.CANT_CALCULATE_SCOPE_WARNING;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;

@RunWith(MockitoJUnitRunner.class)
public class ScopeMonitorCalculatorTest {

    @Test
    public void givenActualScopeBiggerOrEqualsThanExpected_thenExpectedNormalStatusToScope() {
        given()
            .projectWithRisk(0.2)
            .progressDataWithActualProjection(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.8)
                    .sumEffortDone(5.0)
                    .sumEffortBacklog(6.0)
                    .build()
            )
            .progressDataWithExpected(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.7)
                    .sumEffortDone(5.0)
                    .sumEffortBacklog(6.0)
                    .build()
            )

        .whenCalculate()

        .then()
            .assertLabel("Scope")
            .assertIcon("#icon-list")
            .assertActual("80%")
            .assertExpected("70%")
            .assertStatus("normal")
            .assertWarning("56% - 70%");
    }

    @Test
    public void givenActualScopeInsideScopeRange_thenExpectedAlertStatusToScope() {
        given()
            .projectWithRisk(0.2)
            .progressDataWithActualProjection(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.6)
                    .sumEffortDone(5.0)
                    .sumEffortBacklog(6.0)
                    .build()
            )
            .progressDataWithExpected(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.7)
                    .sumEffortDone(5.0)
                    .sumEffortBacklog(6.0)
                    .build()
            )

        .whenCalculate()

        .then()
            .assertLabel("Scope")
            .assertIcon("#icon-list")
            .assertActual("60%")
            .assertExpected("70%")
            .assertStatus("alert")
            .assertWarning("56% - 70%");
    }

    @Test
    public void givenActualScopeSmallerOrEqualsScopeWithRisk_thenExpectedDangerStatusToScope() {
        given()
        .projectWithRisk(0.2)
            .progressDataWithActualProjection(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.5)
                    .sumEffortDone(5.0)
                    .sumEffortBacklog(6.0)
                    .build()
            )
            .progressDataWithExpected(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.7)
                    .sumEffortDone(5.0)
                    .sumEffortBacklog(6.0)
                    .build()
            )

        .whenCalculate()

        .then()
            .assertLabel("Scope")
            .assertIcon("#icon-list")
            .assertActual("50%")
            .assertExpected("70%")
            .assertStatus("danger")
            .assertWarning("56% - 70%");
    }

    @Test
    public void givenProjectWithoutRisk_thenNoResultsToWarning() {
        given()
            .progressDataWithActualProjection(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.8)
                    .sumEffortDone(5.0)
                    .sumEffortBacklog(6.0)
                    .build()
            )
            .progressDataWithExpected(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.7)
                    .sumEffortDone(5.0)
                    .sumEffortBacklog(6.0)
                    .build()
            )

        .whenCalculate()

        .then()
            .assertLabel("Scope")
            .assertIcon("#icon-list")
            .assertActual("80%")
            .assertExpected("70%")
            .assertStatus("normal")
            .assertWarning(CANT_CALCULATE_MESSAGE)
            .assertErrors(CANT_CALCULATE_SCOPE_WARNING);
    }

    @Test
    public void givenProgressDataWithoutActulProjectionandExpectedData_thenThrowUnexpectedErrorException() {
        given()
            .progressDataWithActualProjection(
                progressDataPoint()
                    .build()
            )
            .progressDataWithExpected(
                progressDataPoint()
                    .build()
            )

        .whenCalculate()

        .then()
            .assertLabel("Scope")
            .assertIcon("#icon-list")
            .assertActual(CANT_CALCULATE_MESSAGE)
            .assertExpected(CANT_CALCULATE_MESSAGE)
            .assertStatus(null)
            .assertWarning(CANT_CALCULATE_MESSAGE)
            .assertErrors(CANT_CALCULATE_SCOPE_UNEXPECTED);
    }

    @Test
    public void givenProjectDateNotConfigured_thenThrowProjectDatesNotConfiguredException() {
        assertMonitorError(
            given(),
            ProjectDatesNotConfiguredException.fromProject(),
            "Can't calculate Scope: The project has no start or delivery date.");
    }

    @Test
    public void givenClusterNotConfigured_thenThrowClusterNotConfiguredException() {
        assertMonitorError(
                given(),
                ClusterNotConfiguredException.fromProject(),
                "Can't calculate Scope: No cluster configuration found.");
    }

    private MonitorCalculatorDSL given() {
        return MonitorCalculatorDSL.asScope();
    }

}
