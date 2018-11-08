package objective.taskboard.followup;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.mockito.Mockito;

import objective.taskboard.data.Changelog;
import objective.taskboard.data.Issue;

public class IssueTransitionServiceTest {
    
    
    private static ZoneId ZONE_ID = ZoneId.systemDefault();
    private static String[] STATUS_ORDER = new String[] {"Cancelled","Done","Doing","To Do","Open"};
    
    private IssueTransitionService subject = new IssueTransitionService();

    @Test
    public void getTransitions_happyDay() {
        
        Issue issue = new IssueMock(parseDateTime("2018-01-01"),"Cancelled")
                    .addStatusChangeLog("2018-01-02", "Open", "To Do")
                    .addStatusChangeLog("2018-01-03", "To Do", "Doing")
                    .addStatusChangeLog("2018-01-04", "Doing", "Done")
                    .addStatusChangeLog("2018-01-05", "Doing", "Cancelled")
                    .mock();

        Map<String, ZonedDateTime> transitions = subject.getTransitions(issue, ZONE_ID, STATUS_ORDER);
        
        withTransitions(transitions)
            .assertSize(5)
            .assertDate("Open","2018-01-01")
            .assertDate("To Do","2018-01-02")
            .assertDate("Doing","2018-01-03")
            .assertDate("Done","2018-01-04")
            .assertDate("Cancelled","2018-01-05");
    }
    
    @Test
    public void getTransitions_openIssue() {
        
        Issue issue = new IssueMock(parseDateTime("2018-01-01"),"Open").mock();

        Map<String, ZonedDateTime> transitions = subject.getTransitions(issue, ZONE_ID, STATUS_ORDER);
        
        withTransitions(transitions)
            .assertSize(5)
            .assertDate("Open","2018-01-01")
            .assertNullDate("To Do")
            .assertNullDate("Doing")
            .assertNullDate("Done")
            .assertNullDate("Cancelled");
    }
    
    @Test
    public void getTransitions_withExtraChangeLogs() {
        
        Issue issue = new IssueMock(parseDateTime("2018-01-01"),"Cancelled")
                    .addStatusChangeLog("2018-01-02", "Open", "To Do")
                    .addStatusChangeLog("2018-01-03", "To Do", "Doing")
                    .addStatusChangeLog("2018-01-04", "Doing", "Done")
                    .addStatusChangeLog("2018-01-05", "Doing", "Cancelled")
                    .addChangelog("Description", "2018-01-02", "", "a new description")
                    .mock();

        Map<String, ZonedDateTime> transitions = subject.getTransitions(issue, ZONE_ID, STATUS_ORDER);
        
        withTransitions(transitions)
            .assertSize(5)
            .assertDate("Open","2018-01-01")
            .assertDate("To Do","2018-01-02")
            .assertDate("Doing","2018-01-03")
            .assertDate("Done","2018-01-04")
            .assertDate("Cancelled","2018-01-05");
    }
    
    @Test
    public void getTransitions_severalTransitions_sameDay() {
        
        Issue issue = new IssueMock(parseDateTime("2018-01-01"),"Cancelled")
                    .addStatusChangeLog("2018-01-02", "Open", "To Do")
                    .addStatusChangeLog("2018-01-02", "To Do", "Doing")
                    .addStatusChangeLog("2018-01-02", "Doing", "Done")
                    .addStatusChangeLog("2018-01-03", "Doing", "Cancelled")
                    .mock();

        Map<String, ZonedDateTime> transitions = subject.getTransitions(issue, ZONE_ID, STATUS_ORDER);
        
        withTransitions(transitions)
            .assertSize(5)
            .assertDate("Open","2018-01-01")
            .assertDate("To Do","2018-01-02")
            .assertDate("Doing","2018-01-02")
            .assertDate("Done","2018-01-02")
            .assertDate("Cancelled","2018-01-03");
    }
            
