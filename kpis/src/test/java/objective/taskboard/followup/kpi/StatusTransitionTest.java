package objective.taskboard.followup.kpi;

import org.junit.Test;

import objective.taskboard.followup.kpi.enviroment.DSLKpi;

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
    
    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .withStatus("To Do").isNotProgressing()
            .withStatus("Doing").isProgressing()
            .withStatus("To Review").isNotProgressing()
            .withStatus("Reviewing").isProgressing()
            .withStatus("Done").isNotProgressing();
        return dsl;
    }
    
}
