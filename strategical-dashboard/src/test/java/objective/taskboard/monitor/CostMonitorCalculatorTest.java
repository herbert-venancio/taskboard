package objective.taskboard.monitor;

import static objective.taskboard.monitor.CostMonitorCalculator.CANT_CALCULATE_COST_UNEXPECTED;
import static objective.taskboard.monitor.CostMonitorCalculator.CANT_CALCULATE_COST_WARNING;
import static objective.taskboard.monitor.MonitorCalculator.CANT_CALCULATE_MESSAGE;
import static objective.taskboard.monitor.ProgressDataPointBuilder.progressDataPoint;

import org.junit.Test;

public class CostMonitorCalculatorTest {

    @Test
    public void givenCostSmallerThanExpectedCost_thenExpectedNormalStatusToCost() {
        given()
            .projectWithRisk(0.3)
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
            .assertLabel("Cost")
            .assertIcon("#icon-money-bag")
            .assertActual("5h")
            .assertExpected("8h")
            .assertStatus("normal")
            .assertWarning("8h - 10h");

    }

    @Test
    public void givenCostInsideCostRangeData_thenExpectedWarningStatusToCost() {
        given()
            .projectWithRisk(0.3)
            .progressDataWithActualProjection(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.8)
                    .sumEffortDone(9.0)
                    .sumEffortBacklog(2.0)
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
            .assertLabel("Cost")
            .assertIcon("#icon-money-bag")
            .assertActual("9h")
            .assertExpected("8h")
            .assertStatus("alert")
            .assertWarning("8h - 10h");
    }

    @Test
    public void givenCostBiggerThanLimit_thenExpectedDangerStatusToCost() {
        given()
            .projectWithRisk(0.3)
            .progressDataWithActualProjection(
                progressDataPoint()
                    .date("2019-04-10")
                    .progress(0.8)
                    .sumEffortDone(11.0)
                    .sumEffortBacklog(0.0)
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
            .assertLabel("Cost")
            .assertIcon("#icon-money-bag")
            .assertActual("11h")
            .assertExpected("8h")
            .assertStatus("danger")
            .assertWarning("8h - 10h");
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
            .assertLabel("Cost")
            .assertIcon("#icon-money-bag")
            .assertActual("5h")
            .assertExpected("8h")
            .assertStatus("normal")
            .assertWarning(CANT_CALCULATE_MESSAGE)
            .assertErrors(CANT_CALCULATE_COST_WARNING);
    }

    @Test
    public void givenProgressDataWithoutActualProjectionAndExpectedData_thenThrowUnexpectedErrorException() {
        given()
            .progressDataWithActualProjection(
                progressDataPoint()
            )
            .progressDataWithExpected(
                progressDataPoint()
            )

        .whenCalculate()

        .then()
            .assertLabel("Cost")
            .assertIcon("#icon-money-bag")
            .assertActual(CANT_CALCULATE_MESSAGE)
            .assertExpected(CANT_CALCULATE_MESSAGE)
            .assertStatus(null)
            .assertWarning(CANT_CALCULATE_MESSAGE)
            .assertErrors(CANT_CALCULATE_COST_UNEXPECTED);
    }

    private MonitorCalculatorDSL given() {
        return MonitorCalculatorDSL.forCost();
    }

}
