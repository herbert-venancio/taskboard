package objective.taskboard.followup.kpi.transformer;

import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import objective.taskboard.followup.kpi.IssueKpi;

public class IssueKpiTransformerTest {
    
    private static final long STATUS_DOING = 3l;
    private static final long STATUS_DONE = 4l;
    private static final String[] STATUSES = new String[] {"Done","Doing","To Do","Open"};

        
    @Test
    public void transformIssues_happyDay() {
        IssueKpiDataItemAdapter i1 = new IssueMockBuilder(STATUSES)
                .withKey("I-1")
                .withProjectKey("PROJ")
                .withType("Dev")
                .withLevel(SUBTASKS)
                .withStatusId(STATUS_DOING)
                .addTransition("Open", "2020-01-01")
                .addTransition("To Do", "2020-01-02")
                .addTransition("Doing", "2020-01-03")
                .buildIssueKPI();
        IssueKpiDataItemAdapter i2 = new IssueMockBuilder(STATUSES)
                .withKey("I-2")
                .withProjectKey("PROJ")
                .withType("Alpha")
                .withLevel(SUBTASKS)
                .withStatusId(STATUS_DONE)
                .addTransition("Open", "2020-01-01")
                .addTransition("To Do", "2020-01-02")
                .addTransition("Doing", "2020-01-03")
                .addTransition("Done", "2020-01-04")
                .buildIssueKPI();
        
        List<IssueKpi> issuesKpi = new IssueKpiTransformer(Arrays.asList(i1,i2)).transform();
        assertThat(issuesKpi.size(),is(2));
        
        IssueKpi kpi1 = issuesKpi.get(0);
        
        assertThat(kpi1.getIssueKey(),is("I-1"));
        assertThat(kpi1.getIssueType(),is("Dev"));
        assertThat(kpi1.getLevel(),is(SUBTASKS));
        
        assertTrue(kpi1.isOnStatusOnDay("Open", parseDateTime("2020-01-01")));
        assertFalse(kpi1.isOnStatusOnDay("To Do", parseDateTime("2020-01-01")));
        assertFalse(kpi1.isOnStatusOnDay("Doing", parseDateTime("2020-01-01")));
        assertFalse(kpi1.isOnStatusOnDay("Done", parseDateTime("2020-01-01")));
        
        assertFalse(kpi1.isOnStatusOnDay("Open", parseDateTime("2020-01-04")));
        assertFalse(kpi1.isOnStatusOnDay("To Do", parseDateTime("2020-01-04")));
        assertTrue(kpi1.isOnStatusOnDay("Doing", parseDateTime("2020-01-04")));
        assertFalse(kpi1.isOnStatusOnDay("Done", parseDateTime("2020-01-04")));
        
        
        IssueKpi kpi2 = issuesKpi.get(1);
        
        assertThat(kpi2.getIssueKey(),is("I-2"));
        assertThat(kpi2.getIssueType(),is("Alpha"));
        assertThat(kpi2.getLevel(),is(SUBTASKS));
        
        assertFalse(kpi2.isOnStatusOnDay("Open", parseDateTime("2020-01-04")));
        assertFalse(kpi2.isOnStatusOnDay("To Do", parseDateTime("2020-01-04")));
        assertFalse(kpi2.isOnStatusOnDay("Doing", parseDateTime("2020-01-04")));
        assertTrue(kpi2.isOnStatusOnDay("Done", parseDateTime("2020-01-04")));
    }

    
    
    
}