    @Test
    public void getTransitions_issueGoneBackAndForth() {
        
        Issue issue = new IssueMock(parseDateTime("2018-01-01"),"Cancelled")
                .addStatusChangeLog("2018-01-02", "Open", "To Do")
                .addStatusChangeLog("2018-01-03", "To Do", "Open")
                .addStatusChangeLog("2018-01-04", "Open", "To Do")
                .addStatusChangeLog("2018-01-05", "To Do", "Doing")
                .addStatusChangeLog("2018-01-06", "Doing", "Cancelled")
                .mock();

        Map<String, ZonedDateTime> transitions = subject.getTransitions(issue, ZONE_ID, STATUS_ORDER);
        
        withTransitions(transitions)
            .assertSize(5)
            .assertDate("Open","2018-01-03")
            .assertDate("To Do","2018-01-04")
            .assertDate("Doing","2018-01-05")
            .assertNullDate("Done")
            .assertDate("Cancelled","2018-01-06");
    }
    
    @Test
    public void getTransitions_issueGoneForthAndBack_shouldNotReturnTransitionBeforeTheReturn() {
        
        Issue issue = new IssueMock(parseDateTime("2018-01-01"),"To Do")
                .addStatusChangeLog("2018-01-02", "Open", "To Do")
                .addStatusChangeLog("2018-01-03", "To Do", "Open")
                .addStatusChangeLog("2018-01-04", "Open", "To Do")
                .addStatusChangeLog("2018-01-05", "To Do", "Doing")
                .addStatusChangeLog("2018-01-06", "Doing", "To Do")
                .mock();

        Map<String, ZonedDateTime> transitions = subject.getTransitions(issue, ZONE_ID, STATUS_ORDER);
        
        withTransitions(transitions)
            .assertSize(5)
            .assertDate("Open","2018-01-03")
            .assertDate("To Do","2018-01-06")
            .assertNullDate("Doing")
            .assertNullDate("Done")
            .assertNullDate("Cancelled");
    }
    
    @Test
    public void getTransitions_emptyStatuses() {
        
        Issue issue = new IssueMock(parseDateTime("2018-01-01"),"Cancelled")
                .addStatusChangeLog("2018-01-02", "Open", "To Do")
                .addStatusChangeLog("2018-01-03", "To Do", "Doing")
                .addStatusChangeLog("2018-01-04", "Doing", "Done")
                .addStatusChangeLog("2018-01-05", "Done", "Cancelled")
                .mock();

        Map<String, ZonedDateTime> transitions = subject.getTransitions(issue, ZONE_ID, new String[] {});
        
        withTransitions(transitions)
            .assertSize(0);
            
    }
    
    private TransitionAsserter withTransitions(Map<String, ZonedDateTime> transitions) {
        return new TransitionAsserter(transitions);
    }
    
    private class IssueMock {
        
        private ZonedDateTime created;
        private String statusName;
        private List<Changelog> changelogs = new LinkedList<>();
                
        private IssueMock(ZonedDateTime created, String statusName) {
            this.created = created;
            this.statusName = statusName;
        }
        
        private IssueMock addStatusChangeLog(String date, String statusFrom, String statusTo) {
            addChangelog("status",date,statusFrom,statusTo);
            return this;
        }
        
        private IssueMock addChangelog(String field, String date, String stringValueFrom, String stringValueTo) {
            changelogs.add(new Changelog("a.developer", field, stringValueFrom, stringValueTo, "42", parseDateTime(date)));
            return this;
        }
        
        private Issue mock() {
            Issue issue = Mockito.mock(Issue.class);
            when(issue.getCreated()).thenReturn(created.toInstant().toEpochMilli());
            when(issue.getStatusName()).thenReturn(statusName);
            when(issue.getChangelog()).thenReturn(changelogs);
            return issue;
        }
        
    }
    
    private class TransitionAsserter {
        private Map<String,ZonedDateTime> transitions;

        private TransitionAsserter(Map<String, ZonedDateTime> transitions) {
            this.transitions = transitions;
        }
        
        public TransitionAsserter assertNullDate(String status) {
            String message = String.format("Transitions should have an empty value to status %s.", status);
            assertTrue(message, transitions.containsKey(status));
            assertNull(message,transitions.get(status));
            return this;
        }

        private TransitionAsserter assertSize(int i) {
            assertThat(transitions.size(),is(i));
            return this;
        }

        private TransitionAsserter assertDate(String status, String date) {
            assertThat(transitions.get(status),is(parseDateTime(date)));
            return this;
        }
        
    }

}
