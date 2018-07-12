package objective.taskboard.followup;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Spy;

import objective.taskboard.followup.kpi.WipKPIService;
import objective.taskboard.jira.JiraProperties;

public class WipKpiServiceTest extends FollowUpDataGeneratorTestBase {

    private static final int SUBTASK_TRANSITIONS_DATASET_INDEX = 2;
    
    @Spy
    @InjectMocks
    private WipKPIService wipKpiService = new WipKPIService();
    
    @Before
    public void setupProperties() {
        
        JiraProperties.StatusCountingOnWip statusCountingOnWip = new JiraProperties.StatusCountingOnWip();
        statusCountingOnWip.setDemands(new String[] {"UATing", "To UAT", "Doing"});
        statusCountingOnWip.setTasks(new String[] {"QAing", "To QA", "Feature Reviewing", "To Feature Review",
                "Alpha Testing", "To Alpha Test", "Doing"});
        statusCountingOnWip.setSubtasks(new String[] {"Doing","To Review","Reviewing"});
        
        when(jiraProperties.getStatusCountingOnWip()).thenReturn(statusCountingOnWip);
        
    }
    
    @Test
    public void wipRows_eachDayShouldCountWipGivenIssueStatusOnThatDay() {
        issues(subtask().id(100).key("PROJ-100")
                .issueType(devIssueType)
                .transition("Open", "2020-01-01")
                .transition("To Do", "2020-01-02")
                .transition("Doing", "2020-01-03")
                .transition("To Review", "2020-01-04")
                .transition("Reviewing", "2020-01-05")
                .transition("Done", "2020-01-06")
                .transition("Cancelled", "2020-01-07")
                .issueStatus(statusCancelled));

        // when
        FollowUpData followupData = subject.generate(ZoneId.systemDefault(),DEFAULT_PROJECT);
        List<WipDataSet> wipDataSets = wipKpiService.getWipData(followupData);
        List<WipRow> rows = wipDataSets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        
        //then
        assertThat(rows.size(),is(21));

        assertRow(rows.get(0),parseDateTime("2020-01-01"),"Dev","Doing",0L);
        assertRow(rows.get(1),parseDateTime("2020-01-01"),"Dev","To Review",0L);
        assertRow(rows.get(2),parseDateTime("2020-01-01"),"Dev","Reviewing",0L);
        
        assertRow(rows.get(3),parseDateTime("2020-01-02"),"Dev","Doing",0L);
        assertRow(rows.get(4),parseDateTime("2020-01-02"),"Dev","To Review",0L);
        assertRow(rows.get(5),parseDateTime("2020-01-02"),"Dev","Reviewing",0L);
        
        assertRow(rows.get(6),parseDateTime("2020-01-03"),"Dev","Doing",1L);
        assertRow(rows.get(7),parseDateTime("2020-01-03"),"Dev","To Review",0L);
        assertRow(rows.get(8),parseDateTime("2020-01-03"),"Dev","Reviewing",0L);
        
        assertRow(rows.get(9),parseDateTime("2020-01-04"),"Dev","Doing",0L);
        assertRow(rows.get(10),parseDateTime("2020-01-04"),"Dev","To Review",1L);
        assertRow(rows.get(11),parseDateTime("2020-01-04"),"Dev","Reviewing",0L);
        
        assertRow(rows.get(12),parseDateTime("2020-01-05"),"Dev","Doing",0L);
        assertRow(rows.get(13),parseDateTime("2020-01-05"),"Dev","To Review",0L);
        assertRow(rows.get(14),parseDateTime("2020-01-05"),"Dev","Reviewing",1L);
        
        assertRow(rows.get(15),parseDateTime("2020-01-06"),"Dev","Doing",0L);
        assertRow(rows.get(16),parseDateTime("2020-01-06"),"Dev","To Review",0L);
        assertRow(rows.get(17),parseDateTime("2020-01-06"),"Dev","Reviewing",0L);
        
        assertRow(rows.get(18),parseDateTime("2020-01-07"),"Dev","Doing",0L);
        assertRow(rows.get(19),parseDateTime("2020-01-07"),"Dev","To Review",0L);
        assertRow(rows.get(20),parseDateTime("2020-01-07"),"Dev","Reviewing",0L);
        
    }

