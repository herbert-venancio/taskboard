package objective.taskboard.followup.kpi.cycletime;

import static objective.taskboard.followup.kpi.cycletime.CycleTimeKpiAsserter.cycleTimeKpi;
import static objective.taskboard.followup.kpi.properties.KpiCycleTimePropertiesMocker.withSubtaskCycleTimeProperties;

import org.junit.Test;

import objective.taskboard.followup.kpi.services.DSLKpi;

public class CycleTimeKpiTest {

    @Test
    public void cycleTime_happyDay() {
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .type("Development")
                    .project("TASKB")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-03")
                        .status("To Review").date("2019-01-04")
                        .status("Reviewing").date("2019-01-05")
                        .status("Done").date("2019-01-06")
                        .status("Cancelled").noDate()
                    .eoT()
                .eoI()
                .withKpiProperties(
                    withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
                )
                .services()
                    .issueColor()
                        .withProgressingStatusesColor("#FFFFFF")
                        .withNonProgressingStatusesColor("#AAAAAA")
                    .eoIC()
                .eoS()
            .then()
                .assertThat(cycleTimeKpi("I-1"))
                        .startsAt("2019-01-02")
                        .endsAt("2019-01-06")
                        .hasTotalCycleTime(5)
                        .hasSubCycles()
                            .subCycle("To Do")
                                .hasEnterDate("2019-01-02")
                                .hasExitDate("2019-01-03")
                                .hasCycleTimeInDays(1)
                                .hasColorHex("#AAAAAA")
                            .eoS()
                            .subCycle("Doing")
                                .hasEnterDate("2019-01-03")
                                .hasExitDate("2019-01-04")
                                .hasCycleTimeInDays(1)
                                .hasColorHex("#FFFFFF")
                            .eoS()
                            .subCycle("To Review")
                                .hasEnterDate("2019-01-04")
                                .hasExitDate("2019-01-05")
                                .hasCycleTimeInDays(1)
                                .hasColorHex("#AAAAAA")
                             .eoS()
                            .subCycle("Reviewing")
                                .hasEnterDate("2019-01-05")
                                .hasExitDate("2019-01-06")
                                .hasCycleTimeInDays(1)
                                .hasColorHex("#FFFFFF")
                            .eoS()
                        .eoSC();
    }
    @Test
    public void cycleTime_whenNoTypeConfigured_thenReturnDefaultColor() {
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .emptyType()
                    .project("TASKB")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-03")
                        .status("To Review").date("2019-01-04")
                        .status("Reviewing").date("2019-01-05")
                        .status("Done").date("2019-01-06")
                        .status("Cancelled").noDate()
                    .eoT()
                .eoI()
                .withKpiProperties(
                    withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
                )
                .services()
                    .issueColor()
                        .withProgressingStatusesColor("#FFFFFF")
                        .withNonProgressingStatusesColor("#AAAAAA")
                    .eoIC()
                .eoS()
            .then()
                .assertThat(cycleTimeKpi("I-1"))
                        .startsAt("2019-01-02")
                        .endsAt("2019-01-06")
                        .hasTotalCycleTime(5)
                        .hasSubCycles()
                            .subCycle("To Do")
                                .hasEnterDate("2019-01-02")
                                .hasExitDate("2019-01-03")
                                .hasCycleTimeInDays(1)
                                .hasColorHex("#A1423C")
                            .eoS()
                            .subCycle("Doing")
                                .hasEnterDate("2019-01-03")
                                .hasExitDate("2019-01-04")
                                .hasCycleTimeInDays(1)
                                .hasColorHex("#A1423C")
                            .eoS()
                            .subCycle("To Review")
                                .hasEnterDate("2019-01-04")
                                .hasExitDate("2019-01-05")
                                .hasCycleTimeInDays(1)
                                .hasColorHex("#A1423C")
                             .eoS()
                            .subCycle("Reviewing")
                                .hasEnterDate("2019-01-05")
                                .hasExitDate("2019-01-06")
                                .hasCycleTimeInDays(1)
                                .hasColorHex("#A1423C")
                            .eoS()
                        .eoSC();
    }
    @Test
    public void cycleTime_whenJumpingStatus_thenShouldntHaveCycleTime() {
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .type("Development")
                    .project("TASKB")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").noDate()
                        .status("To Review").noDate()
                        .status("Reviewing").noDate()
                        .status("Done").date("2019-01-06")
                        .status("Cancelled").noDate()
                    .eoT()
                .eoI()
                .withKpiProperties(
                    withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
                )
            .then()
                .assertThat(cycleTimeKpi("I-1"))
                        .startsAt("2019-01-02")
                        .endsAt("2019-01-06")
                        .hasTotalCycleTime(5)
                        .hasSubCycles()
                            .subCycle("To Do")
                                .hasEnterDate("2019-01-02")
                                .hasExitDate("2019-01-06")
                                .hasCycleTimeInDays(4)
                            .eoS()
                            .subCycle("Doing")
                                .hasNoEnterDate()
                                .hasExitDate("2019-01-06")
                                .hasNoCycle()
                            .eoS()
                            .subCycle("To Review")
                                .hasNoEnterDate()
                                .hasExitDate("2019-01-06")
                                .hasNoCycle()
                            .eoS()
                            .subCycle("Reviewing")
                                .hasNoEnterDate()
                                .hasExitDate("2019-01-06")
                                .hasNoCycle()
                            .eoS()
                        .eoSC();
    }

