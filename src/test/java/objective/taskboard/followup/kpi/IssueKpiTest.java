package objective.taskboard.followup.kpi;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import objective.taskboard.data.Worklog;
import objective.taskboard.followup.kpi.enviroment.IssueKpiBuilder;
import objective.taskboard.followup.kpi.enviroment.StatusTransitionBuilder.DefaultStatus;
import objective.taskboard.utils.DateTimeUtils;

public class IssueKpiTest {
    
    private static DefaultStatus TODO = new DefaultStatus("To Do",false);
    private static DefaultStatus DOING = new DefaultStatus("Doing",true);
    private static DefaultStatus DONE = new DefaultStatus("Done",false);
    
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
    
    @Test
    public void getWorklogFromChildren() {
        Worklog worklog = new Worklog("a.developer", DateTimeUtils.parseStringToDate("2020-010-2"), 300);
        
        IssueKpi issue = builder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(DONE,"2020-01-03")
                .addWorklog(worklog)
                .build();
        
        IssueKpi fatherIssue = new IssueKpiBuilder("PROJ-02", new IssueTypeKpi(2l,"Feature"), KpiLevel.FEATURES )
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(DONE,"2020-01-03")
                .addChild(issue)
                .build();
        List<Worklog> childrenWorklog = fatherIssue.getWorklogFromChildren(1l);
        
        assertThat(childrenWorklog.size(),is(1));
        assertThat(childrenWorklog.get(0),is(worklog));
        assertThat(issue.getEffort("Doing"),is(300l));
    }
    
    @Test
    public void wrongConfiguration_dontGetWorklogFromChildren() {
        Worklog worklog = new Worklog("a.developer", DateTimeUtils.parseStringToDate("2020-01-02"), 300);
        
        IssueKpi issue = new IssueKpiBuilder("PROJ-01", null, KpiLevel.SUBTASKS)
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(DONE,"2020-01-03")
                .addWorklog(worklog)
                .build();
        
        IssueKpi fatherIssue = new IssueKpiBuilder("PROJ-02", new IssueTypeKpi(2l,"Feature"), KpiLevel.FEATURES )
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(DONE,"2020-01-03")
                .addChild(issue)
                .build();
        List<Worklog> childrenWorklog = fatherIssue.getWorklogFromChildren(1l);
        
        assertThat(childrenWorklog.size(),is(0));
    }
    
    @Test
    public void getWorklogFromChildrenStatus_happyDay() {
        Worklog worklog = new Worklog("a.developer", DateTimeUtils.parseStringToDate("2020-01-02"), 300);
        
        IssueKpi issue = builder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(DONE,"2020-01-03")
                .addWorklog(worklog)
                .build();
        
        IssueKpi fatherIssue = new IssueKpiBuilder("PROJ-02", new IssueTypeKpi(2l,"Feature"), KpiLevel.FEATURES )
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(DONE,"2020-01-03")
                .addChild(issue)
                .build();
        List<Worklog> childrenWorklog = fatherIssue.getWorklogFromChildrenStatus("To Do");
        assertThat(childrenWorklog.size(),is(0));
        
        childrenWorklog = fatherIssue.getWorklogFromChildrenStatus("Doing");
        assertThat(childrenWorklog.size(),is(1));
        assertThat(childrenWorklog.get(0),is(worklog));
        assertThat(issue.getEffort("Doing"),is(300l));
        
    }
    
    @Test
    public void wrongConfiguration_dontGetWorklogFromChildrenStatus() {
        Worklog worklog = new Worklog("a.developer", DateTimeUtils.parseStringToDate("2020-01-02"), 300);
        
        IssueKpi issue = builder()
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(DONE,"2020-01-03")
                .addWorklog(worklog)
                .build();
        
        IssueKpi fatherIssue = new IssueKpiBuilder("PROJ-02", new IssueTypeKpi(2l,"Feature"), KpiLevel.FEATURES )
                .addTransition(TODO,"2020-01-01")
                .addTransition(DOING,"2020-01-02")
                .addTransition(DONE,"2020-01-03")
                .addChild(issue)
                .build();
        List<Worklog> childrenWorklog = fatherIssue.getWorklogFromChildrenStatus("To Do");
        assertThat(childrenWorklog.size(),is(0));
        
        childrenWorklog = fatherIssue.getWorklogFromChildrenStatus("Inexistent");
        assertThat(childrenWorklog.size(),is(0));
                
    }
    
    private IssueKpiBuilder builder() {
        return new IssueKpiBuilder("PROJ-01", new IssueTypeKpi(1l,"Subtask"), KpiLevel.SUBTASKS);
    }
}
