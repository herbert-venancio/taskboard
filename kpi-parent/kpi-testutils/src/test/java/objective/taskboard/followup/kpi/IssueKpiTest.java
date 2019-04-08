package objective.taskboard.followup.kpi;

import org.junit.Test;

import objective.taskboard.followup.kpi.services.DSLKpi;

public class IssueKpiTest {
    @Test
    public void checkStatusOnDay_happyDay() {
        dsl()
        .environment()
            .givenSubtask("I-1")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("Done").date("2020-01-03")
                .eoT()
            .eoI()
        .then()
        .assertThat()
            .issueKpi("I-1")
            .atDate("2020-01-01")
                .isOnStatus("To Do")
                .isNotOnStatus("Doing")
                .isNotOnStatus("Done").eoDc()
            .atDate("2020-01-02")
                .isNotOnStatus("To Do")
                .isOnStatus("Doing")
                .isNotOnStatus("Done").eoDc()
            .atDate("2020-01-03")
                .isNotOnStatus("To Do")
                .isNotOnStatus("Doing")
                .isOnStatus("Done").eoDc()
            .atDate("2020-01-01")
                .isNotOnStatus("Review").eoDc()
            .atDate("2020-01-04")
                .isNotOnStatus("Doing").eoDc();
    }

    @Test
    public void checkStatusOnDay_emptyTransitions() {
        dsl()
        .environment()
            .givenSubtask("I-1")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").noDate()
                    .status("Done").date("2020-01-03")
                .eoT()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("I-1")
                .atDate("2020-01-01")
                    .isOnStatus("To Do")
                    .isNotOnStatus("Doing")
                    .isNotOnStatus("Done").eoDc()
                .atDate("2020-01-02")
                    .isOnStatus("To Do")
                    .isNotOnStatus("Doing")
                    .isNotOnStatus("Done").eoDc()
                .atDate("2020-01-03")
                    .isNotOnStatus("To Do")
                    .isNotOnStatus("Doing")
                    .isOnStatus("Done").eoDc()
                .atDate("2020-01-04")
                    .isNotOnStatus("To Do")
                    .isNotOnStatus("Doing")
                    .isOnStatus("Done").eoDc();
    }

    @Test
    public void checkStatusOnDay_openIssue() {
        dsl()
        .environment()
            .givenSubtask("I-1")
                .type("Subtask")
                .project("PROJ")
            .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .eoT()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("I-1")
                .atDate("2020-01-01")
                    .isOnStatus("To Do")
                    .isNotOnStatus("Doing").eoDc()
                .atDate("2020-01-02")
                    .isOnStatus("To Do")
                    .isNotOnStatus("Doing").eoDc();
    }

    @Test
    public void checkStatusOnDay_futureIssue() {
        dsl()
        .environment()
            .givenSubtask("I-1")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-02")
                    .status("Doing").date("2020-01-03")
                    .status("Done").date("2020-01-04")
                .eoT()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("I-1")
                .atDate("2020-01-01")
                    .isNotOnStatus("To Do")
                    .isNotOnStatus("Doing")
                    .isNotOnStatus("Done").eoDc()
                .atDate("2020-01-02")
                    .isOnStatus("To Do")
                    .isNotOnStatus("Doing")
                    .isNotOnStatus("Done").eoDc();
    }

    @Test
    public void hasTransitedToStatus_happyDay() {
        dsl()
        .environment()
            .givenSubtask("I-1")
                .project("PROJ")
                .type("Subtask")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("Done").date("2020-01-03")
                .eoT()
            .eoI()
        .then()
        .assertThat()
            .issueKpi("I-1")
            .atDate("2020-01-01")
                .hasNotTransitedToAnyStatus("Done").eoDc()
            .atDate("2020-01-02")
                .hasNotTransitedToAnyStatus("Done").eoDc()
            .atDate("2020-01-03")
                .hasTransitedToAnyStatus("Done").eoDc();
    }

    @Test
    public void hasTransitedToStatus_nonExistentStatus() {
        dsl()
        .environment()
            .givenSubtask("I-1")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("Done").date("2020-01-03")
                .eoT()
            .eoI()
        .then()
        .assertThat()
            .issueKpi("I-1")
            .atDate("2020-01-01")
                .hasNotTransitedToAnyStatus("Integrating","Cancelled").eoDc()
            .atDate("2020-01-02")
                .hasNotTransitedToAnyStatus("Integrating","Cancelled").eoDc()
            .atDate("2020-01-03")
                .hasNotTransitedToAnyStatus("Integrating","Cancelled").eoDc();
    }

