package objective.taskboard.followup.kpi.touchtime.helpers;

import org.assertj.core.api.Assertions;

import objective.taskboard.followup.kpi.enviroment.KpiDataPointAsserter;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiDataPoint;

class TTByIssueKpiDataPointAsserter implements KpiDataPointAsserter<TouchTimeByIssueKpiDataPoint> {

    private TouchTimeByIssueKpiDataPoint expected;

    public TTByIssueKpiDataPointAsserter(TouchTimeByIssueKpiDataPoint expected) {
        this.expected = expected;
    }

    @Override
    public void doAssert(TouchTimeByIssueKpiDataPoint subject) {
        Assertions.assertThat(subject.issueKey).isEqualTo(expected.issueKey);
        Assertions.assertThat(subject.issueType).isEqualTo(expected.issueType);
        Assertions.assertThat(subject.issueStatus).isEqualTo(expected.issueStatus);
        Assertions.assertThat(subject.effortInHours).isCloseTo(expected.effortInHours, Assertions.within(DELTA));
        Assertions.assertThat(subject.startProgressingDate).isEqualTo(expected.startProgressingDate);
        Assertions.assertThat(subject.endProgressingDate).isEqualTo(expected.endProgressingDate);
    }
}