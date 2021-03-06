package objective.taskboard.followup.kpi;

import static objective.taskboard.utils.DateTimeUtils.parseStringToDate;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import objective.taskboard.data.Worklog;
import objective.taskboard.followup.kpi.services.DSLKpi;
import objective.taskboard.followup.kpi.services.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.services.KpiEnvironment;

public class SubtaskWorklogDistributorTest {

    @Test
    public void happyDay() {
        dsl()
            .environment()
                .statusTransition()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("To Review").date("2020-01-03")
                    .status("Review").date("2020-01-04")
                    .status("Done").date("2020-01-05")
                .eoSt()
            .when()
                .appliesBehavior(prepareStatusTransition())
            .then()
                .worklogAt("2020-01-01").isDistributedToStatus("Doing")
                .worklogAt("2020-01-02").isDistributedToStatus("Doing")
                .worklogAt("2020-01-03").isDistributedToStatus("Doing")
                .worklogAt("2020-01-04").isDistributedToStatus("Review")
                .worklogAt("2020-01-05").isDistributedToStatus("Review")
                .worklogAt("2020-01-06").isDistributedToStatus("Review");
    }

    @Test
    public void straightToClose() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").noDate()
                .status("To Review").noDate()
                .status("Review").noDate()
                .status("Done").date("2020-01-05")
            .eoSt()
        .when()
            .appliesBehavior(prepareStatusTransition())
        .then()
            .worklogAt("2020-01-01").isDistributedToStatus("Doing")
            .worklogAt("2020-01-02").isDistributedToStatus("Doing")
            .worklogAt("2020-01-03").isDistributedToStatus("Doing")
            .worklogAt("2020-01-04").isDistributedToStatus("Doing")
            .worklogAt("2020-01-05").isDistributedToStatus("Review")
            .worklogAt("2020-01-06").isDistributedToStatus("Review");
}

    @Test
    public void skippingQueue() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-02")
                .status("To Review").noDate()
                .status("Review").date("2020-01-03")
                .status("Done").date("2020-01-05")
            .eoSt()
        .when()
            .appliesBehavior(prepareStatusTransition())
        .then()
            .worklogAt("2020-01-01").isDistributedToStatus("Doing")
            .worklogAt("2020-01-02").isDistributedToStatus("Doing")
            .worklogAt("2020-01-03").isDistributedToStatus("Review")
            .worklogAt("2020-01-04").isDistributedToStatus("Review")
            .worklogAt("2020-01-05").isDistributedToStatus("Review")
            .worklogAt("2020-01-06").isDistributedToStatus("Review");
}

    @Test
    public void openIssue() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").noDate()
                .status("To Review").noDate()
                .status("Review").noDate()
                .status("Done").noDate()
            .eoSt()
        .when()
            .appliesBehavior(prepareStatusTransition())
        .then()
            .worklogAt("2020-01-01").isDistributedToStatus("Doing")
            .worklogAt("2020-01-02").isDistributedToStatus("Doing")
            .worklogAt("2020-01-03").isDistributedToStatus("Doing")
            .worklogAt("2020-01-04").isDistributedToStatus("Doing")
            .worklogAt("2020-01-05").isDistributedToStatus("Doing")
            .worklogAt("2020-01-06").isDistributedToStatus("Doing");
    }

    @Test
    public void straightToReview() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-01")
                .status("To Review").noDate()
                .status("Review").date("2020-01-01")
                .status("Done").noDate()
            .eoSt()
        .when()
            .appliesBehavior(prepareStatusTransition())
        .then()
            .worklogAt("2020-01-01").isDistributedToStatus("Review")
            .worklogAt("2020-01-02").isDistributedToStatus("Review")
            .worklogAt("2020-01-03").isDistributedToStatus("Review")
            .worklogAt("2020-01-04").isDistributedToStatus("Review")
            .worklogAt("2020-01-05").isDistributedToStatus("Review")
            .worklogAt("2020-01-06").isDistributedToStatus("Review");
    }

    @Test
    public void issueClosed_doingAndReviewAtTheSameDay() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-02")
                .status("To Review").noDate()
                .status("Review").date("2020-01-02")
                .status("Done").date("2020-01-03")
            .eoSt()
        .when()
            .appliesBehavior(prepareStatusTransition())
        .then()
            .worklogAt("2020-01-01").isDistributedToStatus("Doing")
            .worklogAt("2020-01-02").isDistributedToStatus("Review")
            .worklogAt("2020-01-03").isDistributedToStatus("Review")
            .worklogAt("2020-01-04").isDistributedToStatus("Review")
            .worklogAt("2020-01-05").isDistributedToStatus("Review")
            .worklogAt("2020-01-06").isDistributedToStatus("Review");
    }

    @Test
    public void jumpingProgressingStatuses() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").noDate()
                .status("To Review").date("2020-01-03")
                .status("Review").noDate()
                .status("Done").date("2020-01-06")
            .eoSt()
        .when()
            .appliesBehavior(prepareStatusTransition())
        .then()
            .worklogAt("2020-01-01").isDistributedToStatus("Doing")
            .worklogAt("2020-01-02").isDistributedToStatus("Doing")
            .worklogAt("2020-01-03").isDistributedToStatus("Doing")
            .worklogAt("2020-01-04").isDistributedToStatus("Review")
            .worklogAt("2020-01-05").isDistributedToStatus("Review")
            .worklogAt("2020-01-06").isDistributedToStatus("Review");
    }
    @Test
    public void passingThroughProgressing_stoppingOnQueue() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-03")
                .status("To Review").date("2020-01-03")
                .status("Review").date("2020-01-04")
                .status("Done").date("2020-01-04")
            .eoSt()
        .when()
            .appliesBehavior(prepareStatusTransition())
        .then()
            .worklogAt("2020-01-01").isDistributedToStatus("Doing")
            .worklogAt("2020-01-02").isDistributedToStatus("Doing")
            .worklogAt("2020-01-03").isDistributedToStatus("Doing")
            .worklogAt("2020-01-04").isDistributedToStatus("Review")
            .worklogAt("2020-01-05").isDistributedToStatus("Review")
            .worklogAt("2020-01-06").isDistributedToStatus("Review");
    }

    @Test
    public void issueClosedSameDayItWasDoing_worklogShouldGoToDoing() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-04")
                .status("To Review").noDate()
                .status("Review").noDate()
                .status("Done").date("2020-01-04")
            .eoSt()
        .when()
            .appliesBehavior(prepareStatusTransition())
        .then()
            .worklogAt("2020-01-01").isDistributedToStatus("Doing")
            .worklogAt("2020-01-02").isDistributedToStatus("Doing")
            .worklogAt("2020-01-03").isDistributedToStatus("Doing")
            .worklogAt("2020-01-04").isDistributedToStatus("Doing")
            .worklogAt("2020-01-05").isDistributedToStatus("Review")
            .worklogAt("2020-01-06").isDistributedToStatus("Review");
    }

    @Test
    public void givenAllTransitionsSameDay_whenWorklogsOnTransitionDay_thenShouldEnterOnLastProgressingStatus() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-03")
                .status("Doing").date("2020-01-03")
                .status("To Review").date("2020-01-03")
                .status("Review").date("2020-01-03")
                .status("Done").date("2020-01-03")
            .eoSt()
        .when()
            .appliesBehavior(prepareStatusTransition())
        .then()
            .worklogAt("2020-01-01").isDistributedToStatus("Doing")
            .worklogAt("2020-01-02").isDistributedToStatus("Doing")
            .worklogAt("2020-01-03").isDistributedToStatus("Review")
            .worklogAt("2020-01-04").isDistributedToStatus("Review")
            .worklogAt("2020-01-05").isDistributedToStatus("Review")
            .worklogAt("2020-01-06").isDistributedToStatus("Review");

    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .statuses()
                .withNotProgressingStatuses("To Do","To Review","Done")
                .withProgressingStatuses("Doing","Review")
            .eoS();
        return dsl;
    }

    private DSLSimpleBehaviorWithAsserter<SubtaskWorklogDistributorAsserter> prepareStatusTransition() {

        return new DSLSimpleBehaviorWithAsserter<SubtaskWorklogDistributorAsserter>() {

            private SubtaskWorklogDistributorAsserter asserter;
            @Override
            public void behave(KpiEnvironment environment) {
                Optional<StatusTransition> opStatus = environment.statusTransition().getFirstStatusTransition();
                Assertions.assertThat(opStatus).as("Status misconfigured").isPresent();
                asserter = new SubtaskWorklogDistributorAsserter(opStatus.get(), environment.getTimezone());
            }

            @Override
            public SubtaskWorklogDistributorAsserter then() {
                return asserter;
            }
        };
    }

    private class SubtaskWorklogDistributorAsserter {

        private SubtaskWorklogDistributor subject = new SubtaskWorklogDistributor();
        private StatusTransition status;
        private ZoneId timezone;

        public SubtaskWorklogDistributorAsserter(StatusTransition status, ZoneId timezone) {
            this.status = status;
            this.timezone = timezone;
        }

        public DateAsserter worklogAt(String date) {
            return new DateAsserter(date);
        }

        private class DateAsserter {
            String date;

            private DateAsserter(String date) {
                this.date = date;
            }

            private SubtaskWorklogDistributorAsserter isDistributedToStatus(String expectedStatus) {
                Worklog worklog = new Worklog("a.developer",parseStringToDate(date),100);
                ZonedWorklog zonedWorklog = new ZonedWorklog(worklog, timezone);
                Optional<StatusTransition> statusFound = subject.findStatus(status, zonedWorklog);

                assertThat(statusFound).isPresent();
                assertThat(statusFound).hasValueSatisfying( s -> {
                    assertThat(s.status).isEqualTo(expectedStatus);
                });
                return SubtaskWorklogDistributorAsserter.this;
            }

        }

    }

}
