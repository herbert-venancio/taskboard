package objective.taskboard.followup.kpi;

import static objective.taskboard.utils.DateTimeUtils.parseStringToDate;

import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import objective.taskboard.data.Worklog;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;

public class SubtaskTouchTimeScenariosTest {

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
                .appliesBehavior(worklogDistributions().withDefaultWorklogs())
            .then()
                .status("To Do").hasTotalEffortInSeconds(0l)
                .status("Doing").hasTotalEffortInSeconds(600l)
                .status("To Review").hasTotalEffortInSeconds(0l)
                .status("Review").hasTotalEffortInSeconds(1500l)
                .status("Done").hasTotalEffortInSeconds(0l);
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
                .appliesBehavior(worklogDistributions().withDefaultWorklogs())
            .then()
                .status("To Do").hasTotalEffortInSeconds(0l)
                .status("Doing").hasTotalEffortInSeconds(1000l)
                .status("To Review").hasTotalEffortInSeconds(0l)
                .status("Review").hasTotalEffortInSeconds(1100l)
                .status("Done").hasTotalEffortInSeconds(0l);
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
            .appliesBehavior(worklogDistributions().withDefaultWorklogs())
        .then()
            .status("To Do").hasTotalEffortInSeconds(0l)
            .status("Doing").hasTotalEffortInSeconds(300l)
            .status("To Review").hasTotalEffortInSeconds(0l)
            .status("Review").hasTotalEffortInSeconds(1800l)
            .status("Done").hasTotalEffortInSeconds(0l);
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
            .appliesBehavior(worklogDistributions().withDefaultWorklogs())
        .then()
            .status("To Do").hasTotalEffortInSeconds(0l)
            .status("Doing").hasTotalEffortInSeconds(2100l)
            .status("To Review").hasTotalEffortInSeconds(0l)
            .status("Review").hasTotalEffortInSeconds(0l)
            .status("Done").hasTotalEffortInSeconds(0l);
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
            .appliesBehavior(worklogDistributions().withDefaultWorklogs())
        .then()
            .status("To Do").hasTotalEffortInSeconds(0l)
            .status("Doing").hasTotalEffortInSeconds(0l)
            .status("To Review").hasTotalEffortInSeconds(0l)
            .status("Review").hasTotalEffortInSeconds(2100l)
            .status("Done").hasTotalEffortInSeconds(0l);
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
                .status("Done").date("2020-01-02")
            .eoSt()
        .when()
            .appliesBehavior(worklogDistributions().withDefaultWorklogs())
        .then()
            .status("To Do").hasTotalEffortInSeconds(0l)
            .status("Doing").hasTotalEffortInSeconds(100l)
            .status("To Review").hasTotalEffortInSeconds(0l)
            .status("Review").hasTotalEffortInSeconds(2000l)
            .status("Done").hasTotalEffortInSeconds(0l);
    }

    @Test
    public void worklogBeforeOpen() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-02")
                .status("Doing").date("2020-01-03")
                .status("To Review").date("2020-01-04")
                .status("Review").date("2020-01-05")
                .status("Done").date("2020-01-06")
            .eoSt()
        .when()
            .appliesBehavior(
                        worklogDistributions()
                            .withWorklogAt("2019-12-31").withValue(300))
        .then()
            .status("To Do").hasTotalEffortInSeconds(0l)
            .status("Doing").hasTotalEffortInSeconds(300l)
            .status("To Review").hasTotalEffortInSeconds(0l)
            .status("Review").hasTotalEffortInSeconds(0l)
            .status("Done").hasTotalEffortInSeconds(0l);
    }

    @Test
    public void allStatusTransitedSameDay() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-02")
                .status("Doing").date("2020-01-02")
                .status("To Review").noDate()
                .status("Review").date("2020-01-02")
                .status("Done").date("2020-01-02")
            .eoSt()
        .when()
            .appliesBehavior(
                        worklogDistributions()
                            .withWorklogAt("2020-01-02").withValue(200))
        .then()
            .status("To Do").hasTotalEffortInSeconds(0l)
            .status("Doing").hasTotalEffortInSeconds(0)
            .status("To Review").hasTotalEffortInSeconds(0l)
            .status("Review").hasTotalEffortInSeconds(200l)
            .status("Done").hasTotalEffortInSeconds(0l);
    }


    @Test
    public void whenWronglyConfigured_doesNotAddWorklog() {
        new DSLKpi()
        .environment()
            .statuses()
                .withNotProgressingStatuses("To Do","Doing","To Review","Review","Done")
            .eoS()
            .statusTransition()
                .status("To Do").date("2020-01-02")
                .status("Doing").date("2020-01-03")
                .status("To Review").date("2020-01-04")
                .status("Review").date("2020-01-05")
                .status("Done").date("2020-01-06")
            .eoSt()
        .when()
            .appliesBehavior(worklogDistributions().withDefaultWorklogs())
        .then()
            .status("To Do").hasTotalEffortInSeconds(0l)
            .status("Doing").hasTotalEffortInSeconds(0)
            .status("To Review").hasTotalEffortInSeconds(0l)
            .status("Review").hasTotalEffortInSeconds(0l)
            .status("Done").hasTotalEffortInSeconds(0l);
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

    private WorklogDistributor worklogDistributions() {
        return new WorklogDistributor();
    }

    private class WorklogDistributor implements DSLSimpleBehaviorWithAsserter<StatusTransitionAsserter>{

        private StatusTransitionAsserter asserter;
        private List<ZonedWorklog> worklogs = new LinkedList<>();
        private SubtaskWorklogDistributor distributor = new SubtaskWorklogDistributor();
        private ZoneId timezone = ZoneId.systemDefault();

        private WorklogDistributor withDefaultWorklogs() {
            List<Worklog> worklogs = new LinkedList<>();
            worklogs.add(new Worklog("a.developer", parseStringToDate("2020-01-01"), 100));
            worklogs.add(new Worklog("a.developer", parseStringToDate("2020-01-02"), 200));
            worklogs.add(new Worklog("a.developer", parseStringToDate("2020-01-03"), 300));
            worklogs.add(new Worklog("a.developer", parseStringToDate("2020-01-04"), 400));
            worklogs.add(new Worklog("a.developer", parseStringToDate("2020-01-05"), 500));
            worklogs.add(new Worklog("a.developer", parseStringToDate("2020-01-06"), 600));
            this.worklogs = worklogs.stream()
                    .map(w -> new ZonedWorklog(w, timezone))
                    .collect(Collectors.toList());
            return this;
        }

        public WorklogBuilder withWorklogAt(String date) {
            return new WorklogBuilder(date);
        }

        @Override
        public void behave(KpiEnvironment environment) {
            Optional<StatusTransition> status = environment.statusTransition().getFirstStatusTransition();
            Assertions.assertThat(status).as("Status not configured").isPresent();
            configureWorklogs(status.get());
            asserter = new StatusTransitionAsserter(timezone, status);
        }

        private void configureWorklogs(StatusTransition status) {
            worklogs.stream().forEach(w -> distributor.findStatus(status, w).ifPresent(s -> s.putWorklog(w)));
        }

        @Override
        public StatusTransitionAsserter then() {
            return asserter;
        }

        private class WorklogBuilder {
            private String date;

            public WorklogBuilder(String date) {
                this.date = date;
            }

            public WorklogDistributor withValue(int value) {
                worklogs.add(new ZonedWorklog(new Worklog("an.author", parseStringToDate(date), value), timezone));
                return WorklogDistributor.this;
            }
        }

    }
}
