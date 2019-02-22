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
        Assertions.assertThat(subject.date).isEqualTo(expected.date);
        Assertions.assertThat(subject.stackName).isEqualTo(expected.stackName);
        Assertions.assertThat(subject.effortInHours).isCloseTo(expected.effortInHours, Assertions.within(0.1));
    }
}