    @Test
    public void wipRows_countIssuesOnStatus_withoutIntermidiateTransition() {
        issues(subtask().id(100).key("PROJ-100")
                .issueType(devIssueType)
                .transition("Open", "2020-01-01")
                .transition("To Do", "2020-01-02")
                .transition("Doing", "2020-01-03")
                .transition("To Review", "2020-01-04")
                .transition("Cancelled", "2020-01-07")
                .issueStatus(statusCancelled));

        // when
        FollowUpData followupData = subject.generate(ZoneId.systemDefault(),DEFAULT_PROJECT);
        List<WipDataSet> wipDataSets = wipKpiService.getWipData(followupData);
        List<WipRow> rows = wipDataSets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        
        //then
        assertThat(rows.size(),is(21));

        assertRow(rows.get(0),parseDateTime("2020-01-01"),"Dev","Doing",0L);
        assertRow(rows.get(1),parseDateTime("2020-01-01"),"Dev","To Review",0L);
        assertRow(rows.get(2),parseDateTime("2020-01-01"),"Dev","Reviewing",0L);
        
        assertRow(rows.get(3),parseDateTime("2020-01-02"),"Dev","Doing",0L);
        assertRow(rows.get(4),parseDateTime("2020-01-02"),"Dev","To Review",0L);
        assertRow(rows.get(5),parseDateTime("2020-01-02"),"Dev","Reviewing",0L);
        
        assertRow(rows.get(6),parseDateTime("2020-01-03"),"Dev","Doing",1L);
        assertRow(rows.get(7),parseDateTime("2020-01-03"),"Dev","To Review",0L);
        assertRow(rows.get(8),parseDateTime("2020-01-03"),"Dev","Reviewing",0L);
        
        assertRow(rows.get(9),parseDateTime("2020-01-04"),"Dev","Doing",0L);
        assertRow(rows.get(10),parseDateTime("2020-01-04"),"Dev","To Review",1L);
        assertRow(rows.get(11),parseDateTime("2020-01-04"),"Dev","Reviewing",0L);
        
        assertRow(rows.get(12),parseDateTime("2020-01-05"),"Dev","Doing",0L);
        assertRow(rows.get(13),parseDateTime("2020-01-05"),"Dev","To Review",1L);
        assertRow(rows.get(14),parseDateTime("2020-01-05"),"Dev","Reviewing",0L);
        
        assertRow(rows.get(15),parseDateTime("2020-01-06"),"Dev","Doing",0L);
        assertRow(rows.get(16),parseDateTime("2020-01-06"),"Dev","To Review",1L);
        assertRow(rows.get(17),parseDateTime("2020-01-06"),"Dev","Reviewing",0L);
        
        assertRow(rows.get(18),parseDateTime("2020-01-07"),"Dev","Doing",0L);
        assertRow(rows.get(19),parseDateTime("2020-01-07"),"Dev","To Review",0L);
        assertRow(rows.get(20),parseDateTime("2020-01-07"),"Dev","Reviewing",0L);
        
    }

    @Test
    public void wipRows_allTransitionsSameDay_doesntCount() {
        issues(subtask().id(100).key("PROJ-100")
                .issueType(devIssueType)
                .transition("Open", "2020-01-01")
                .transition("To Do", "2020-01-01")
                .transition("Doing", "2020-01-01")
                .transition("To Review", "2020-01-01")
                .transition("Reviewing", "2020-01-01")
                .transition("Done", "2020-01-01")
                .issueStatus(statusDone));

        // when
        FollowUpData followupData = subject.generate(ZoneId.systemDefault(),DEFAULT_PROJECT);
        List<WipDataSet> wipDataSets = wipKpiService.getWipData(followupData);
        List<WipRow> rows = wipDataSets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        
        //then
        assertThat(rows.size(),is(3));

        assertRow(rows.get(0),parseDateTime("2020-01-01"),"Dev","Doing",0L);
        assertRow(rows.get(1),parseDateTime("2020-01-01"),"Dev","To Review",0L);
        assertRow(rows.get(2),parseDateTime("2020-01-01"),"Dev","Reviewing",0L);
    }
    