    @Test
    public void hasTransitedToStatus_onlyOneStatusTransited() {
        dsl()
        .environment()
            .givenSubtask("I-1")
                .project("PROJ")
                .type("Subtask")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").noDate()
                    .status("Done").date("2020-01-03")
                .eoT()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("I-1")
                .atDate("2020-01-01")
                    .hasNotTransitedToAnyStatus("Doing","Done").eoDc()
                .atDate("2020-01-02")
                    .hasNotTransitedToAnyStatus("Doing","Done").eoDc()
                .atDate("2020-01-03")
                    .hasTransitedToAnyStatus("Doing","Done").eoDc();
    }

    @Test
    public void hasTransitedToStatus_onlyNotTransited() {
        dsl()
        .environment()
            .givenSubtask("I-1")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").noDate()
                    .status("Done").date("2020-01-03")
                .eoT()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("I-1")
            .atDate("2020-01-01")
                .hasNotTransitedToAnyStatus("Doing").eoDc()
            .atDate("2020-01-02")
                .hasNotTransitedToAnyStatus("Doing").eoDc()
            .atDate("2020-01-03")
                .hasNotTransitedToAnyStatus("Doing").eoDc();
    }

    @Test
    public void hasTransitedToStatus_earliestTransition() {
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .type("Subtask")
                    .project("PROJ")
                    .withTransitions()
                        .status("To Do").date("2020-01-01")
                        .status("Doing").date("2020-01-02")
                        .status("Done").date("2020-01-03")
                    .eoT()
                .eoI()
            .then()
                .assertThat()
                    .issueKpi("I-1")
                    .atDate("2020-01-01")
                        .hasNotTransitedToAnyStatus("Doing","Done").eoDc()
                    .atDate("2020-01-02")
                        .hasTransitedToAnyStatus("Doing","Done").eoDc()
                    .atDate("2020-01-03")
                        .hasNotTransitedToAnyStatus("Doing","Done").eoDc();
    }

    @Test
    public void getWorklogFromChildren() {
        dsl()
        .environment()
            .givenFeature("PROJ-01")
                .type("Feature")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("Done").date("2020-01-03")
                .eoT()
            .subtask("PROJ-02")
                .type("Subtask")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("Done").date("2020-01-03")
                .eoT()
                .worklogs()
                    .at("2020-01-01").timeSpentInSeconds(300)
                .eoW()
            .endOfSubtask()
        .eoI()
        .then()
        .assertThat()
            .issueKpi("PROJ-01")
                .givenSubtaskType("Subtask")
                    .hasTotalWorklogs(1).withTotalValue(300).eoSc()
                .withChild("PROJ-02")
                    .atStatus("Doing").hasTotalEffort(300l);
    }

    @Test
    public void wrongConfiguration_dontGetWorklogFromChildren() {
        dsl()
        .environment()
            .givenFeature("PROJ-01")
                .type("Feature")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("Done").date("2020-01-03")
                .eoT()
            .subtask("PROJ-02")
                .emptyType()
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("Done").date("2020-01-03")
                .eoT()
                .worklogs()
                    .at("2020-01-01").timeSpentInSeconds(300)
                .eoW()
            .endOfSubtask()
        .eoI()
        .then()
        .assertThat()
            .issueKpi("PROJ-01")
            .givenSubtaskType("Subtask")
                .doesNotHaveWorklogs().eoSc()
            .withChild("PROJ-02")
                .atStatus("Doing").hasTotalEffort(300l);
    }

    @Test
    public void getWorklogFromChildrenStatus_happyDay() {
        dsl().
        environment()
            .givenFeature("PROJ-01")
                .type("Feature")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("Done").date("2020-01-03")
                    .eoT()
                .subtask("PROJ-02")
                    .type("Subtask")
                    .withTransitions()
                        .status("To Do").date("2020-01-01")
                        .status("Doing").date("2020-01-02")
                        .status("Done").date("2020-01-03")
                    .eoT()
                    .worklogs()
                        .at("2020-01-01").timeSpentInSeconds(300)
                    .eoW()
                .endOfSubtask()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .givenSubtaskStatus("To Do")
                    .doesNotHaveWorklogs().eoSc()
                .givenSubtaskStatus("Doing")
                    .hasTotalWorklogs(1)
                    .withTotalValue(300).eoSc()
                .withChild("PROJ-02")
                    .atStatus("Doing")
                        .hasTotalEffort(300l);
    }

