package objective.taskboard.followup.kpi.touchtime.helpers;

import org.assertj.core.api.Assertions;

import objective.taskboard.followup.kpi.enviroment.KpiDataPointAsserter;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByWeekKpiDataPoint;

public class TTByWeekKpiDataPointAsserter implements KpiDataPointAsserter<TouchTimeByWeekKpiDataPoint> {

    private TouchTimeByWeekKpiDataPoint expected;

    TTByWeekKpiDataPointAsserter(TouchTimeByWeekKpiDataPoint expected) {
        this.expected = expected;
    }

    @Override
    public void doAssert(TouchTimeByWeekKpiDataPoint subject) {
        Assertions.assertThat(subject.date).as("Dates must be equals").isEqualTo(expected.date);
        Assertions.assertThat(subject.stackName)
            .as("Stacks names from week %s must be equals", subject.date.toString())
            .isEqualTo(expected.stackName);
        Assertions.assertThat(subject.effortInHours)
            .as("Efforts from week %s must be equals", subject.date.toString())
            .isCloseTo(expected.effortInHours, Assertions.within(DELTA));
    }
}