package objective.taskboard.followup;

import static objective.taskboard.followup.KpiHelper.getDefaultFollowupData;
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

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.ThroughputKPIService;
import objective.taskboard.followup.kpi.enviroment.IssueKpiBuilder;
import objective.taskboard.followup.kpi.enviroment.StatusTransitionBuilder.DefaultStatus;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.StatusConfiguration.FinalStatuses;

@RunWith(MockitoJUnitRunner.class)
public class ThroughputKPIServiceTest {
 
    private static final int DEMAND_TRANSITIONS_DATASET_INDEX = 0;
    private static final int FEATURES_TRANSITIONS_DATASET_INDEX = 1;
    private static final int SUBTASK_TRANSITIONS_DATASET_INDEX = 2;
    
    private static final DefaultStatus TODO = new DefaultStatus("To Do",false); 
    private static final DefaultStatus DOING = new DefaultStatus("Doing",true); 
    private static final DefaultStatus DONE = new DefaultStatus("Done",false);
    
    @Mock
    private JiraProperties jiraProperties;
    
    @Mock
    private IssueKpiService issueService;
    
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
        
        IssueKpi demand = new IssueKpiBuilder("I-1",new IssueTypeKpi(1l,"Demand"),KpiLevel.DEMAND)
                .addTransition(TODO,"2017-09-25")
                .addTransition(DOING,"2017-09-26")
                .addTransition(DONE,"2017-09-27")
                .build();
        
        IssueKpi os = new IssueKpiBuilder("I-2",new IssueTypeKpi(2l,"OS"),KpiLevel.FEATURES)
                .addTransition(TODO,"2017-09-25")
                .addTransition(DOING,"2017-09-26")
                .addTransition(DONE)
                .build();
        
        IssueKpi feature = new IssueKpiBuilder("I-3",new IssueTypeKpi(3l,"Feature"),KpiLevel.FEATURES)
                .addTransition(TODO,"2017-09-25")
                .addTransition(DOING,"2017-09-26")
                .addTransition(DONE)
                .build();
        
        IssueKpi subtask = new IssueKpiBuilder("I-4",new IssueTypeKpi(4l,"Sub-task"),KpiLevel.SUBTASKS)
                .addTransition(TODO,"2017-09-25")
                .addTransition(DOING)
                .addTransition(DONE)
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
        List<ThroughputDataSet> tpDataSets = throughputKpiService.getData(KpiHelper.getEmptyFollowupData());
        
        List<ThroughputRow> demandsRows = tpDataSets.get(DEMAND_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(demandsRows.size(),is(0));
        
        List<ThroughputRow> featureRows = tpDataSets.get(FEATURES_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(featureRows.size(),is(0));
        
        List<ThroughputRow> subtasksRows = tpDataSets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(subtasksRows.size(),is(0));
    }

    
    private void assertRow(ThroughputRow tpRow, ZonedDateTime date, String type, Long count) {
        assertThat(tpRow.date,is(date));
        assertThat(tpRow.issueType,is(type));
        assertThat(tpRow.count,is(count));
    }
    
}
