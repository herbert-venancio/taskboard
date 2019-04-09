package objective.taskboard.followup.kpi;

import org.junit.Test;

import objective.taskboard.followup.kpi.services.DSLKpi;

public class StatusTransitionTest {

    @Test
    public void checkDate_fullTransition() {
        dsl()
            .environment()
                .statusTransition()
                    .status("To Do").date("2020-01-01")
                    .status("Doing").date("2020-01-02")
                    .status("Done").date("2020-01-03")
                .eoSt()
            .then()
            .assertThat()
                .statusTransition()
                    .atDate("2020-01-01").isStatus("To Do")
                    .atDate("2020-01-02").isStatus("Doing")
                    .atDate("2020-01-03").isStatus("Done")
                    .atDate("2020-01-04").isStatus("Done");
    }

    @Test
    public void checkDate_withoutIntermediateTransition() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").noDate()
                .status("Done").date("2020-01-03")
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .atDate("2020-01-01").isStatus("To Do")
                .atDate("2020-01-02").isStatus("To Do")
                .atDate("2020-01-03").isStatus("Done")
                .atDate("2020-01-04").isStatus("Done");
    }

    @Test
    public void checkDate_openIssue() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .atDate("2020-01-01").isStatus("To Do")
                .atDate("2020-01-02").isStatus("To Do");
    }

    @Test
    public void checkDate_futureIssue() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-02")
                .status("Doing").date("2020-01-03")
                .status("Done").date("2020-01-04")
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .atDate("2020-01-01").doesNotHaveStatus()
                .atDate("2020-01-02").isStatus("To Do")
                .atDate("2020-01-03").isStatus("Doing")
                .atDate("2020-01-04").isStatus("Done");
    }

    @Test
    public void firstDateOnProgressing_happyDay() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-02")
                .status("Doing").date("2020-01-03")
                .status("To Review").date("2020-01-04")
                .status("Reviewing").date("2020-01-05")
                .status("Done").date("2020-01-06")
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .firstDateOnProgressing().is("2020-01-03");
    }

    @Test
    public void firstDateOnProgressing_straightToReview() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-02")
                .status("Doing").noDate()
                .status("To Review").noDate()
                .status("Reviewing").date("2020-01-05")
                .status("Done").date("2020-01-06")
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .firstDateOnProgressing().is("2020-01-05");
    }

    @Test
    public void firstDateOnProgressing_openIssue() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-02")
                .status("Doing").noDate()
                .status("To Review").noDate()
                .status("Reviewing").noDate()
                .status("Done").noDate()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .firstDateOnProgressing().isNotPresent();
    }


    @Test
    public void firstDateOnProgressing_doneWithoutProgressing() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-02")
                .status("Doing").noDate()
                .status("To Review").noDate()
                .status("Reviewing").noDate()
                .status("Done").date("2020-01-05")
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .firstDateOnProgressing().isNotPresent();
    }


    @Test
    public void getFirstDateOnProgressingStatus_happyDay_consideringWorklog() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-02")
                .status("To Review").date("2020-01-03")
                .status("Reviewing").date("2020-01-04")
                .status("Done").date("2020-01-05")
                .withWorklogs()
                    .timeSpent(300)
                    .withDate("2020-01-02")
                    .on("Doing")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .firstDateOnProgressing().is("2020-01-02");

    }

    @Test
    public void getFirstDateOnProgressingStatus_happyDay_consideringMultiplesWorklog() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-02")
                .status("To Review").date("2020-01-03")
                .status("Reviewing").date("2020-01-04")
                .status("Done").date("2020-01-05")
                .withWorklogs()
                    .timeSpent(300).withDate("2020-01-01").on("Doing").and()
                    .timeSpent(500).withDate("2020-01-02").on("Doing")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .firstDateOnProgressing().is("2020-01-01");
    }

    @Test
    public void getFirstDateOnProgressingStatus_straightToReview_withWorklogOnDoing() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").noDate()
                .status("To Review").noDate()
                .status("Reviewing").date("2020-01-04")
                .status("Done").date("2020-01-05")
                .withWorklogs()
                    .timeSpent(300).withDate("2020-01-03").on("Doing")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .firstDateOnProgressing().is("2020-01-03");
    }

    @Test
    public void getFirstDateOnProgressingStatus_doneWithoutTransintingToProgressingStatus_withWorklog() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").noDate()
                .status("To Review").noDate()
                .status("Reviewing").noDate()
                .status("Done").date("2020-01-05")
                .withWorklogs()
                    .timeSpent(300).withDate("2020-01-06").on("Reviewing")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .firstDateOnProgressing().is("2020-01-06");
    }

    @Test
    public void getDateAfterLeavingLastProgressingStatus_happyDay(){
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-02")
                .status("Doing").date("2020-01-03")
                .status("To Review").date("2020-01-04")
                .status("Reviewing").date("2020-01-05")
                .status("Done").date("2020-01-06")
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .dateAterLeavingLastProgressingStatus().is("2020-01-06");
    }
    
    @Test
    public void getDateAfterLeavingLastProgressingStatus_withWorklogAfterClosed_thenWorklogDate(){
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-02")
                .status("Doing").date("2020-01-03")
                .status("To Review").date("2020-01-04")
                .status("Reviewing").date("2020-01-05")
                .status("Done").date("2020-01-06")
                .withWorklogs()
                    .timeSpent(300)
                    .withDate("2020-01-07")
                    .on("Reviewing")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .dateAterLeavingLastProgressingStatus().is("2020-01-07");
    }

    @Test
    public void getDateAfterLeavingLastProgressingStatus_skippingProgress(){
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-02")
                .status("Doing").noDate()
                .status("To Review").noDate()
                .status("Reviewing").noDate()
                .status("Done").date("2020-01-06")
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .dateAterLeavingLastProgressingStatus().is("2020-01-06");
    }

    @Test
    public void getDateAfterLeavingLastProgressingStatus_openIssue(){
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-02")
                .status("Doing").noDate()
                .status("To Review").noDate()
                .status("Reviewing").noDate()
                .status("Done").noDate()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .dateAterLeavingLastProgressingStatus().isNotPresent();
    }

    @Test
    public void getDateAfterLeavingLastProgressingStatus_wrongConfiguration(){
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-02")
                .status("Done").noDate()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .dateAterLeavingLastProgressingStatus().isNotPresent();
    }

    @Test
    public void getEnterDate_happyPath() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-01")
                .status("To Review").date("2020-01-02")
                .status("Reviewing").date("2020-01-02")
                .status("Done").date("2020-01-03")
                .withWorklogs()
                    .timeSpent(100).on("Doing").withDate("2020-01-01").and()
                    .timeSpent(200).on("Reviewing").withDate("2020-01-02")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("To Do").hasEnterDate("2020-01-01")
                .status("Doing").hasEnterDate("2020-01-01")
                .status("To Review").hasEnterDate("2020-01-02")
                .status("Reviewing").hasEnterDate("2020-01-02")
                .status("Done").hasEnterDate("2020-01-03");
    }

    @Test
    public void getEnterDate_whenFullTransitionInOneDay_thenHappyPath() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-01")
                .status("Done").date("2020-01-01")
                .withWorklogs()
                    .timeSpent(100).on("Doing").withDate("2020-01-01")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("To Do").hasEnterDate("2020-01-01")
                .status("Doing").hasEnterDate("2020-01-01")
                .status("Done").hasEnterDate("2020-01-01");
    }

    @Test
    public void getEnterDate_whenStatusIsNotDatedNeitherHasWorklogs_thenHasNoEnterDate() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").noDate()
                .status("Done").noDate()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("Doing").hasNoEnterDate();
    }

    @Test
    public void getEnterDate_whenStatusIsNotDatedButHasWorklogs_thenEnterDateIsTheMinimumOfWorklogsDates() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").noDate()
                .status("Done").noDate()
                .withWorklogs()
                    .timeSpent(100).on("Doing").withDate("2020-01-02").and()
                    .timeSpent(200).on("Doing").withDate("2020-01-03")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("Doing").hasEnterDate("2020-01-02");
    }

    public void getEnterDate_whenStatusIsNotDatedButHasWorklogsWithSameDate_thenEnterDateIsWorklogDate() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").noDate()
                .status("Done").noDate()
                .withWorklogs()
                    .timeSpent(100).on("Doing").withDate("2020-01-02").and()
                    .timeSpent(200).on("Doing").withDate("2020-01-02")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("Doing").hasEnterDate("2020-01-02");
    }

    @Test
    public void getEnterDate_whenStatusIsDatedAndDoesntHaveWorklogs_thenEnterDateIsTheStatusDate() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-02")
                .status("Done").noDate()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("Doing").hasEnterDate("2020-01-02");
    }

    @Test
    public void getEnterDate_whenStatusIsDatedAndHasWorklogs_thenEnterDateIsTheMinimumDateBetweenItsDateAndWorklogsDates() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-02")
                .status("Done").noDate()
                .withWorklogs()
                    .timeSpent(100).on("Doing").withDate("2020-01-02").and()
                    .timeSpent(200).on("Doing").withDate("2020-01-03")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("To Do").hasEnterDate("2020-01-01")
                .status("Doing").hasEnterDate("2020-01-02")
                .status("Done").hasNoEnterDate();
    }

    @Test
    public void getExitDate_happyPath() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-02")
                .status("To Review").date("2020-01-03")
                .status("Reviewing").date("2020-01-04")
                .status("Done").date("2020-01-05")
                .withWorklogs()
                    .timeSpent(100).on("Doing").withDate("2020-01-02").and()
                    .timeSpent(200).on("Reviewing").withDate("2020-01-04")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("To Do").hasExitDate("2020-01-02")
                .status("Doing").hasExitDate("2020-01-03")
                .status("To Review").hasExitDate("2020-01-04")
                .status("Reviewing").hasExitDate("2020-01-05")
                .status("Done").hasNoExitDate();
    }

    @Test
    public void getExitDate_whenFullTransitionInOneDay_thenHappyPath() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-01")
                .status("Done").date("2020-01-01")
                .withWorklogs()
                    .timeSpent(100).on("Doing").withDate("2020-01-01")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("To Do").hasExitDate("2020-01-01")
                .status("Doing").hasExitDate("2020-01-01")
                .status("Done").hasNoExitDate();
    }

    @Test
    public void getExitDate_whenNextStatusesAreNotDatedNeitherHaveWorklogs_thenAllStatusesHasNoExitDate() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").noDate()
                .status("To Review").noDate()
                .status("Reviewing").noDate()
                .status("Done").noDate()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("To Do").hasNoExitDate()
                .status("Doing").hasNoExitDate()
                .status("To Review").hasNoExitDate()
                .status("Reviewing").hasNoExitDate()
                .status("Done").hasNoExitDate();
    }

    @Test
    public void getExitDate_whenNextStatusesAreNotDatedButAtLeastOneHasWorklogs_thenExitDateIsTheMinimumOfItsWorklogsDates() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").noDate()
                .status("To Review").noDate()
                .status("Reviewing").noDate()
                .status("Done").noDate()
                .withWorklogs()
                    .timeSpent(100).on("Reviewing").withDate("2020-01-02").and()
                    .timeSpent(200).on("Reviewing").withDate("2020-01-03")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("Doing").hasExitDate("2020-01-02");
    }

    @Test
    public void getExitDate_whenNextStatusesAreNotDatedButHasWorklogs_thenExitDateIsTheMinimumOfWorklogsDatesFromMostRecent() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").noDate()
                .status("To Review").noDate()
                .status("Reviewing").noDate()
                .status("To Merge").noDate()
                .status("Merging").noDate()
                .status("Done").noDate()
                .withWorklogs()
                    .timeSpent(100).on("Reviewing").withDate("2020-01-02").and()
                    .timeSpent(200).on("Reviewing").withDate("2020-01-03").and()
                    .timeSpent(100).on("Merging").withDate("2020-01-05")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("Doing").hasExitDate("2020-01-02");
    }

    @Test
    public void getExitDate_whenNextStatusIsNotDatedNeitherHasWorklogsButAtLeastOneOfTheFollowingHave_thenExitDateIsTheMinimumOfValidDatesFromNextStatuses() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").noDate()
                .status("To Review").noDate()
                .status("Reviewing").noDate()
                .status("To Merge").date("2020-01-04")
                .status("Merging").date("2020-01-05")
                .status("Done").noDate()
                .withWorklogs()
                    .timeSpent(100).on("Merging").withDate("2020-01-05")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("Doing").hasExitDate("2020-01-04");
    }

    @Test
    public void getExitDate_whenNextStatusIsDatedAndDoesNotHaveWorklogs_thenExitDateIsTheNextStatusDate() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-02")
                .status("To Review").date("2020-01-03")
                .status("Reviewing").date("2020-01-03")
                .status("Done").noDate()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("Doing").hasExitDate("2020-01-03");
    }

    @Test
    public void getExitDate_whenNextStatusIsDatedAndHasWorklogs_thenExitDateIsTheMinimumDateBetweenNextStatusDateAndWorklogsDates() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-02")
                .status("To Review").date("2020-01-03")
                .status("Reviewing").date("2020-01-04")
                .status("Done").noDate()
                .withWorklogs()
                    .timeSpent(100).on("Doing").withDate("2020-01-02").and()
                    .timeSpent(200).on("Reviewing").withDate("2020-01-04")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("Doing").hasExitDate("2020-01-03");
    }

    @Test
    public void getEnterDateAndExitDateFromStatus_whenIntermediaryStatusesAreNotDated_thenSomeStatusesCanHaveExitDateWithoutHavingEnterDate() {
        dsl()
        .environment()
            .statusTransition()
                .status("To Do").date("2020-01-01")
                .status("Doing").date("2020-01-02")
                .status("To Review").noDate()
                .status("Reviewing").noDate()
                .status("Done").date("2020-01-05")
                .withWorklogs()
                    .timeSpent(100).on("Doing").withDate("2020-01-02").and()
                    .timeSpent(200).on("Reviewing").withDate("2020-01-04")
                .eoW()
            .eoSt()
        .then()
        .assertThat()
            .statusTransition()
                .status("Doing").hasEnterDate("2020-01-02")
                .status("Doing").hasExitDate("2020-01-04")
                .status("To Review").hasNoEnterDate()
                .status("To Review").hasExitDate("2020-01-04")
                .status("Reviewing").hasEnterDate("2020-01-04")
                .status("Reviewing").hasExitDate("2020-01-05")
                .status("Done").hasEnterDate("2020-01-05")
                .status("Done").hasNoExitDate();
    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .withStatus("To Do").isNotProgressing()
            .withStatus("Doing").isProgressing()
            .withStatus("To Review").isNotProgressing()
            .withStatus("Reviewing").isProgressing()
            .withStatus("To Merge").isNotProgressing()
            .withStatus("Merging").isProgressing()
            .withStatus("Done").isNotProgressing();
        return dsl;
    }

}