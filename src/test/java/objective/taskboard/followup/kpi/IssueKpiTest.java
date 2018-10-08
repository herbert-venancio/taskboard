package objective.taskboard.followup.kpi;

import static objective.taskboard.followup.kpi.StatusTransitionBuilder.DefaultStatus.DOING;
import static objective.taskboard.followup.kpi.StatusTransitionBuilder.DefaultStatus.DONE;
import static objective.taskboard.followup.kpi.StatusTransitionBuilder.DefaultStatus.TODO;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class IssueKpiTest {
    
    @Test
    public void checkStatusOnDay_happyDay() {
        
        IssueKpi issue = builder()
                            .addTransition(TODO,"2020-01-01")
                            .addTransition(DOING,"2020-01-02")
                            .addTransition(DONE,"2020-01-03")
                            .build();
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-01")), is(true));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-01")), is(false));
        assertThat(issue.isOnStatusOnDay("Done", parseDateTime("2020-01-01")), is(false));
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-02")), is(false));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-02")), is(true));
        assertThat(issue.isOnStatusOnDay("Done", parseDateTime("2020-01-02")), is(false));
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-03")), is(false));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-03")), is(false));
        assertThat(issue.isOnStatusOnDay("Done", parseDateTime("2020-01-03")), is(true));
        
        assertThat(issue.isOnStatusOnDay("Review", parseDateTime("2020-01-01")), is(false));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-04")), is(false));
    }

    
    
    @Test
    public void checkStatusOnDay_emptyTransitions() {
        
        IssueKpi issue = builder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING)
                .addTransition(DONE,"2020-01-03")
                .build();
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-01")), is(true));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-01")), is(false));
        assertThat(issue.isOnStatusOnDay("Done", parseDateTime("2020-01-01")), is(false));
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-02")), is(true));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-02")), is(false));
        assertThat(issue.isOnStatusOnDay("Done", parseDateTime("2020-01-02")), is(false));
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-03")), is(false));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-03")), is(false));
        assertThat(issue.isOnStatusOnDay("Done", parseDateTime("2020-01-03")), is(true));
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-04")), is(false));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-04")), is(false));
        assertThat(issue.isOnStatusOnDay("Done", parseDateTime("2020-01-04")), is(true));
        
    }
    
    @Test
    public void checkStatusOnDay_openIssue() {
        
        IssueKpi issue = builder()
                .addTransition(TODO,"2020-01-01")
                .build();
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-01")), is(true));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-01")), is(false));
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-02")), is(true));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-02")), is(false));
    }
    
    @Test
    public void checkStatusOnDay_futureIssue() {
        
        IssueKpi issue = builder()
                .addTransition(TODO,"2020-01-02")
                .addTransition(DOING,"2020-01-03")
                .addTransition(DONE,"2020-01-04")
                .build();
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-01")), is(false));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-01")), is(false));
        assertThat(issue.isOnStatusOnDay("Done", parseDateTime("2020-01-01")), is(false));
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-02")), is(true));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-02")), is(false));
        assertThat(issue.isOnStatusOnDay("Done", parseDateTime("2020-01-02")), is(false));
        
    }
    
    @Test
    public void hasTransitedToStatus_happyDay() {
        IssueKpi issue = builder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(DONE,"2020-01-03")
                .build();
        
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-01"), "Done"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-02"), "Done"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-03"), "Done"),is (true));
    }
    
    @Test
    public void hasTransitedToStatus_nonExistentStatus() {
        IssueKpi issue = builder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(DONE,"2020-01-03")
                .build();
        
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-01"), "Integrating","Cancelled"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-02"), "Integrating","Cancelled"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-03"), "Integrating","Cancelled"),is (false));
    }

    @Test
    public void hasTransitedToStatus_onlyOneStatusTransited() {
        IssueKpi issue = builder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING)
                .addTransition(DONE,"2020-01-03")
                .build();
        
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-01"), "Doing","Done"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-02"), "Doing","Done"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-03"), "Doing","Done"),is (true));
    }
    
    @Test
    public void hasTransitedToStatus_onlyNotTransited() {
        IssueKpi issue = builder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING)
                .addTransition(DONE,"2020-01-03")
                .build();
        
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-01"), "Doing"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-02"), "Doing"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-03"), "Doing"),is (false));
    }
    
    @Test
    public void hasTransitedToStatus_earliestTransition() {
        
        IssueKpi issue = builder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(DONE,"2020-01-03")
                .build();
        
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-01"), "Doing","Done"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-02"), "Doing","Done"),is (true));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-03"), "Doing","Done"),is (false));
    }
    
    private IssueKpiBuilder builder() {
        return new IssueKpiBuilder("PROJ-01", "Subtask", KpiLevel.SUBTASKS);
    }
}
