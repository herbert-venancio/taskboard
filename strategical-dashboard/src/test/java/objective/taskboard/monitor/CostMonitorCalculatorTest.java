package objective.taskboard.monitor;

import static objective.taskboard.monitor.CostMonitorCalculator.CANT_CALCULATE_COST_UNEXPECTED;
import static objective.taskboard.monitor.CostMonitorCalculator.CANT_CALCULATE_COST_WARNING;
import static objective.taskboard.monitor.MonitorCalculator.CANT_CALCULATE_MESSAGE;
import static objective.taskboard.monitor.MonitorCalculatorDSL.assertMonitorError;
import static objective.taskboard.monitor.ProgressDataPointBuilder.progressDataPoint;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;

@RunWith(MockitoJUnitRunner.class)
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
            .assertLabel("Cost")
            .assertIcon("#icon-money-bag")
            .assertActual("5h")
            .assertExpected("8h")
            .assertStatus("normal")
            .assertWarning(CANT_CALCULATE_MESSAGE)
            .assertErrors(CANT_CALCULATE_COST_WARNING);;
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
            .assertLabel("Cost")
            .assertIcon("#icon-money-bag")
            .assertActual(CANT_CALCULATE_MESSAGE)
            .assertExpected(CANT_CALCULATE_MESSAGE)
            .assertStatus(null)
            .assertWarning(CANT_CALCULATE_MESSAGE)
            .assertErrors(CANT_CALCULATE_COST_UNEXPECTED);
    }

    @Test
    public void givenProjectDateNotConfigured_thenThrowProjectDatesNotConfiguredException() {
        assertMonitorError(
            given(),
            ProjectDatesNotConfiguredException.fromProject(),
            "Can't calculate Cost: The project has no start or delivery date.");
    }

    @Test
    public void givenClusterNotConfigured_thenThrowClusterNotConfiguredException() {
        assertMonitorError(
                given(),
                ClusterNotConfiguredException.fromProject(),
                "Can't calculate Cost: No cluster configuration found.");
    }

    private MonitorCalculatorDSL given() {
        return MonitorCalculatorDSL.asCost();
    }

}