    @Test
    public void wipRows_differentTypes() {
        issues(subtask().id(100).key("PROJ-100")
                .issueType(devIssueType)
                    .transition("Open", "2020-01-01")
                    .transition("To Do", "2020-01-02")
                    .transition("Doing", "2020-01-02")
                    .transition("To Review", "2020-01-03")
                    .transition("Reviewing", "2020-01-03")
                    .transition("Done", "2020-01-04")
                    .issueStatus(statusDone),
               subtask().id(101).key("PROJ-101")
                .issueType(alphaIssueType)
                    .transition("Open", "2020-01-01")
                    .transition("To Do", "2020-01-02")
                    .transition("Doing", "2020-01-02")
                    .transition("Done", "2020-01-04")
                    .issueStatus(statusDone)
                    );

        // when
        FollowUpData followupData = subject.generate(ZoneId.systemDefault(),DEFAULT_PROJECT);
        List<WipDataSet> wipDataSets = wipKpiService.getWipData(followupData);
        List<WipRow> rows = wipDataSets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        
        //then
        assertThat(rows.size(),is(24));

        assertRow(rows.get(0),parseDateTime("2020-01-01"),"Alpha","Doing",0L);
        assertRow(rows.get(1),parseDateTime("2020-01-01"),"Dev","Doing",0L);
        assertRow(rows.get(2),parseDateTime("2020-01-01"),"Alpha","To Review",0L);
        assertRow(rows.get(3),parseDateTime("2020-01-01"),"Dev","To Review",0L);
        assertRow(rows.get(4),parseDateTime("2020-01-01"),"Alpha","Reviewing",0L);
        assertRow(rows.get(5),parseDateTime("2020-01-01"),"Dev","Reviewing",0L);
        
        assertRow(rows.get(6),parseDateTime("2020-01-02"),"Alpha","Doing",1L);
        assertRow(rows.get(7),parseDateTime("2020-01-02"),"Dev","Doing",1L);
        assertRow(rows.get(8),parseDateTime("2020-01-02"),"Alpha","To Review",0L);
        assertRow(rows.get(9),parseDateTime("2020-01-02"),"Dev","To Review",0L);
        assertRow(rows.get(10),parseDateTime("2020-01-02"),"Alpha","Reviewing",0L);
        assertRow(rows.get(11),parseDateTime("2020-01-02"),"Dev","Reviewing",0L);
        
        assertRow(rows.get(12),parseDateTime("2020-01-03"),"Alpha","Doing",1L);
        assertRow(rows.get(13),parseDateTime("2020-01-03"),"Dev","Doing",0L);
        assertRow(rows.get(14),parseDateTime("2020-01-03"),"Alpha","To Review",0L);
        assertRow(rows.get(15),parseDateTime("2020-01-03"),"Dev","To Review",0L);
        assertRow(rows.get(16),parseDateTime("2020-01-03"),"Alpha","Reviewing",0L);
        assertRow(rows.get(17),parseDateTime("2020-01-03"),"Dev","Reviewing",1L);
        
        assertRow(rows.get(18),parseDateTime("2020-01-04"),"Alpha","Doing",0L);
        assertRow(rows.get(19),parseDateTime("2020-01-04"),"Dev","Doing",0L);
        assertRow(rows.get(20),parseDateTime("2020-01-04"),"Alpha","To Review",0L);
        assertRow(rows.get(21),parseDateTime("2020-01-04"),"Dev","To Review",0L);
        assertRow(rows.get(22),parseDateTime("2020-01-04"),"Alpha","Reviewing",0L);
        assertRow(rows.get(23),parseDateTime("2020-01-04"),"Dev","Reviewing",0L);
        
    }

    private void assertRow(WipRow wipRow, ZonedDateTime date, String type, String status, Long count) {
        assertThat(wipRow.date,is(date));
        assertThat(wipRow.type,is(type));
        assertThat(wipRow.status,is(status));
        assertThat(wipRow.count,is(count));
    }
    
}