    @Test
    public void wrongConfiguration_dontGetWorklogFromChildrenStatus() {
        dsl().
        environment()
            .givenFeature("PROJ-01")
                .type("Feature")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("Done").date("2020-01-03")
                .eoT()
                .subtask("PROJ-02")
                    .type("Subtask")
                    .withTransitions()
                        .status("To Do").date("2020-01-01")
                        .status("Doing").date("2020-01-02")
                        .status("Done").date("2020-01-03")
                    .eoT()
                    .worklogs()
                        .at("2020-01-01").timeSpentInSeconds(300)
                    .eoW()
                .endOfSubtask()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .givenSubtaskStatus("To Do")
                    .doesNotHaveWorklogs().eoSc()
                .givenSubtaskStatus("Inexistent")
                    .doesNotHaveWorklogs().eoSc();
    }

    @Test
    public void getRangeByProgressingStatuses_happyDay() {
        dsl().
        environment()
            .todayIs("2020-01-06")
            .givenFeature("PROJ-01")
                .type("Feature")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("To Review").date("2020-01-03")
                    .status("Reviewing").date("2020-01-04")
                    .status("Done").date("2020-01-05")
                .eoT()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .rangeBasedOnProgressingStatuses()
                        .startsOn("2020-01-02").endsOn("2020-01-05");
    }


    @Test
    public void getRangeByProgressingStatuses_onlyOneProgressingWithDate() {
        dsl().
        environment()
            .todayIs("2020-01-06")
            .givenFeature("PROJ-01")
                .type("Feature")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").date("2020-01-06")
                .eoT()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .rangeBasedOnProgressingStatuses()
                        .startsOn("2020-01-02").endsOn("2020-01-06");
    }


    @Test
    public void getRangeByProgressingStatuses_doingIssue() {
        dsl().
        environment()
            .todayIs("2020-01-03")
            .givenFeature("PROJ-01")
                .type("Feature")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").noDate()
                .eoT()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .rangeBasedOnProgressingStatuses()
                        .startsOn("2020-01-02").endsOn("2020-01-03");
    }


    @Test
    public void getRangeByProgressingStatuses_straightToReview_workingOnDoing() {

        dsl().
        environment()
            .todayIs("2020-01-10")
            .givenSubtask("PROJ-01")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").noDate()
                    .status("To Review").noDate()
                    .status("Reviewing").date("2020-01-06")
                    .status("Done").date("2020-01-08")
                .eoT()
                .worklogs()
                    .at("2020-01-01").timeSpentInSeconds(300)
                .eoW()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .rangeBasedOnProgressingStatuses()
                        .startsOn("2020-01-01").endsOn("2020-01-08")
                .atStatus("Doing").hasTotalEffort(300l);
    }

    @Test
    public void getRangeByProgressingStatuses_openIssue_workingOnReview() {
        dsl().
        environment()
            .todayIs("2020-01-10")
            .givenSubtask("PROJ-01")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").noDate()
                    .status("To Review").noDate()
                    .status("Reviewing").date("2020-01-06")
                    .status("Done").noDate()
                .eoT()
                .worklogs()
                    .at("2020-01-06").timeSpentInSeconds(300)
                .eoW()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .rangeBasedOnProgressingStatuses()
                        .startsOn("2020-01-06").endsOn("2020-01-10")
                .atStatus("Doing").doesNotHaveEffort()
                .atStatus("Reviewing").hasTotalEffort(300l);
    }

    @Test
    public void getRangeByProgressingStatuses_straightToDone_workingOnDoing() {
        dsl().
        environment()
            .todayIs("2020-01-10")
            .givenSubtask("PROJ-01")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").noDate()
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").date("2020-01-06")
                .eoT()
                .worklogs()
                    .at("2020-01-03").timeSpentInSeconds(300)
                .eoW()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .rangeBasedOnProgressingStatuses()
                        .startsOn("2020-01-03").endsOn("2020-01-06")
                .atStatus("Doing").hasTotalEffort(300l);
    }

    @Test
    public void getRangeByProgressingStatuses_straightToDone_workingOnReview() {
        dsl().
        environment()
            .todayIs("2020-01-10")
            .givenSubtask("PROJ-01")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").noDate()
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").date("2020-01-06")
                .eoT()
                .worklogs()
                    .at("2020-01-06").timeSpentInSeconds(300)
                .eoW()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .rangeBasedOnProgressingStatuses()
                        .startsOn("2020-01-06").endsOn("2020-01-06")
                .atStatus("Reviewing").hasTotalEffort(300l);
    }

