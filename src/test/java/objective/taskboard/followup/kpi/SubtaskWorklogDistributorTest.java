package objective.taskboard.followup.kpi;

import static objective.taskboard.utils.DateTimeUtils.parseStringToDate;
import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;

import objective.taskboard.data.Worklog;
import objective.taskboard.followup.kpi.enviroment.StatusTransitionBuilder;
import objective.taskboard.followup.kpi.enviroment.StatusTransitionBuilder.DefaultStatus;

public class SubtaskWorklogDistributorTest {

    private static final Worklog DAY_ONE = new Worklog("a.developer", parseStringToDate("2020-01-01"), 100);
    private static final Worklog DAY_TWO = new Worklog("a.developer", parseStringToDate("2020-01-02"), 200);
    private static final Worklog DAY_THREE = new Worklog("a.developer", parseStringToDate("2020-01-03"), 300);
    private static final Worklog DAY_FOUR = new Worklog("a.developer", parseStringToDate("2020-01-04"), 400);
    private static final Worklog DAY_FIVE = new Worklog("a.developer", parseStringToDate("2020-01-05"), 500);
    private static final Worklog DAY_SIX = new Worklog("a.developer", parseStringToDate("2020-01-06"), 600);
    
    private static final DefaultStatus TODO = new DefaultStatus("To Do",false); 
    private static final DefaultStatus DOING = new DefaultStatus("Doing",true); 
    private static final DefaultStatus TO_REVIEW = new DefaultStatus("To Review",false); 
    private static final DefaultStatus REVIEW = new DefaultStatus("Review",true); 
    private static final DefaultStatus DONE = new DefaultStatus("Done",false);
    
    private SubtaskWorklogDistributor subject = new SubtaskWorklogDistributor();
    
    @Test
    public void happyDay() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(TO_REVIEW,"2020-01-03")
                .addTransition(REVIEW,"2020-01-04")
                .addTransition(DONE,"2020-01-05");
        
        StatusTransition status = builder.buildOrCry();
        
        assertStatus(subject.findStatus(status,DAY_ONE),DOING);
        assertStatus(subject.findStatus(status,DAY_TWO),DOING);
        assertStatus(subject.findStatus(status,DAY_THREE),DOING);
        assertStatus(subject.findStatus(status,DAY_FOUR),REVIEW);
        assertStatus(subject.findStatus(status,DAY_FIVE),REVIEW);
        assertStatus(subject.findStatus(status,DAY_SIX),REVIEW);
    }
    
    @Test
    public void straightToClose() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING)
                .addTransition(TO_REVIEW)
                .addTransition(REVIEW)
                .addTransition(DONE,"2020-01-05");
        
        StatusTransition status = builder.buildOrCry();
        
        assertStatus(subject.findStatus(status,DAY_ONE),DOING);
        assertStatus(subject.findStatus(status,DAY_TWO),DOING);
        assertStatus(subject.findStatus(status,DAY_THREE),DOING);
        assertStatus(subject.findStatus(status,DAY_FOUR),DOING);
        assertStatus(subject.findStatus(status,DAY_FIVE),REVIEW);
        assertStatus(subject.findStatus(status,DAY_SIX),REVIEW);
    }
    
    @Test
    public void skippingQueue() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(TO_REVIEW)
                .addTransition(REVIEW,"2020-01-03")
                .addTransition(DONE,"2020-01-05");
        
        StatusTransition status = builder.buildOrCry();
        
        assertStatus(subject.findStatus(status,DAY_ONE),DOING);
        assertStatus(subject.findStatus(status,DAY_TWO),DOING);
        assertStatus(subject.findStatus(status,DAY_THREE),REVIEW);
        assertStatus(subject.findStatus(status,DAY_FOUR),REVIEW);
        assertStatus(subject.findStatus(status,DAY_FIVE),REVIEW);
        assertStatus(subject.findStatus(status,DAY_SIX),REVIEW);
        
    }
    
    @Test
    public void openIssue() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING)
                .addTransition(TO_REVIEW)
                .addTransition(REVIEW)
                .addTransition(DONE);
        
        StatusTransition status = builder.buildOrCry();
                
        assertStatus(subject.findStatus(status,DAY_ONE),DOING);
        assertStatus(subject.findStatus(status,DAY_TWO),DOING);
        assertStatus(subject.findStatus(status,DAY_THREE),DOING);
        assertStatus(subject.findStatus(status,DAY_FOUR),DOING);
        assertStatus(subject.findStatus(status,DAY_FIVE),DOING);
        assertStatus(subject.findStatus(status,DAY_SIX),DOING);
    }
    
    @Test
    public void straightToReview() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-01")
                .addTransition(TO_REVIEW)
                .addTransition(REVIEW,"2020-01-01")
                .addTransition(DONE);
        
        StatusTransition status = builder.buildOrCry();
        
        assertStatus(subject.findStatus(status,DAY_ONE),REVIEW);
        assertStatus(subject.findStatus(status,DAY_TWO),REVIEW);
        assertStatus(subject.findStatus(status,DAY_THREE),REVIEW);
        assertStatus(subject.findStatus(status,DAY_FOUR),REVIEW);
        assertStatus(subject.findStatus(status,DAY_FIVE),REVIEW);
        assertStatus(subject.findStatus(status,DAY_SIX),REVIEW);
    }
    
    @Test
    public void issueClosed_doingAndReviewAtTheSameDay() {
        StatusTransitionBuilder builder = new StatusTransitionBuilder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(TO_REVIEW)
                .addTransition(REVIEW,"2020-01-02")
                .addTransition(DONE,"2020-01-03");
        
        StatusTransition status = builder.buildOrCry();
        
        assertStatus(subject.findStatus(status,DAY_ONE),DOING);
        assertStatus(subject.findStatus(status,DAY_TWO),REVIEW);
        assertStatus(subject.findStatus(status,DAY_THREE),REVIEW);
        assertStatus(subject.findStatus(status,DAY_FOUR),REVIEW);
        assertStatus(subject.findStatus(status,DAY_FIVE),REVIEW);
        assertStatus(subject.findStatus(status,DAY_SIX),REVIEW);

    }
    
    private void assertStatus(Optional<StatusTransition> statusTransition, DefaultStatus status) {
        String foundStatus = statusTransition.map(s -> s.status).orElse("Empty");
        assertEquals(status.name,foundStatus);
    }
    
    
}
