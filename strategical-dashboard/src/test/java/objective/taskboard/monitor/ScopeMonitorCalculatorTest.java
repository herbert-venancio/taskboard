package objective.taskboard.monitor;

import static objective.taskboard.monitor.MonitorCalculator.CANT_CALCULATE_MESSAGE;
import static objective.taskboard.monitor.ProgressDataPointBuilder.progressDataPoint;
import static objective.taskboard.monitor.ScopeMonitorCalculator.CANT_CALCULATE_SCOPE_UNEXPECTED;
import static objective.taskboard.monitor.ScopeMonitorCalculator.CANT_CALCULATE_SCOPE_WARNING;

import org.junit.Test;

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
            )
            .progressDataWithExpected(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.7)
                    .sumEffortDone(5.0)
                    .sumEffortBacklog(6.0)
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
            )
            .progressDataWithExpected(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.7)
                    .sumEffortDone(5.0)
                    .sumEffortBacklog(6.0)
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
            )
            .progressDataWithExpected(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.7)
                    .sumEffortDone(5.0)
                    .sumEffortBacklog(6.0)
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
            )
            .progressDataWithExpected(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.7)
                    .sumEffortDone(5.0)
                    .sumEffortBacklog(6.0)
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
            )
            .progressDataWithExpected(
                progressDataPoint()
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

    private MonitorCalculatorDSL given() {
        return MonitorCalculatorDSL.forScope();
    }

}