    @Test
    public void getRangeByProgressingStatuses_datesOnlyOnNoProgressingStatuses_worklogDistributed() {
        dsl().
        environment()
            .todayIs("2020-01-10")
            .givenSubtask("PROJ-01")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").noDate()
                    .status("To Review").date("2020-01-03")
                    .status("Reviewing").noDate()
                    .status("Done").date("2020-01-06")
                .eoT()
                .worklogs()
                    .at("2020-01-02").timeSpentInSeconds(100)
                    .at("2020-01-03").timeSpentInSeconds(200)
                    .at("2020-01-04").timeSpentInSeconds(300)
                    .at("2020-01-05").timeSpentInSeconds(400)
                .eoW()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .rangeBasedOnProgressingStatuses()
                        .startsOn("2020-01-02").endsOn("2020-01-06")
                .atStatus("Doing").hasTotalEffort(300l).eoSa()
                .atStatus("Reviewing").hasTotalEffort(700l);
    }

    @Test
    public void getRangeByProgressingStatuses_openIssue() {
        dsl().
        environment()
            .todayIs("2020-01-10")
            .givenFeature("PROJ-01")
                .type("Feature")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").noDate()
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").noDate()
                .eoT()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .rangeBasedOnProgressingStatuses().isNotPresent();
    }


    @Test
    public void getRangeByProgressingStatuses_closedIssue_WithoutWorking() {
        dsl().
        environment()
            .todayIs("2020-01-10")
            .givenFeature("PROJ-01")
                .type("Feature")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").noDate()
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").date("2020-01-01")
                .eoT()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .rangeBasedOnProgressingStatuses().isNotPresent();
    }

    @Test
    public void getRangeByProgressingStatuses_straightToDone_worklogOnReviewAfterDone() {
        dsl().
        environment()
            .todayIs("2020-01-10")
            .givenSubtask("PROJ-01")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").noDate()
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").date("2020-01-06")
                .eoT()
                .worklogs()
                    .at("2020-01-07").timeSpentInSeconds(100)
                .eoW()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .rangeBasedOnProgressingStatuses()
                    .startsOn("2020-01-07").endsOn("2020-01-07")
                .atStatus("Doing").doesNotHaveEffort()
                .atStatus("Reviewing").hasTotalEffort(100l);
    }
    
    @Test
    public void getRangeByProgressingStatuses_issueClosedNormally_withWorklogOnReviewAfterDone() {
        dsl().
        environment()
            .todayIs("2020-01-10")
            .givenSubtask("PROJ-01")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("To Review").date("2020-01-03")
                    .status("Reviewing").date("2020-01-04")
                    .status("Done").date("2020-01-06")
                .eoT()
                .worklogs()
                    .at("2020-01-07").timeSpentInSeconds(100)
                .eoW()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                .rangeBasedOnProgressingStatuses()
                    .startsOn("2020-01-02").endsOn("2020-01-07")
                .atStatus("Doing").doesNotHaveEffort()
                .atStatus("Reviewing").hasTotalEffort(100l);
    }

    @Test
    public void getAllWorklog_untilDate() {

        dsl().
        environment()
            .todayIs("2020-01-10")
            .givenSubtask("PROJ-01")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("To Review").date("2020-01-04")
                    .status("Reviewing").date("2020-01-04")
                    .status("Done").date("2020-01-05")
                .eoT()
                .worklogs()
                    .at("2020-01-02").timeSpentInSeconds(200)
                    .at("2020-01-03").timeSpentInSeconds(300)
                    .at("2020-01-04").timeSpentInSeconds(700)
                .eoW()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                    .atStatus("Doing")
                        .hasTotalEffort(500L)
                        .untilDate("2020-01-02").hasEffort(200L)
                        .untilDate("2020-01-03").hasEffort(500L)
                        .untilDate("2020-01-04").hasEffort(500L)
                        .untilDate("2020-01-05").hasEffort(500L)
                    .eoSa()
                    .atStatus("Reviewing")
                        .hasTotalEffort(700L)
                        .untilDate("2020-01-02").hasEffort(0L)
                        .untilDate("2020-01-03").hasEffort(0L)
                        .untilDate("2020-01-04").hasEffort(700L)
                        .untilDate("2020-01-05").hasEffort(700L)
                    .eoSa()
                    .atDate("2020-01-02").hasEffort(200L).eoDc()
                    .atDate("2020-01-03").hasEffort(500L).eoDc()
                    .atDate("2020-01-04").hasEffort(1200L).eoDc()
                    .atDate("2020-01-05").hasEffort(1200L);

    }