    @Test
    public void cycleTime_whenJumpingStatus_withWorklogOnThem_thenShouldHaveCycleTime() {
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .type("Development")
                    .project("TASKB")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").noDate()
                        .status("To Review").noDate()
                        .status("Reviewing").date("2019-01-05")
                        .status("Done").date("2019-01-06")
                        .status("Cancelled").noDate()
                    .eoT()
                    .worklogs()
                        .at("2019-01-03").timeSpentInHours(1)
                    .eoW()
                .eoI()
                .withKpiProperties(
                    withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
                )
            .then()
                .assertThat(cycleTimeKpi("I-1"))
                        .startsAt("2019-01-02")
                        .endsAt("2019-01-06")
                        .hasTotalCycleTime(5)
                        .hasSubCycles()
                            .subCycle("To Do")
                                .hasEnterDate("2019-01-02")
                                .hasExitDate("2019-01-03")
                                .hasCycleTimeInDays(1)
                             .eoS()
                            .subCycle("Doing")
                                .hasEnterDate("2019-01-03")
                                .hasExitDate("2019-01-05")
                                .hasCycleTimeInDays(2)
                            .eoS()
                            .subCycle("To Review")
                                .hasNoEnterDate()
                                .hasExitDate("2019-01-05")
                                .hasNoCycle()
                            .eoS()
                            .subCycle("Reviewing")
                                .hasEnterDate("2019-01-05")
                                .hasExitDate("2019-01-06")
                                .hasCycleTimeInDays(1)
                            .eoS()
                        .eoSC();
    }

    @Test
    public void cycleTime_whenJumpingStatus_withMultipleWorklogOnThem_thenShouldHaveCycleTime_countingTheEarliestWorklogDate() {
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .type("Development")
                    .project("TASKB")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").noDate()
                        .status("To Review").noDate()
                        .status("Reviewing").date("2019-01-05")
                        .status("Done").date("2019-01-06")
                        .status("Cancelled").noDate()
                    .eoT()
                    .worklogs()
                        .at("2019-01-03").timeSpentInHours(1)
                        .at("2019-01-04").timeSpentInHours(1)
                        .at("2019-01-05").timeSpentInHours(1)
                    .eoW()
                .eoI()
                .withKpiProperties(
                    withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
                )
            .then()
                .assertThat(cycleTimeKpi("I-1"))
                        .startsAt("2019-01-02")
                        .endsAt("2019-01-06")
                        .hasTotalCycleTime(5)
                        .hasSubCycles()
                            .subCycle("To Do")
                                .hasEnterDate("2019-01-02")
                                .hasExitDate("2019-01-03")
                                .hasCycleTimeInDays(1)
                             .eoS()
                            .subCycle("Doing")
                                .hasEnterDate("2019-01-03")
                                .hasExitDate("2019-01-05")
                                .hasCycleTimeInDays(2)
                            .eoS()
                            .subCycle("To Review")
                                .hasNoEnterDate()
                                .hasExitDate("2019-01-05")
                                .hasNoCycle()
                            .eoS()
                            .subCycle("Reviewing")
                                .hasEnterDate("2019-01-05")
                                .hasExitDate("2019-01-06")
                                .hasCycleTimeInDays(1)
                            .eoS()
                        .eoSC();
    }

