package objective.taskboard.followup;

import static objective.taskboard.followup.FollowUpHelper.getDefaultFollowupData;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.kpi.IssueStatusFlow;
import objective.taskboard.followup.kpi.IssueStatusFlowBuilder;
import objective.taskboard.followup.kpi.IssueStatusFlowService;
import objective.taskboard.followup.kpi.ThroughputKPIService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.StatusConfiguration.FinalStatuses;

@RunWith(MockitoJUnitRunner.class)
public class ThroughputKPIServiceTest {
 
    private static final int DEMAND_TRANSITIONS_DATASET_INDEX = 0;
    private static final int FEATURES_TRANSITIONS_DATASET_INDEX = 1;
    private static final int SUBTASK_TRANSITIONS_DATASET_INDEX = 2;
    
    @Mock
    private JiraProperties jiraProperties;
    
    @Mock
    private IssueStatusFlowService issueService;
    
    @Spy
    @InjectMocks
    private ThroughputKPIService throughputKpiService = new ThroughputKPIService();
    
    
    @Before
    public void setupProperties() {
        
        String[] defaultDoneStatuses = new String[] {"Done","Cancelled"};
        FinalStatuses finalStatuses = new FinalStatuses();
        
        finalStatuses.setDemands(defaultDoneStatuses);
        finalStatuses.setTasks(defaultDoneStatuses);
        finalStatuses.setSubtasks(defaultDoneStatuses);
        
        when(jiraProperties.getFinalStatuses()).thenReturn(finalStatuses);
        
        IssueStatusFlow demand = new IssueStatusFlowBuilder("I-1")
                .type("Demand")
                .addChain("To Do",parseDateTime("2017-09-25"))
                .addChain("Doing",parseDateTime("2017-09-26"))
                .addChain("Done",parseDateTime("2017-09-27"))
                .build();
        
        IssueStatusFlow os = new IssueStatusFlowBuilder("I-2")
                .type("OS")
                .addChain("To Do",parseDateTime("2017-09-25"))
                .addChain("Doing",parseDateTime("2017-09-26"))
                .addChain("Done")
                .build();
        
        IssueStatusFlow feature = new IssueStatusFlowBuilder("I-3")
                .type("Feature")
                .addChain("To Do",parseDateTime("2017-09-25"))
                .addChain("Doing",parseDateTime("2017-09-26"))
                .addChain("Done")
                .build();
        
        IssueStatusFlow subtask = new IssueStatusFlowBuilder("I-4")
                .type("Sub-task")
                .addChain("To Do",parseDateTime("2017-09-25"))
                .addChain("Doing")
                .addChain("Done")
                .build();
        
        when(issueService.getIssues(Mockito.any()))
                .thenReturn(Arrays.asList(demand,os))
                .thenReturn(Arrays.asList(feature))
                .thenReturn(Arrays.asList(subtask));
        
    }
    
    @Test
    public void checkThroughputRows() {
        List<ThroughputDataSet> tpDataSets = throughputKpiService.getData(getDefaultFollowupData());
        List<ThroughputRow> demandsRows = tpDataSets.get(DEMAND_TRANSITIONS_DATASET_INDEX).rows;
        
        assertThat(demandsRows.size(),is(6));
        assertRow(demandsRows.get(0),parseDateTime("2017-09-25"),"Demand",0L);
        assertRow(demandsRows.get(1),parseDateTime("2017-09-25"),"OS",0L);
        
        assertRow(demandsRows.get(2),parseDateTime("2017-09-26"),"Demand",0L);
        assertRow(demandsRows.get(3),parseDateTime("2017-09-26"),"OS",0L);
        
        assertRow(demandsRows.get(4),parseDateTime("2017-09-27"),"Demand",1L);
        assertRow(demandsRows.get(5),parseDateTime("2017-09-27"),"OS",0L);
        
        List<ThroughputRow> featureRows = tpDataSets.get(FEATURES_TRANSITIONS_DATASET_INDEX).rows;
        
        assertThat(featureRows.size(),is(2));
        assertRow(featureRows.get(0),parseDateTime("2017-09-25"),"Feature",0L);
        assertRow(featureRows.get(1),parseDateTime("2017-09-26"),"Feature",0L);
        
        List<ThroughputRow> subtasksRows = tpDataSets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        
        assertThat(subtasksRows.size(),is(1));
        assertRow(subtasksRows.get(0),parseDateTime("2017-09-25"),"Sub-task",0L);
    }
    
    @Test
    public void checkEmptyDataSets() {
        List<ThroughputDataSet> tpDataSets = throughputKpiService.getData(FollowUpHelper.getEmptyFollowupData());
        
        List<ThroughputRow> demandsRows = tpDataSets.get(DEMAND_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(demandsRows.size(),is(0));
        
        List<ThroughputRow> featureRows = tpDataSets.get(FEATURES_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(featureRows.size(),is(0));
        
        List<ThroughputRow> subtasksRows = tpDataSets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(subtasksRows.size(),is(0));
    }

    
    private void assertRow(ThroughputRow tpRow, ZonedDateTime date, String type, Long count) {
        assertThat(tpRow.date,is(date));
        assertThat(tpRow.type,is(type));
        assertThat(tpRow.count,is(count));
    }
    
}