    @Test
    public void getEffortSumFromStatusesUntilDate_happyPath() {
        dsl()
        .environment()
            .todayIs("2020-01-10")
            .givenSubtask("I-1")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("To Review").date("2020-01-04")
                    .status("Reviewing").date("2020-01-04")
                    .status("Done").date("2020-01-05")
                .eoT()
                .worklogs()
                    .at("2020-01-02").timeSpentInSeconds(200)
                    .at("2020-01-03").timeSpentInSeconds(300)
                    .at("2020-01-04").timeSpentInSeconds(700)
                .eoW()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("I-1")
                    .atDate("2020-01-10").forStatuses("Doing", "Reviewing").hasEffortSumInSeconds(1200L);
    }

    @Test
    public void getEffortSumFromStatusesUntilDate_whenUsingOnlyOneStatus_thenHappyPath() {
        dsl()
        .environment()
            .todayIs("2020-01-10")
            .givenSubtask("I-1")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("To Review").date("2020-01-04")
                    .status("Reviewing").date("2020-01-04")
                    .status("Done").date("2020-01-05")
                .eoT()
                .worklogs()
                    .at("2020-01-02").timeSpentInSeconds(200)
                    .at("2020-01-03").timeSpentInSeconds(300)
                    .at("2020-01-04").timeSpentInSeconds(700)
                .eoW()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("I-1")
                    .atDate("2020-01-10").forStatuses("Doing").hasEffortSumInSeconds(500L);
    }

    @Test
    public void getEffortSumFromStatusesUntilDate_whenNoStatuses_thenSumIsZero() {
        dsl()
        .environment()
            .todayIs("2020-01-10")
            .givenFeature("I-1")
                .type("Task")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("To Review").date("2020-01-04")
                    .status("Reviewing").date("2020-01-04")
                    .status("Done").date("2020-01-05")
                .eoT()
                .worklogs()
                    .at("2020-01-02").timeSpentInSeconds(200)
                    .at("2020-01-03").timeSpentInSeconds(300)
                    .at("2020-01-04").timeSpentInSeconds(700)
                .eoW()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("I-1")
                    .atDate("2020-01-10").forStatuses().hasEffortSumInSeconds(0L);
    }

    @Test
    public void getEffortSumFromStatusesUntilDate_whenInvalidStatuses_thenSumIsZero() {
        dsl()
        .environment()
            .todayIs("2020-01-10")
            .givenSubtask("I-1")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("To Review").date("2020-01-04")
                    .status("Reviewing").date("2020-01-04")
                    .status("Done").date("2020-01-05")
                .eoT()
                .worklogs()
                    .at("2020-01-02").timeSpentInSeconds(200)
                    .at("2020-01-03").timeSpentInSeconds(300)
                    .at("2020-01-04").timeSpentInSeconds(700)
                .eoW()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("I-1")
                    .atDate("2020-01-10").forStatuses("Foo", "Bar").hasEffortSumInSeconds(0L);
    }

    @Test
    public void hasCompletedCycle_happyDay() {
        dsl()
           .environment()
               .givenSubtask("PROJ-01")
                   .type("Subtask")
                   .project("PROJ")
                   .withTransitions()
                       .status("Open").date("2020-01-01")
                       .status("To Do").date("2020-01-02")
                       .status("Doing").date("2020-01-03")
                       .status("To Review").date("2020-01-04")
                       .status("Reviewing").date("2020-01-05")
                       .status("Done").date("2020-01-06")
                   .eoT()
               .eoI()
           .then()
               .assertThat()
                   .issueKpi("PROJ-01")
                       .hasCompletedCycle("Doing","To Review","Review");
    }

    @Test
    public void hasNotCompletedCycle() {
        dsl()
           .environment()
               .givenSubtask("PROJ-01")
                   .type("Subtask")
                   .project("PROJ")
                   .withTransitions()
                       .status("Open").date("2020-01-01")
                       .status("To Do").date("2020-01-02")
                       .status("Doing").date("2020-01-03")
                       .status("To Review").noDate()
                       .status("Reviewing").noDate()
                       .status("Done").noDate()
                   .eoT()
               .eoI()
           .then()
               .assertThat()
                   .issueKpi("PROJ-01")
                       .hasNotCompletedCycle("Doing","To Review","Review");
    }

