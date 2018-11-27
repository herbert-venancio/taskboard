package objective.taskboard.followup.kpi;

import static java.util.Arrays.asList;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static objective.taskboard.utils.DateTimeUtils.parseStringToDate;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.data.Worklog;
import objective.taskboard.followup.kpi.enviroment.StatusTransitionBuilder;
import objective.taskboard.followup.kpi.enviroment.StatusTransitionBuilder.DefaultStatus;

public class SubtaskCycleEffortScenariosTest {

    private List<Worklog> worklogs = new LinkedList<>();
    
    private static final DefaultStatus TODO = new DefaultStatus("To Do",false); 
    private static final DefaultStatus DOING = new DefaultStatus("Doing",true); 
    private static final DefaultStatus TO_REVIEW = new DefaultStatus("To Review",false); 
    private static final DefaultStatus REVIEW = new DefaultStatus("Review",true); 
    private static final DefaultStatus DONE = new DefaultStatus("Done",false);
    
    @Before
    public void setUpAllWorklogs() {
        worklogs.add(new Worklog("a.developer", parseStringToDate("2020-01-01"), 100));
        worklogs.add(new Worklog("a.developer", parseStringToDate("2020-01-02"), 200));
        worklogs.add(new Worklog("a.developer", parseStringToDate("2020-01-03"), 300));
        worklogs.add(new Worklog("a.developer", parseStringToDate("2020-01-04"), 400));
        worklogs.add(new Worklog("a.developer", parseStringToDate("2020-01-05"), 500));
        worklogs.add(new Worklog("a.developer", parseStringToDate("2020-01-06"), 600));
    }
    
    @Test
    public void happyDay() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(TO_REVIEW,"2020-01-03")
                .addTransition(REVIEW,"2020-01-04")
                .addTransition(DONE,"2020-01-05");
        
        addWorklogs(builder.buildOrCry());
        
        builder.assertEffort(TODO, 0l);
        builder.assertEffort(DOING, 600l);
        builder.assertEffort(TO_REVIEW, 0l);
        builder.assertEffort(REVIEW, 1500l);
        builder.assertEffort(DONE, 0l);
    }
    
    @Test
    public void straightToClose() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING)
                .addTransition(TO_REVIEW)
                .addTransition(REVIEW)
                .addTransition(DONE,"2020-01-05");
        
        addWorklogs(builder.buildOrCry());
        
        builder.assertEffort(TODO, 0l);
        builder.assertEffort(DOING, 1000l);
        builder.assertEffort(TO_REVIEW, 0l);
        builder.assertEffort(REVIEW, 1100l);
        builder.assertEffort(DONE, 0l);
    }

    @Test
    public void skippingQueue() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(TO_REVIEW)
                .addTransition(REVIEW,"2020-01-03")
                .addTransition(DONE,"2020-01-05");
        
        addWorklogs(builder.buildOrCry());
        
        builder.assertEffort(TODO, 0l);
        builder.assertEffort(DOING, 300l);
        builder.assertEffort(TO_REVIEW, 0l);
        builder.assertEffort(REVIEW, 1800l);
        builder.assertEffort(DONE, 0l);
    }
    
    @Test
    public void openIssue() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING)
                .addTransition(TO_REVIEW)
                .addTransition(REVIEW)
                .addTransition(DONE);
        
        addWorklogs(builder.buildOrCry());
        
        builder.assertEffort(TODO, 0l);
        builder.assertEffort(DOING, 2100l);
        builder.assertEffort(TO_REVIEW, 0l);
        builder.assertEffort(REVIEW, 0l);
        builder.assertEffort(DONE, 0l);
    }
    
    @Test
    public void straightToReview() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-01")
                .addTransition(TO_REVIEW)
                .addTransition(REVIEW,"2020-01-01")
                .addTransition(DONE);
        
        addWorklogs(builder.buildOrCry());
        
        builder.assertEffort(TODO, 0l);
        builder.assertEffort(DOING, 0l);
        builder.assertEffort(TO_REVIEW, 0l);
        builder.assertEffort(REVIEW, 2100l);
        builder.assertEffort(DONE, 0l);
    }
    
    @Test
    public void issueClosed_doingAndReviewAtTheSameDay() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(TO_REVIEW)
                .addTransition(REVIEW,"2020-01-02")
                .addTransition(DONE,"2020-01-03");
        
        addWorklogs(builder.buildOrCry());
        
        builder.assertEffort(TODO, 0l);
        builder.assertEffort(DOING, 100l);
        builder.assertEffort(TO_REVIEW, 0l);
        builder.assertEffort(REVIEW, 2000l);
        builder.assertEffort(DONE, 0l);

    }
    
    @Test
    public void worklogBeforeOpen() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-02")
                .addTransition(DOING,"2020-01-03")
                .addTransition(TO_REVIEW,"2020-01-04")
                .addTransition(REVIEW,"2020-01-05")
                .addTransition(DONE,"2020-01-06");


        Worklog worklog = new Worklog("a.developer", parseStringToDate("2019-12-31"), 300);
        
        addWorklogs(builder.buildOrCry(), asList(worklog));
        
        builder.assertEffort(TODO,0l);
        builder.assertEffort(DOING,300l);
        builder.assertEffort(TO_REVIEW,0l);
        builder.assertEffort(REVIEW,0l);
        builder.assertEffort(DONE,0l);
    }

    @Test
    public void allStatusTransitedSameDay() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-02")
                .addTransition(DOING,"2020-01-02")
                .addTransition(TO_REVIEW)
                .addTransition(REVIEW,"2020-01-02")
                .addTransition(DONE,"2020-01-02");


        Worklog worklog1 = new Worklog("a.developer", parseStringToDate("2020-01-02"), 200);
        
        addWorklogs(builder.buildOrCry(), asList(worklog1));
        
        builder.assertEffort(TODO,0l);
        builder.assertEffort(DOING,0l);
        builder.assertEffort(TO_REVIEW,0l);
        builder.assertEffort(REVIEW,200l);
        builder.assertEffort(DONE,0l);
        
    }

    
    @Test
    public void whenWronglyConfigured_doesNotAddWorklog() {
       
        StatusTransition done = new DatedStatusTransition("Done", parseDateTime("2020-01-06"), false, Optional.empty());
        StatusTransition review = new DatedStatusTransition("Review", parseDateTime("2020-01-05"), false, Optional.of(done));
        StatusTransition toReview = new DatedStatusTransition("To Review", parseDateTime("2020-01-04"), false, Optional.of(review));
        StatusTransition doing = new DatedStatusTransition("Doing", parseDateTime("2020-01-03"), false, Optional.of(toReview));
        StatusTransition todo = new DatedStatusTransition("To Do", parseDateTime("2020-01-02"), false, Optional.of(doing));

        addWorklogs(todo);
        
        assertThat(todo.getEffort(),is(0l));
        assertThat(doing.getEffort(),is(0l));
        assertThat(toReview.getEffort(),is(0l));
        assertThat(review.getEffort(),is(0l));
        assertThat(done.getEffort(),is(0l));
    }
    
    private void addWorklogs(StatusTransition status) {
        addWorklogs(status, this.worklogs);
    }
    
    private void addWorklogs(StatusTransition status, List<Worklog> worklogs) {
        SubtaskWorklogDistributor distributor = new SubtaskWorklogDistributor();
        worklogs.stream().forEach(w -> distributor.findStatus(status, w).ifPresent(s -> s.addWorklog(w)));
    }
    
}
