package objective.taskboard.followup.kpi;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.time.ZoneId;
import java.util.Optional;

import org.junit.Test;

import objective.taskboard.followup.kpi.enviroment.IssueKpiBuilder;
import objective.taskboard.followup.kpi.enviroment.StatusTransitionBuilder;
import objective.taskboard.followup.kpi.enviroment.StatusTransitionBuilder.DefaultStatus;
import objective.taskboard.testUtils.FixedClock;

public class StatusTransitionTest {
    
    private static final DefaultStatus TODO = new DefaultStatus("To Do",false);
    private static final DefaultStatus DOING = new DefaultStatus("Doing",true);
    private static final DefaultStatus TO_REVIEW = new DefaultStatus("To Review",false);
    private static final DefaultStatus REVIEWING = new DefaultStatus("Reviewing",true);
    private static final DefaultStatus DONE = new DefaultStatus("Done",false);
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    
    @Test
    public void checkDate_fullTransition() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                                                .addTransition(TODO, "2020-01-01")
                                                .addTransition(DOING, "2020-01-02")
                                                .addTransition(DONE, "2020-01-03");
        
        StatusTransition first = builder.buildOrCry();
                        
        assertThat(first.givenDate(parseDateTime("2020-01-01")).get(), is(builder.getTransition(TODO)));
        assertThat(first.givenDate(parseDateTime("2020-01-02")).get(), is(builder.getTransition(DOING)));
        assertThat(first.givenDate(parseDateTime("2020-01-03")).get(), is(builder.getTransition(DONE)));
        assertThat(first.givenDate(parseDateTime("2020-01-04")).get(), is(builder.getTransition(DONE)));
    }
    
    @Test
    public void checkDate_withoutIntermediateTransition() {
        
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO, "2020-01-01")
                .addTransition(DOING)
                .addTransition(DONE, "2020-01-03");

        StatusTransition first = builder.buildOrCry();

        assertThat(first.givenDate(parseDateTime("2020-01-01")).get(), is(builder.getTransition(TODO)));
        assertThat(first.givenDate(parseDateTime("2020-01-02")).get(), is(builder.getTransition(TODO)));
        assertThat(first.givenDate(parseDateTime("2020-01-03")).get(), is(builder.getTransition(DONE)));
        assertThat(first.givenDate(parseDateTime("2020-01-04")).get(), is(builder.getTransition(DONE)));

    }
    
    @Test
    public void checkDate_openIssue() {
        StatusTransition statusTransitions = new DatedStatusTransition("To Do", parseDateTime("2020-01-01"),false,Optional.empty());

        assertThat(statusTransitions.givenDate(parseDateTime("2020-01-01")).get(), is(statusTransitions));
        assertThat(statusTransitions.givenDate(parseDateTime("2020-01-02")).get(), is(statusTransitions));
    }
    
    @Test
    public void checkDate_futureIssue() {
        
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO, "2020-01-02")
                .addTransition(DOING,"2020-01-03")
                .addTransition(DONE, "2020-01-04");
    
        StatusTransition first = builder.buildOrCry();

        assertThat(first.givenDate(parseDateTime("2020-01-01")).isPresent(), is(false) );
        assertThat(first.givenDate(parseDateTime("2020-01-02")).get(), is(builder.getTransition(TODO)));
        assertThat(first.givenDate(parseDateTime("2020-01-03")).get(), is(builder.getTransition(DOING)));
        assertThat(first.givenDate(parseDateTime("2020-01-04")).get(), is(builder.getTransition(DONE)));
    }
    
    @Test
    public void firstDateOnProgressing_happyDay() {
        
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO, "2020-01-02")
                .addTransition(DOING,"2020-01-03")
                .addTransition(TO_REVIEW,"2020-01-04")
                .addTransition(REVIEWING,"2020-01-05")
                .addTransition(DONE, "2020-01-06");
    
        StatusTransition first = builder.buildOrCry();
        
        String date = first.firstDateOnProgressing(ZONE_ID).map(s -> s.toLocalDate().toString()).orElse("Not Found");
        assertThat(date,is("2020-01-03"));
    }
    
    @Test
    public void firstDateOnProgressing_straightToReview() {
        
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO, "2020-01-02")
                .addTransition(DOING)
                .addTransition(TO_REVIEW)
                .addTransition(REVIEWING,"2020-01-05")
                .addTransition(DONE, "2020-01-06");
    
        StatusTransition first = builder.buildOrCry();
        
        String date = first.firstDateOnProgressing(ZONE_ID).map(s -> s.toLocalDate().toString()).orElse("Not Found");
        assertThat(date,is("2020-01-05"));
    }
    
    @Test
    public void firstDateOnProgressing_openIssue() {
        
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING)
                .addTransition(TO_REVIEW)
                .addTransition(REVIEWING)
                .addTransition(DONE);
        
        StatusTransition first = builder.buildOrCry();
        
        String date = first.firstDateOnProgressing(ZONE_ID).map(s -> s.toLocalDate().toString()).orElse("Not Found");
        assertThat(date, is("Not Found"));
    }


    @Test
    public void firstDateOnProgressing_doneWithoutProgressing() {

        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING)
                .addTransition(TO_REVIEW)
                .addTransition(REVIEWING)
                .addTransition(DONE,"2020-01-05");
        
        StatusTransition first = builder.buildOrCry();
        
        String date = first.firstDateOnProgressing(ZONE_ID).map(s -> s.toLocalDate().toString()).orElse("Not Found");
        assertThat(date, is("Not Found"));
    }


    @Test
    public void getFirstDateOnProgressingStatus_happyDay_consideringWorklog() {
        FixedClock clock = new FixedClock();
        final String today = "2020-01-05";
        clock.setNow(parseDateTime(today).toInstant());
        IssueKpi issue = new IssueKpiBuilder("I-1", new IssueTypeKpi(1l, "Development"), KpiLevel.SUBTASKS, clock)
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(TO_REVIEW,"2020-01-03")
                .addTransition(REVIEWING,"2020-01-04")
                .addTransition(DONE,"2020-01-05")
                .addWorklog("2020-01-02", 300)
                .build();
        
        StatusTransition first = issue.firstStatus().get();
        String date = first.firstDateOnProgressing(ZONE_ID).map(s -> s.toLocalDate().toString()).orElse("Not Found");
        assertThat(date, is("2020-01-02"));
    }
    
    @Test
    public void getFirstDateOnProgressingStatus_happyDay_consideringMultiplesWorklog() {
        FixedClock clock = new FixedClock();
        final String today = "2020-01-05";
        clock.setNow(parseDateTime(today).toInstant());
        IssueKpi issue = new IssueKpiBuilder("I-1", new IssueTypeKpi(1l, "Development"), KpiLevel.SUBTASKS, clock)
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(TO_REVIEW,"2020-01-03")
                .addTransition(REVIEWING,"2020-01-04")
                .addTransition(DONE,"2020-01-05")
                .addWorklog("2020-01-01", 300)
                .addWorklog("2020-01-02", 500)
                .build();
        
        StatusTransition first = issue.firstStatus().get();
        String date = first.firstDateOnProgressing(ZONE_ID).map(s -> s.toLocalDate().toString()).orElse("Not Found");
        assertThat(date, is("2020-01-01"));
    }
    
    @Test
    public void getFirstDateOnProgressingStatus_straightToReview_withWorklogOnDoing() {
        FixedClock clock = new FixedClock();
        final String today = "2020-01-05";
        clock.setNow(parseDateTime(today).toInstant());
        IssueKpi issue = new IssueKpiBuilder("I-1", new IssueTypeKpi(1l, "Development"), KpiLevel.SUBTASKS, clock)
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING)
                .addTransition(TO_REVIEW)
                .addTransition(REVIEWING,"2020-01-04")
                .addTransition(DONE,"2020-01-05")
                .addWorklog("2020-01-03",300)
                .build();
        
        StatusTransition first = issue.firstStatus().get();
        String date = first.firstDateOnProgressing(ZONE_ID).map(s -> s.toLocalDate().toString()).orElse("Not Found");
        assertThat(date, is("2020-01-03"));
    }
    
    @Test
    public void getFirstDateOnProgressingStatus_doneWithoutTransintingToProgressingStatus_withWorklog() {
        FixedClock clock = new FixedClock();
        final String today = "2020-01-05";
        clock.setNow(parseDateTime(today).toInstant());
        IssueKpi issue = new IssueKpiBuilder("I-1", new IssueTypeKpi(1l, "Development"), KpiLevel.SUBTASKS, clock)
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING)
                .addTransition(TO_REVIEW)
                .addTransition(REVIEWING)
                .addTransition(DONE,today)
                .addWorklog("2020-01-06",300)
                .build();
        
        StatusTransition first = issue.firstStatus().get();
        String date = first.firstDateOnProgressing(ZONE_ID).map(s -> s.toLocalDate().toString()).orElse("Not Found");
        assertThat(date, is("2020-01-06"));
    }
    
    @Test
    public void getDateAfterLeavingLastProgressingStatus_happyDay(){
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO, "2020-01-02")
                .addTransition(DOING,"2020-01-03")
                .addTransition(TO_REVIEW,"2020-01-04")
                .addTransition(REVIEWING,"2020-01-05")
                .addTransition(DONE, "2020-01-06");
    
        StatusTransition first = builder.buildOrCry();
        
        String date = first.getDateAfterLeavingLastProgressingStatus().map(s -> s.toString()).orElse("Not Found");
        assertThat(date,is("2020-01-06"));
    }
    
    @Test
    public void getDateAfterLeavingLastProgressingStatus_skippingProgress(){
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO, "2020-01-02")
                .addTransition(DOING)
                .addTransition(TO_REVIEW)
                .addTransition(REVIEWING)
                .addTransition(DONE, "2020-01-06");
    
        StatusTransition first = builder.buildOrCry();
        
        String date = first.getDateAfterLeavingLastProgressingStatus().map(s -> s.toString()).orElse("Not Found");
        assertThat(date,is("2020-01-06"));
    }
    
    @Test
    public void getDateAfterLeavingLastProgressingStatus_consideringWorklog(){
        IssueKpi issue = new IssueKpiBuilder("I-1", new IssueTypeKpi(1l, "Development"), KpiLevel.SUBTASKS)
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING)
                .addTransition(TO_REVIEW)
                .addTransition(REVIEWING)
                .addTransition(DONE,"2020-01-05")
                .addWorklog("2020-01-04",300)
                .build();
        
        StatusTransition first = issue.firstStatus().get();
        String date = first.getDateAfterLeavingLastProgressingStatus().map(s -> s.toString()).orElse("Not Found");
        assertThat(date, is("2020-01-05"));
    }
    
    @Test
    public void getDateAfterLeavingLastProgressingStatus_wrongConfiguration(){
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO, "2020-01-02")
                .addTransition(DONE);
    
        StatusTransition first = builder.buildOrCry();
        
        String date = first.getDateAfterLeavingLastProgressingStatus().map(s -> s.toString()).orElse("Not Found");
        assertThat(date,is("Not Found"));
    }
    
    @Test
    public void getDateAfterLeavingLastProgressingStatus_openIssue(){
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO, "2020-01-02")
                .addTransition(DOING)
                .addTransition(TO_REVIEW)
                .addTransition(REVIEWING)
                .addTransition(DONE);
    
        StatusTransition first = builder.buildOrCry();
        
        String date = first.getDateAfterLeavingLastProgressingStatus().map(s -> s.toString()).orElse("Not Found");
        assertThat(date,is("Not Found"));
    }
    
}
