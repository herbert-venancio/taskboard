package objective.taskboard.followup.kpi.touchtime.helpers;

import java.util.Iterator;
import java.util.List;

import org.assertj.core.api.Assertions;

import objective.taskboard.followup.kpi.enviroment.KpiDataPointAsserter;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiDataPoint;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiDataPoint.Stack;

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
        Assertions.assertThat(subject.startProgressingDate)
            .as("Start progressing dates must be equals")
            .isEqualTo(expected.startProgressingDate);
        Assertions.assertThat(subject.endProgressingDate)
            .as("Start progressing dates must be equals")
            .isEqualTo(expected.endProgressingDate);
        Assertions.assertThat(subject.stacks).hasSize(expected.stacks.size());
        this.assertStacks(subject.stacks, expected.stacks);
    }

    private void assertStacks(List<Stack> subjectStacks, List<Stack> expectedStacks) {
        Iterator<Stack> subjectStacksIterator = subjectStacks.iterator();
        Iterator<Stack> expectedStacksIterator = expectedStacks.iterator();
        while (subjectStacksIterator.hasNext()) {
            Stack subjectStack = subjectStacksIterator.next();
            Stack expectedStack = expectedStacksIterator.next();
            this.assertStack(subjectStack, expectedStack);
        }

    }

    private void assertStack(Stack subject, Stack expected) {
        Assertions.assertThat(subject.stackName).isEqualTo(expected.stackName);
        Assertions.assertThat(subject.effortInHours).isCloseTo(expected.effortInHours, Assertions.within(DELTA));
    }
}