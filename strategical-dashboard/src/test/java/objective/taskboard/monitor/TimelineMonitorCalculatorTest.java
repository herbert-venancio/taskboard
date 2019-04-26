package objective.taskboard.monitor;

import static objective.taskboard.monitor.MonitorCalculator.CANT_CALCULATE_MESSAGE;
import static objective.taskboard.monitor.MonitorCalculatorDSL.assertMonitorError;
import static objective.taskboard.monitor.TimelineMonitorCalculator.CANT_CALCULATE_TIMELINE_UNEXPECTED;
import static objective.taskboard.monitor.TimelineMonitorCalculator.CANT_CALCULATE_TIMELINE_WARNING;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;
import objective.taskboard.rules.LocaleRule;

@RunWith(MockitoJUnitRunner.class)
public class TimelineMonitorCalculatorTest {

    @Rule
    public LocaleRule localeRule = new LocaleRule("pt-BR");
    @Test
    public void givenDateEqualsOrBeforeExpectedDate_thenExpectedNormalStatusToTimeline() {
        given()
            .projectWithRisk(0.2)
            .projectWithStartDate("2019-04-10")
            .projectWithDeliveryDate("2019-04-20")
            .budgetChartwithProjectionDate("2019-04-19")

        .whenCalculate()

        .then()
            .assertLabel("Timeline Forecast")
            .assertIcon("#icon-notebook")
            .assertActual("abr19/2019")
            .assertExpected("abr20/2019")
            .assertStatus("normal")
            .assertWarning("abr20/2019 - abr22/2019");
    }

    @Test
    public void givenDateInsideRangeOfTimeline_thenExpectedAlertStatusToTimeline() {
        given()
            .projectWithRisk(0.2)
            .projectWithStartDate("2019-04-10")
            .projectWithDeliveryDate("2019-04-20")
            .budgetChartwithProjectionDate("2019-04-21")

        .whenCalculate()

        .then()
            .assertLabel("Timeline Forecast")
            .assertIcon("#icon-notebook")
            .assertActual("abr21/2019")
            .assertExpected("abr20/2019")
            .assertStatus("alert")
            .assertWarning("abr20/2019 - abr22/2019");
    }

    @Test
    public void givendateAfterLimitDate_thenExpectedDangerStatusToTimeline() {
        given()
            .projectWithRisk(0.2)
            .projectWithStartDate("2019-04-10")
            .projectWithDeliveryDate("2019-04-20")
            .budgetChartwithProjectionDate("2019-04-23")

        .whenCalculate()

        .then()
            .assertLabel("Timeline Forecast")
            .assertIcon("#icon-notebook")
            .assertActual("abr23/2019")
            .assertExpected("abr20/2019")
            .assertStatus("danger")
            .assertWarning("abr20/2019 - abr22/2019");
    }

    @Test
    public void givenProjectWithoutRisk_thenNoResultsToWarning() {
        given()
            .projectWithStartDate("2019-04-10")
            .projectWithDeliveryDate("2019-04-20")
            .budgetChartwithProjectionDate("2019-04-23")

        .whenCalculate()

        .then()
            .assertLabel("Timeline Forecast")
            .assertIcon("#icon-notebook")
            .assertActual("abr23/2019")
            .assertExpected("abr20/2019")
            .assertStatus("danger")
            .assertWarning(CANT_CALCULATE_MESSAGE)
            .assertErrors(CANT_CALCULATE_TIMELINE_WARNING);
    }

    @Test
    public void givenBudgetChartWithoutData_thenThrowUnexpectedErrorException() {
        given()
        .whenCalculate()

        .then()
            .assertLabel("Timeline Forecast")
            .assertIcon("#icon-notebook")
            .assertActual(CANT_CALCULATE_MESSAGE)
            .assertExpected(CANT_CALCULATE_MESSAGE)
            .assertStatus(null)
            .assertWarning(CANT_CALCULATE_MESSAGE)
            .assertErrors(CANT_CALCULATE_TIMELINE_UNEXPECTED);
    }

    @Test
    public void givenProjectDateNotConfigured_thenThrowProjectDatesNotConfiguredException() {
        assertMonitorError(
            given(),
            ProjectDatesNotConfiguredException.fromProject(),
            "Can't calculate Timeline: The project has no start or delivery date.");
    }

    @Test
    public void givenClusterNotConfigured_thenThrowClusterNotConfiguredException() {
        assertMonitorError(
                given(),
                ClusterNotConfiguredException.fromProject(),
                "Can't calculate Timeline: No cluster configuration found.");
    }

    private MonitorCalculatorDSL given() {
        return MonitorCalculatorDSL.asTimeline();
    }

}
