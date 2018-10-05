package objective.taskboard.followup;

import static objective.taskboard.followup.FollowUpHelper.getDefaultFollowupData;
import static objective.taskboard.followup.FollowUpHelper.getEmptyFollowupData;
import static objective.taskboard.followup.kpi.StatusTransitionBuilder.DefaultStatus.DOING;
import static objective.taskboard.followup.kpi.StatusTransitionBuilder.DefaultStatus.DONE;
import static objective.taskboard.followup.kpi.StatusTransitionBuilder.DefaultStatus.TODO;
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
import objective.taskboard.followup.kpi.IssueKpiBuilder;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.WipKPIService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.StatusConfiguration.StatusCountingOnWip;

@RunWith(MockitoJUnitRunner.class)
public class WipKPIServiceTest {

    private static final int DEMAND_TRANSITIONS_DATASET_INDEX = 0;
    private static final int FEATURES_TRANSITIONS_DATASET_INDEX = 1;
    private static final int SUBTASK_TRANSITIONS_DATASET_INDEX = 2;

    @Mock
    private JiraProperties jiraProperties;

    @Mock
    private IssueKpiService issueService;
        
    @Spy
    @InjectMocks
    private WipKPIService wipKpiService = new WipKPIService();

    @Before
    public void setupProperties() {

        StatusCountingOnWip statusCountingOnWip = new StatusCountingOnWip();
        statusCountingOnWip.setDemands(new String[] { "Doing" });
        statusCountingOnWip.setTasks(new String[] {"Doing" });
        statusCountingOnWip.setSubtasks(new String[] { "Doing"});

        when(jiraProperties.getStatusCountingOnWip()).thenReturn(statusCountingOnWip);
        
        IssueKpi demand = new IssueKpiBuilder("I-1","Demand",KpiLevel.DEMAND)
                .addTransition(TODO,"2017-09-25")
                .addTransition(DOING,"2017-09-26")
                .addTransition(DONE,"2017-09-27")
                .build();
        
        IssueKpi os = new IssueKpiBuilder("I-2","OS",KpiLevel.FEATURES)
                .addTransition(TODO,"2017-09-25")
                .addTransition(DOING,"2017-09-26")
                .addTransition(DONE)
                .build();
        
        IssueKpi feature = new IssueKpiBuilder("I-3","Feature",KpiLevel.FEATURES)
                .addTransition(TODO,"2017-09-25")
                .addTransition(DOING,"2017-09-26")
                .addTransition(DONE)
                .build();
        
        IssueKpi subtask = new IssueKpiBuilder("I-4","Sub-task",KpiLevel.SUBTASKS)
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
    public void checkWipRows() {
        List<WipDataSet> wipDataSets = wipKpiService.getData(getDefaultFollowupData());
        List<WipRow> demandsRows = wipDataSets.get(DEMAND_TRANSITIONS_DATASET_INDEX).rows;

        assertThat(demandsRows.size(), is(6));
        assertRow(demandsRows.get(0), parseDateTime("2017-09-25"), "Demand", "Doing", 0L);
        assertRow(demandsRows.get(1), parseDateTime("2017-09-25"), "OS", "Doing", 0L);
        assertRow(demandsRows.get(2), parseDateTime("2017-09-26"), "Demand", "Doing", 1L);
        assertRow(demandsRows.get(3), parseDateTime("2017-09-26"), "OS", "Doing", 1L);
        assertRow(demandsRows.get(4), parseDateTime("2017-09-27"), "Demand", "Doing", 0L);
        assertRow(demandsRows.get(5), parseDateTime("2017-09-27"), "OS", "Doing", 1L);
        
        List<WipRow> featuresRows = wipDataSets.get(FEATURES_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(featuresRows.size(), is(2));
        assertRow(featuresRows.get(0), parseDateTime("2017-09-25"), "Feature", "Doing", 0L);
        assertRow(featuresRows.get(1), parseDateTime("2017-09-26"), "Feature", "Doing", 1L);
        
        List<WipRow> subtasksRows = wipDataSets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(subtasksRows.size(), is(1));
        assertRow(subtasksRows.get(0), parseDateTime("2017-09-25"), "Sub-task", "Doing", 0L);
    }
    
    @Test
    public void checkEmptyDataSets() {
        List<WipDataSet> wipDataSets = wipKpiService.getData(getEmptyFollowupData());
        
        List<WipRow> demandsRows = wipDataSets.get(DEMAND_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(demandsRows.size(), is(0));
        
        List<WipRow> featuresRows = wipDataSets.get(FEATURES_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(featuresRows.size(), is(0));
        
        List<WipRow> subtasksRows = wipDataSets.get(SUBTASK_TRANSITIONS_DATASET_INDEX).rows;
        assertThat(subtasksRows.size(), is(0));
    }

    private void assertRow(WipRow wipRow, ZonedDateTime date, String type, String status, Long count) {
        assertThat(wipRow.date, is(date));
        assertThat(wipRow.type, is(type));
        assertThat(wipRow.status, is(status));
        assertThat(wipRow.count, is(count));
    }

}
