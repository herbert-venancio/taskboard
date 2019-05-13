package objective.taskboard.monitor;

import static objective.taskboard.monitor.MonitorCalculator.CANT_CALCULATE_MESSAGE;
import static objective.taskboard.monitor.TimelineMonitorCalculator.CANT_CALCULATE_TIMELINE_UNEXPECTED;
import static objective.taskboard.monitor.TimelineMonitorCalculator.CANT_CALCULATE_TIMELINE_WARNING;

import org.junit.Rule;
import org.junit.Test;

import objective.taskboard.rules.LocaleRule;

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

    private MonitorCalculatorDSL given() {
        return MonitorCalculatorDSL.forTimeline();
    }

}
