package objective.taskboard.followup.kpi.touchtime.helpers;

import java.util.List;
import java.util.ListIterator;

import org.assertj.core.api.Assertions;

import objective.taskboard.followup.kpi.services.KpiDataPointAsserter;
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
        Assertions.assertThat(subject.stacks)
            .as("Stacks must have same size")
            .hasSize(expected.stacks.size());
        this.assertStacks(subject.stacks, expected.stacks, subject.issueKey);
    }

    private void assertStacks(List<Stack> subjectStacks, List<Stack> expectedStacks, String issueKey) {
        ListIterator<Stack> subjectStacksIterator = subjectStacks.listIterator();
        ListIterator<Stack> expectedStacksIterator = expectedStacks.listIterator();
        while (subjectStacksIterator.hasNext()) {
            int currentIndex = subjectStacksIterator.nextIndex();
            Stack subjectStack = subjectStacksIterator.next();
            Stack expectedStack = expectedStacksIterator.next();
            this.assertStack(subjectStack, expectedStack, issueKey, currentIndex);
        }

    }

    private void assertStack(Stack subject, Stack expected, String issueKey, int stackNumber) {
        Assertions.assertThat(subject.stackName)
            .as("Stack names at position %d of %s must be equals", stackNumber, issueKey)
            .isEqualTo(expected.stackName);
        Assertions.assertThat(subject.effortInHours)
            .as("Efforts at position %d of %s must be equals", stackNumber, issueKey)
            .isCloseTo(expected.effortInHours, Assertions.within(DELTA));
    }
}