    @Test
    public void cycleTime_withDate_withMultipleWorklogOnThem_thenShouldHaveCycleTime_countingTheEarliestOfAllDates() {
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .type("Development")
                    .project("TASKB")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-04")
                        .status("To Review").noDate()
                        .status("Reviewing").date("2019-01-05")
                        .status("Done").date("2019-01-06")
                        .status("Cancelled").noDate()
                    .eoT()
                    .worklogs()
                        .at("2019-01-03").timeSpentInHours(1)
                        .at("2019-01-04").timeSpentInHours(1)
                        .at("2019-01-05").timeSpentInHours(1)
                    .eoW()
                .eoI()
                .withKpiProperties(
                    withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
                )
            .then()
                .assertThat(cycleTimeKpi("I-1"))
                        .startsAt("2019-01-02")
                        .endsAt("2019-01-06")
                        .hasTotalCycleTime(5)
                        .hasSubCycles()
                            .subCycle("To Do")
                                .hasEnterDate("2019-01-02")
                                .hasExitDate("2019-01-03")
                                .hasCycleTimeInDays(1)
                            .eoS()
                            .subCycle("Doing")
                                .hasEnterDate("2019-01-03")
                                .hasExitDate("2019-01-05")
                                .hasCycleTimeInDays(2)
                            .eoS()
                            .subCycle("To Review")
                                .hasNoEnterDate()
                                .hasExitDate("2019-01-05")
                                .hasNoCycle()
                            .eoS()
                            .subCycle("Reviewing")
                                .hasEnterDate("2019-01-05")
                                .hasExitDate("2019-01-06")
                                .hasCycleTimeInDays(1)
                            .eoS()
                        .eoSC();
    }

    @Test
    public void cycleTime_whenAllTransitionsWereMadeAtTheSameDay_thenShouldNotHaveSubCycle() {
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .type("Development")
                    .project("TASKB")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-01")
                        .status("Doing").date("2019-01-01")
                        .status("To Review").date("2019-01-01")
                        .status("Reviewing").date("2019-01-01")
                        .status("Done").date("2019-01-01")
                        .status("Cancelled").date("2019-01-01")
                    .eoT()
                .eoI()
                .withKpiProperties(
                    withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
                )
            .then()
                .assertThat(cycleTimeKpi("I-1"))
                        .startsAt("2019-01-01")
                        .endsAt("2019-01-01")
                        .hasTotalCycleTime(1)
                        .hasSubCycles()
                            .subCycle("To Do")
                                .hasEnterDate("2019-01-01")
                                .hasExitDate("2019-01-01")
                                .hasNoCycle()
                            .eoS()
                            .subCycle("Doing")
                                .hasEnterDate("2019-01-01")
                                .hasExitDate("2019-01-01")
                                .hasNoCycle()
                            .eoS()
                            .subCycle("To Review")
                                .hasEnterDate("2019-01-01")
                                .hasExitDate("2019-01-01")
                                .hasNoCycle()
                            .eoS()
                            .subCycle("Reviewing")
                                .hasEnterDate("2019-01-01")
                                .hasExitDate("2019-01-01")
                                .hasNoCycle()
                            .eoS()
                        .eoSC();
    }

