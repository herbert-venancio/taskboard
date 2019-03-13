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
        Assertions.assertThat(subject.issueKey)
            .as("Issue keys must be equals")
            .isEqualTo(expected.issueKey);
        Assertions.assertThat(subject.issueType)
            .as("Issue types must be equals")
            .isEqualTo(expected.issueType);
        Assertions.assertThat(subject.issueStatus)
            .as("Statuses must be equals")
            .isEqualTo(expected.issueStatus);
        Assertions.assertThat(subject.effortInHours)
            .as("Efforts must be equals")
            .isCloseTo(expected.effortInHours, Assertions.within(DELTA));
        Assertions.assertThat(subject.startProgressingDate)
            .as("Start progressing dates must be equals")
            .isEqualTo(expected.startProgressingDate);
        Assertions.assertThat(subject.endProgressingDate)
            .as("Start progressing dates must be equals")
            .isEqualTo(expected.endProgressingDate);
    }
}