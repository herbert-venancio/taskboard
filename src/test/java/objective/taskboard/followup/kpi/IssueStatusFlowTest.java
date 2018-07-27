package objective.taskboard.followup.kpi;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class IssueStatusFlowTest {
    
    @Test
    public void checkStatusOnDay_happyDay() {
        
        StatusTransitionChain done = makeStatus("Done","2020-01-03",new TerminalStateTransition());
        StatusTransitionChain doing = makeStatus("Doing","2020-01-02",done);
        StatusTransitionChain todo = makeStatus("To Do","2020-01-01",doing);
        IssueStatusFlow issue = makeIssue("PROJ-01",todo);
        
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
        StatusTransitionChain done = makeStatus("Done","2020-01-03",new TerminalStateTransition());
        StatusTransitionChain doing = new NoDateStatusTransition("Doing",done);
        StatusTransitionChain todo = makeStatus("To Do","2020-01-01",doing);
        IssueStatusFlow issue = makeIssue("PROJ-01",todo);
        
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
        StatusTransitionChain todo = makeStatus("To Do","2020-01-01",new TerminalStateTransition());
        IssueStatusFlow issue = makeIssue("PROJ-01",todo);
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-01")), is(true));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-01")), is(false));
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-02")), is(true));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-02")), is(false));
    }
    
    @Test
    public void checkStatusOnDay_futureIssue() {
        StatusTransitionChain done = makeStatus("Done","2020-01-04",new TerminalStateTransition());
        StatusTransitionChain doing = makeStatus("Doing","2020-01-03",done);
        StatusTransitionChain todo = makeStatus("To Do","2020-01-02",doing);
        IssueStatusFlow issue = makeIssue("PROJ-01",todo);
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-01")), is(false));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-01")), is(false));
        assertThat(issue.isOnStatusOnDay("Done", parseDateTime("2020-01-01")), is(false));
        
        assertThat(issue.isOnStatusOnDay("To Do", parseDateTime("2020-01-02")), is(true));
        assertThat(issue.isOnStatusOnDay("Doing", parseDateTime("2020-01-02")), is(false));
        assertThat(issue.isOnStatusOnDay("Done", parseDateTime("2020-01-02")), is(false));
        
    }
    
    @Test
    public void hasTransitedToStatus_happyDay() {
        StatusTransitionChain done = makeStatus("Done","2020-01-03",new TerminalStateTransition());
        StatusTransitionChain doing = makeStatus("Doing","2020-01-02",done);
        StatusTransitionChain todo = makeStatus("To Do","2020-01-01",doing);
        IssueStatusFlow issue = makeIssue("PROJ-01",todo);
        
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-01"), "Done"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-02"), "Done"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-03"), "Done"),is (true));
    }
    
    @Test
    public void hasTransitedToStatus_nonExistentStatus() {
        StatusTransitionChain done = makeStatus("Done","2020-01-03",new TerminalStateTransition());
        StatusTransitionChain doing = makeStatus("Doing","2020-01-02",done);
        StatusTransitionChain todo = makeStatus("To Do","2020-01-01",doing);
        IssueStatusFlow issue = makeIssue("PROJ-01",todo);
        
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-01"), "Integrating","Cancelled"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-02"), "Integrating","Cancelled"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-03"), "Integrating","Cancelled"),is (false));
    }
    
    @Test
    public void hasTransitedToStatus_onlyOneStatusTransited() {
        StatusTransitionChain done = makeStatus("Done","2020-01-03",new TerminalStateTransition());
        StatusTransitionChain doing = new NoDateStatusTransition("Doing",done);
        StatusTransitionChain todo = makeStatus("To Do","2020-01-01",doing);
        IssueStatusFlow issue = makeIssue("PROJ-01",todo);
        
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-01"), "Doing","Done"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-02"), "Doing","Done"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-03"), "Doing","Done"),is (true));
    }
    
    @Test
    public void hasTransitedToStatus_onlyNotTransited() {
        StatusTransitionChain done = makeStatus("Done","2020-01-03",new TerminalStateTransition());
        StatusTransitionChain doing = new NoDateStatusTransition("Doing",done);
        StatusTransitionChain todo = makeStatus("To Do","2020-01-01",doing);
        IssueStatusFlow issue = makeIssue("PROJ-01",todo);
        
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-01"), "Doing"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-02"), "Doing"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-03"), "Doing"),is (false));
    }
    
    @Test
    public void hasTransitedToStatus_earliestTransition() {
        StatusTransitionChain done = makeStatus("Done","2020-01-03",new TerminalStateTransition());
        StatusTransitionChain doing = makeStatus("Doing","2020-01-02",done);
        StatusTransitionChain todo = makeStatus("To Do","2020-01-01",doing);
        IssueStatusFlow issue = makeIssue("PROJ-01",todo);
        
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-01"), "Doing","Done"),is (false));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-02"), "Doing","Done"),is (true));
        assertThat(issue.hasTransitedToAnyStatusOnDay(parseDateTime("2020-01-03"), "Doing","Done"),is (false));
    }
    
   
    private IssueStatusFlow makeIssue(String pKey, StatusTransitionChain firstStatus) {
        return new IssueStatusFlow(pKey,"Subtask", firstStatus);
        
    }

    private StatusTransitionChain makeStatus(String status, String date, StatusTransitionChain next) {
        return new StatusTransition(status, parseDateTime(date),next);
    }

}