    @Test
    public void cycleTime_whenSomeTransitionsWereMadeAtTheSameDay_andOtherStatusDoesNotHaveTransitions_thenThoseWithDifferentDateTransitionsShouldHaveCycle() {
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .type("Development")
                    .project("TASKB")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-05")
                        .status("To Review").noDate()
                        .status("Reviewing").date("2019-01-05")
                        .status("Done").date("2019-01-08")
                        .status("Cancelled").noDate()
                    .eoT()
                .eoI()
                .withKpiProperties(
                    withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
                )
            .then()
                .assertThat(cycleTimeKpi("I-1"))
                        .startsAt("2019-01-02")
                        .endsAt("2019-01-08")
                        .hasTotalCycleTime(7)
                        .hasSubCycles()
                            .subCycle("To Do")
                                .hasEnterDate("2019-01-02")
                                .hasExitDate("2019-01-05")
                                .hasCycleTimeInDays(3)
                            .eoS()
                            .subCycle("Doing")
                                .hasEnterDate("2019-01-05")
                                .hasExitDate("2019-01-05")
                                .hasNoCycle()
                            .eoS()
                            .subCycle("To Review")
                                .hasNoEnterDate()
                                .hasExitDate("2019-01-05")
                                .hasNoCycle()
                            .eoS()
                            .subCycle("Reviewing")
                                .hasEnterDate("2019-01-05")
                                .hasExitDate("2019-01-08")
                                .hasCycleTimeInDays(3)
                            .eoS()
                        .eoSC();
    }

    @Test
    public void cycleTime_whenWorklogRegisteredAfterIssueIsClosed_thenCycleForThePreviousStatusShouldConsiderTheWorklogDate_andStatusFromWorklogShouldHaveNoCycle() {
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .type("Development")
                    .project("TASKB")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-03")
                        .status("To Review").date("2019-01-04")
                        .status("Reviewing").date("2019-01-05")
                        .status("Done").date("2019-01-06")
                        .status("Cancelled").noDate()
                    .eoT()
                    .worklogs()
                        .at("2019-01-08").timeSpentInHours(1)
                    .eoW()
                .eoI()
                .withKpiProperties(
                    withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
                )
            .then()
                .assertThat(cycleTimeKpi("I-1"))
                        .startsAt("2019-01-02")
                        .endsAt("2019-01-08")
                        .hasTotalCycleTime(7)
                        .hasSubCycles()
                            .subCycle("To Do")
                                .hasEnterDate("2019-01-02")
                                .hasExitDate("2019-01-03")
                                .hasCycleTimeInDays(1)
                            .eoS()
                            .subCycle("Doing")
                                .hasEnterDate("2019-01-03")
                                .hasExitDate("2019-01-04")
                                .hasCycleTimeInDays(1)
                            .eoS()
                            .subCycle("To Review")
                                .hasEnterDate("2019-01-04")
                                .hasExitDate("2019-01-05")
                                .hasCycleTimeInDays(1)
                            .eoS()
                            .subCycle("Reviewing")
                                .hasEnterDate("2019-01-05")
                                .hasExitDate("2019-01-08")
                                .hasCycleTimeInDays(3)
                            .eoS()
                        .eoSC();
    }

    @Test
    public void cycleTime_whenWorklogAfterDoneAndSkippedReviewing_thenReviewingEnterDateIsTheWorklogsMinimumDateAndExitDateIsTheWorklosgMaximumDate() {
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .type("Development")
                    .project("TASKB")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-03")
                        .status("To Review").date("2019-01-04")
                        .status("Reviewing").noDate()
                        .status("Done").date("2019-01-06")
                        .status("Cancelled").noDate()
                    .eoT()
                    .worklogs()
                        .at("2019-01-08").timeSpentInHours(1)
                        .at("2019-01-09").timeSpentInHours(2)
                    .eoW()
                .eoI()
                .withKpiProperties(
                    withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
                )
            .then()
                .assertThat(cycleTimeKpi("I-1"))
                        .startsAt("2019-01-02")
                        .endsAt("2019-01-09")
                        .hasTotalCycleTime(8)
                        .hasSubCycles()
                            .subCycle("To Do")
                                .hasEnterDate("2019-01-02")
                                .hasExitDate("2019-01-03")
                                .hasCycleTimeInDays(1)
                            .eoS()
                            .subCycle("Doing")
                                .hasEnterDate("2019-01-03")
                                .hasExitDate("2019-01-04")
                                .hasCycleTimeInDays(1)
                            .eoS()
                            .subCycle("To Review")
                                .hasEnterDate("2019-01-04")
                                .hasExitDate("2019-01-08")
                                .hasCycleTimeInDays(4)
                            .eoS()
                            .subCycle("Reviewing")
                                .hasEnterDate("2019-01-08")
                                .hasExitDate("2019-01-09")
                                .hasCycleTimeInDays(1)
                            .eoS()
                        .eoSC();
    }

