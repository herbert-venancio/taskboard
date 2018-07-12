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
    
    private IssueStatusFlow makeIssue(String pKey, StatusTransitionChain firstStatus) {
        return new IssueStatusFlow(pKey,"Subtask", firstStatus);
        
    }

    private StatusTransitionChain makeStatus(String status, String date, StatusTransitionChain next) {
        return new StatusTransition(status, parseDateTime(date),next);
    }

}