    @Test
    public void issueNotEnteringCycle_hasNotCompletedCycle() {
        dsl()
           .environment()
               .givenSubtask("PROJ-01")
                   .type("Subtask")
                   .project("PROJ")
                   .withTransitions()
                       .status("Open").date("2020-01-01")
                       .status("To Do").date("2020-01-02")
                       .status("Doing").noDate()
                       .status("To Review").noDate()
                       .status("Reviewing").noDate()
                       .status("Done").noDate()
                   .eoT()
               .eoI()
           .then()
               .assertThat()
                   .issueKpi("PROJ-01")
                       .hasNotCompletedCycle("Doing","To Review","Review");
    }

    @Test
    public void getIssueTypeName_whenTypeExists_thenHappyPath() {
        dsl()
        .environment()
            .givenSubtask("PROJ-01")
                .type("Subtask")
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").noDate()
                    .status("Doing").noDate()
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").noDate()
                .eoT()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                    .hasType("Subtask");
    }

    @Test
    public void getIssueTypeName_whenTypeNotConfigured_thenReturnUnmappedType() {
        dsl()
        .environment()
            .givenSubtask("PROJ-01")
                .emptyType()
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").noDate()
                    .status("Doing").noDate()
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").noDate()
                .eoT()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                    .hasType("Unmapped");
    }

    @Test
    public void getEffortSumFromChildrenWithSubtaskTypeId_happyPath() {
        dsl()
        .environment()
            .givenDemand("PROJ-01")
                .type("Demand")
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").date("2020-01-02")
                    .status("Doing").date("2020-01-03")
                    .status("To Review").date("2020-01-04")
                    .status("Reviewing").date("2020-01-05")
                    .status("Done").date("2020-01-06")
                .eoT()
                .subtask("PROJ-02")
                    .type("Subtask")
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("To Review").date("2020-01-04")
                        .status("Reviewing").date("2020-01-05")
                        .status("Done").date("2020-01-06")
                    .eoT()
                    .worklogs()
                        .at("2020-01-03").timeSpentInHours(2.0)
                        .at("2020-01-05").timeSpentInHours(3.0)
                    .eoW()
                .endOfSubtask()
                .subtask("PROJ-03")
                    .type("Subtask")
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("To Review").date("2020-01-04")
                        .status("Reviewing").date("2020-01-05")
                        .status("Done").date("2020-01-06")
                    .eoT()
                    .worklogs()
                        .at("2020-01-03").timeSpentInHours(3.0)
                        .at("2020-01-05").timeSpentInHours(5.0)
                    .eoW()
                .endOfSubtask()
                .subtask("PROJ-04")
                    .type("Another Subtask")
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("To Review").date("2020-01-04")
                        .status("Reviewing").date("2020-01-05")
                        .status("Done").date("2020-01-06")
                    .eoT()
                    .worklogs()
                        .at("2020-01-03").timeSpentInHours(5.0)
                        .at("2020-01-05").timeSpentInHours(7.0)
                    .eoW()
                .endOfSubtask()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                    .hasType("Demand")
                    .hasEffortSumFromChildrenWithSubtaskTypeName(13.0, "Subtask");
    }

    @Test
    public void getEffortSumFromChildrenWithSubtaskTypeId_whenInvalidType_thenSumIsZero() {
        dsl()
        .environment()
            .givenDemand("PROJ-01")
                .type("Demand")
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").date("2020-01-02")
                    .status("Doing").date("2020-01-03")
                    .status("To Review").date("2020-01-04")
                    .status("Reviewing").date("2020-01-05")
                    .status("Done").date("2020-01-06")
                .eoT()
                .subtask("PROJ-02")
                    .type("Subtask")
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("To Review").date("2020-01-04")
                        .status("Reviewing").date("2020-01-05")
                        .status("Done").date("2020-01-06")
                    .eoT()
                    .worklogs()
                        .at("2020-01-03").timeSpentInHours(2.0)
                        .at("2020-01-05").timeSpentInHours(3.0)
                    .eoW()
            .eoI()
        .then()
            .assertThat()
                .issueKpi("PROJ-01")
                    .hasType("Demand")
                    .hasEffortSumFromChildrenWithSubtaskTypeName(0.0, "Foo");
    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .statuses()
                .withProgressingStatuses("Doing", "Reviewing")
                .withNotProgressingStatuses("Open", "To Do", "To Review", "Done")
            .eoS()
            .types()
                .addDemand("Demand")
                .addFeatures("Feature")
                .addSubtasks("Subtask", "Another Subtask")
            .eoT();

        return dsl;
    }
}