    @Test
    public void cycleTime_whenWorklogDateBeforeDone_thenDoingEnterDateIsTheMinimumOfWorklogsDatesAndExitDateIsTheMaximumOfWorklogsDates() {
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .type("Development")
                    .project("TASKB")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-03")
                        .status("To Review").date("2019-01-04")
                        .status("Reviewing").date("2019-01-05")
                        .status("Done").date("2019-01-06")
                        .status("Cancelled").noDate()
                    .eoT()
                    .worklogs()
                        .at("2019-01-01").timeSpentInHours(1)
                    .eoW()
                .eoI()
                .withKpiProperties(
                    withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
                )
            .then()
                .assertThat(cycleTimeKpi("I-1"))
                        .startsAt("2019-01-01")
                        .endsAt("2019-01-06")
                        .hasTotalCycleTime(6)
                        .hasSubCycles()
                            .subCycle("To Do")
                                .hasEnterDate("2019-01-01")
                                .hasExitDate("2019-01-01")
                                .hasCycleTimeInDays(0)
                            .eoS()
                            .subCycle("Doing")
                                .hasEnterDate("2019-01-01")
                                .hasExitDate("2019-01-04")
                                .hasCycleTimeInDays(3)
                            .eoS()
                            .subCycle("To Review")
                                .hasEnterDate("2019-01-04")
                                .hasExitDate("2019-01-05")
                                .hasCycleTimeInDays(1)
                            .eoS()
                            .subCycle("Reviewing")
                                .hasEnterDate("2019-01-05")
                                .hasExitDate("2019-01-06")
                                .hasCycleTimeInDays(1)
                            .eoS()
                        .eoSC();
    }

    @Test
    public void cycleTime_whenWorklogDateBeforeDoingAndSkippedDoing_thenDoingEnterDateIsTheMinimumOfWorklogsDatesAndExitDateIsTheMaximumOfWorklogsDates() {
        dsl()
            .environment()
                .givenSubtask("I-1")
                    .type("Development")
                    .project("TASKB")
                    .withTransitions()
                        .status("Open").date("2019-01-02")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").noDate()
                        .status("To Review").date("2019-01-04")
                        .status("Reviewing").date("2019-01-05")
                        .status("Done").date("2019-01-06")
                        .status("Cancelled").noDate()
                    .eoT()
                    .worklogs()
                        .at("2019-01-01").timeSpentInHours(1)
                    .eoW()
                .eoI()
                .withKpiProperties(
                    withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
                )
            .then()
                .assertThat(cycleTimeKpi("I-1"))
                        .startsAt("2019-01-01")
                        .endsAt("2019-01-06")
                        .hasTotalCycleTime(6)
                        .hasSubCycles()
                            .subCycle("To Do")
                                .hasEnterDate("2019-01-01")
                                .hasExitDate("2019-01-01")
                                .hasCycleTimeInDays(0)
                            .eoS()
                            .subCycle("Doing")
                                .hasEnterDate("2019-01-01")
                                .hasExitDate("2019-01-04")
                                .hasCycleTimeInDays(3)
                            .eoS()
                            .subCycle("To Review")
                                .hasEnterDate("2019-01-04")
                                .hasExitDate("2019-01-05")
                                .hasCycleTimeInDays(1)
                            .eoS()
                            .subCycle("Reviewing")
                                .hasEnterDate("2019-01-05")
                                .hasExitDate("2019-01-06")
                                .hasCycleTimeInDays(1)
                            .eoS()
                        .eoSC();
    }

    @Test
    public void subCycle_whenNotCompletedAllCycles() {
        dsl()
           .environment()
               .withKpiProperties(
                   withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
               )
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
               .assertThat(cycleTimeKpi("PROJ-01"))
                       .hasSubCycles()
                           .subCycle("To Do")
                               .hasEnterDate("2020-01-02")
                               .hasExitDate("2020-01-03")
                               .hasCycleTimeInDays(1l)
                           .eoS()
                           .subCycle("Doing")
                               .hasEnterDate("2020-01-03")
                               .hasNoExitDate()
                               .hasNoCycle()
                           .eoS()
                           .subCycle("To Review")
                               .hasNoEnterDate()
                               .hasNoExitDate()
                               .hasNoCycle()
                           .eoS()
                               .subCycle("Reviewing")
                               .hasNoEnterDate()
                               .hasNoExitDate()
                               .hasNoCycle()
                           .eoS();
    }

    @Test
    public void subCycle_whenSkippingStatus_thoseStausShouldNotHaveSubCycle() {
        dsl()
           .environment()
               .withKpiProperties(
                   withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
               )
               .givenSubtask("PROJ-01")
                   .type("Subtask")
                   .project("PROJ")
                   .withTransitions()
                       .status("Open").date("2020-01-01")
                       .status("To Do").date("2020-01-02")
                       .status("Doing").noDate()
                       .status("To Review").noDate()
                       .status("Reviewing").date("2020-01-05")
                       .status("Done").date("2020-01-06")
                   .eoT()
               .eoI()
           .then()
               .assertThat(cycleTimeKpi("PROJ-01"))
                       .hasSubCycles()
                           .subCycle("To Do")
                               .hasEnterDate("2020-01-02")
                               .hasExitDate("2020-01-05")
                               .hasCycleTimeInDays(3l)
                           .eoS()
                           .subCycle("Doing")
                               .hasNoEnterDate()
                               .hasExitDate("2020-01-05")
                               .hasNoCycle()
                           .eoS()
                           .subCycle("To Review")
                               .hasNoEnterDate()
                               .hasExitDate("2020-01-05")
                               .hasNoCycle()
                           .eoS()
                               .subCycle("Reviewing")
                                   .hasEnterDate("2020-01-05")
                                   .hasExitDate("2020-01-06")
                                   .hasCycleTimeInDays(1l)
                           .eoS();
    }

    @Test
    public void subCycle_whenAllTransitionsWereOnSameDay_thenShouldHaveSubCycles_withZeroDaysOfCycle() {
        dsl()
           .environment()
               .withKpiProperties(
                   withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
               )
               .givenSubtask("PROJ-01")
                   .type("Subtask")
                   .project("PROJ")
                   .withTransitions()
                       .status("Open").date("2020-01-01")
                       .status("To Do").date("2020-01-01")
                       .status("Doing").date("2020-01-01")
                       .status("To Review").date("2020-01-01")
                       .status("Reviewing").date("2020-01-01")
                       .status("Done").date("2020-01-01")
                   .eoT()
               .eoI()
           .then()
               .assertThat(cycleTimeKpi("PROJ-01"))
                       .hasSubCycles()
                           .subCycle("To Do")
                               .hasEnterDate("2020-01-01")
                               .hasExitDate("2020-01-01")
                               .hasCycleTimeInDays(0l)
                           .eoS()
                           .subCycle("Doing")
                               .hasEnterDate("2020-01-01")
                               .hasExitDate("2020-01-01")
                               .hasCycleTimeInDays(0l)
                           .eoS()
                           .subCycle("To Review")
                               .hasEnterDate("2020-01-01")
                               .hasExitDate("2020-01-01")
                               .hasCycleTimeInDays(0l)
                           .eoS()
                               .subCycle("Reviewing")
                                   .hasEnterDate("2020-01-01")
                                   .hasExitDate("2020-01-01")
                                   .hasCycleTimeInDays(0l)
                           .eoS();
    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .statuses()
                .withProgressingStatuses("Doing","Reviewing")
                .withNotProgressingStatuses("Open","To Do","To Review","Cancelled","Done")
            .eoS()
            .withSubtaskType("Development");
        return dsl;
    }
}
