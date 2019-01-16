package objective.taskboard.followup.kpi.transformer;

import static java.util.Arrays.asList;
import static objective.taskboard.followup.kpi.KpiLevel.DEMAND;
import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.AnalyticsTransitionsDataRow;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.IssueTransitionService;
import objective.taskboard.followup.KpiHelper;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.KPIEnvironmentBuilder;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.IssueType;
import objective.taskboard.jira.properties.JiraProperties.IssueType.IssueTypeDetails;
import objective.taskboard.jira.properties.StatusConfiguration.StatusPriorityOrder;
import objective.taskboard.utils.DateTimeUtils;

@RunWith(MockitoJUnitRunner.class)
public class IssueKpiDataItemAdapterFactoryTest {

    private static final int DEMAND_TRANSITIONS_DATASET_INDEX = 0;

    @Mock
    private MetadataService metadataService;
    
    @Mock
    private JiraProperties jiraProperties;
    
    @Mock
    private IssueTransitionService transitionService;
    
    private IssueKpiDataItemAdapterFactory subject;
    
    @Before
    public void setup() {
        setupPriorityOrder();
        setupIssueType();
        subject = new IssueKpiDataItemAdapterFactory(metadataService,jiraProperties,transitionService);
    }
    
    @Test
    public void convertIssues() { 
        
        KPIEnvironmentBuilder builder = new KPIEnvironmentBuilder().withIssueTransitionService(transitionService);
        
        builder.addStatus(1l, "Open", false)
                .addStatus(2l, "To Do", false)
                .addStatus(3l, "Doing", true)
                .addStatus(4l, "Done", false);
        
        builder.addSubtaskType(1l, "Dev")
                .addSubtaskType(2l, "Alpha");
        
         builder.withMockingIssue("I-1", "Dev",KpiLevel.SUBTASKS)
                    .setProjectKeyToCurrentIssue("PROJ")
                    .setCurrentStatusToCurrentIssue("Doing")
                    .addTransition("Open", "2020-01-01")
                    .addTransition("To Do", "2020-01-02")
                    .addTransition("Doing", "2020-01-03")
                    .addTransition("Done");
        
        builder.withMockingIssue("I-2", "Alpha",KpiLevel.SUBTASKS)
                .setProjectKeyToCurrentIssue("PROJ")
                .setCurrentStatusToCurrentIssue("Done")
                .addTransition("Open", "2020-01-01")
                .addTransition("To Do", "2020-01-02")
                .addTransition("Doing", "2020-01-03")
                .addTransition("Done", "2020-01-04");
        
        List<IssueKpiDataItemAdapter> items = subject.getItems(builder.mockAllIssues(), ZoneId.systemDefault());
        assertThat(items.size(),is(2));
        IssueKpiDataItemAdapter issue = items.get(0);
        assertThat(issue.getIssueKey(),is("I-1"));
        assertThat(issue.getLevel(),is(SUBTASKS));
        
        String devType = issue.getIssueType().map(t -> t.getType()).orElse("Unmapped");
        assertThat(devType,is("Dev"));
                
        Map<String, ZonedDateTime> transitions = issue.getTransitions();
        assertThat(transitions.keySet().toString(),is("[Done, Doing, To Do, Open]"));
        assertThat(transitions.get("Open"),is(parseDateTime("2020-01-01")));
        assertThat(transitions.get("To Do"),is(parseDateTime("2020-01-02")));
        assertThat(transitions.get("Doing"),is(parseDateTime("2020-01-03")));
        assertNull(transitions.get("Done"));
        
        
        IssueKpiDataItemAdapter issue2 = items.get(1);
        assertThat(issue2.getIssueKey(),is("I-2"));
        assertThat(issue2.getLevel(),is(SUBTASKS));
        
        String alphaType = issue2.getIssueType().map(t -> t.getType()).orElse("Unmapped");
        assertThat(alphaType,is("Alpha"));
        
        
        Map<String, ZonedDateTime> transitions2 = issue2.getTransitions();
        assertThat(transitions2.keySet().toString(),is("[Done, Doing, To Do, Open]"));
        assertThat(transitions2.get("Open"),is(parseDateTime("2020-01-01")));
        assertThat(transitions2.get("To Do"),is(parseDateTime("2020-01-02")));
        assertThat(transitions2.get("Doing"),is(parseDateTime("2020-01-03")));
        assertThat(transitions2.get("Done"),is(parseDateTime("2020-01-04")));
    }
    
    @Test
    public void getIssuesFromAnalytic_happyDay() {
        List<AnalyticsTransitionsDataSet> dataSets = KpiHelper.getDefaultAnalyticsTransitionsDataSet();
        
        List<IssueKpiDataItemAdapter> demands = subject.getItems(Optional.of(dataSets.get(DEMAND_TRANSITIONS_DATASET_INDEX)));
        assertThat(demands.size(),is(2));
        IssueKpiDataItemAdapter demand = demands.get(0);
        IssueKpiDataItemAdapter os = demands.get(1);
        
        assertThat(demand.getIssueKey(),is("I-1"));
        assertThat(demand.getLevel(),is(DEMAND));
        
        String type = demand.getIssueType().map(t -> t.getType()).orElse("Unmapped");
        assertThat(type,is("Demand"));
        
        Map<String, ZonedDateTime> transitionsDemand = demand.getTransitions();
        assertThat(transitionsDemand.keySet().toString(),is("[Done, Doing, To Do]"));
        assertThat(transitionsDemand.get("To Do"),is(parseDateTime("2017-09-25")));
        assertThat(transitionsDemand.get("Doing"),is(parseDateTime("2017-09-26")));
        assertThat(transitionsDemand.get("Done"),is(parseDateTime("2017-09-27")));
        
        
        assertThat(os.getIssueKey(),is("I-4"));
        assertThat(os.getLevel(),is(FEATURES));
        
        type = os.getIssueType().map(t -> t.getType()).orElse("Unmapped");
        assertThat(type,is("OS"));
        
        Map<String, ZonedDateTime> transitionsOS = os.getTransitions();
        assertThat(transitionsOS.keySet().toString(),is("[Done, Doing, To Do]"));
        assertThat(transitionsOS.get("To Do"),is(parseDateTime("2017-09-25")));
        assertThat(transitionsOS.get("Doing"),is(parseDateTime("2017-09-26")));
        assertNull(transitionsOS.get("Done"));
        
    }
    
    @Test
    public void getIssuesFromAnalytic_emptySet() {
        List<AnalyticsTransitionsDataSet> dataSets = KpiHelper.getEmptyAnalyticsTransitionsDataSet();
        List<IssueKpiDataItemAdapter> demands = subject.getItems(Optional.of(dataSets.get(0)));
        
        assertThat(demands.size(),is(0));
    }
    
    @Test
    public void getIssuesFromAnalytic_inexitentLevelTye() {
        
        Mockito.when(metadataService.getIssueTypeByName("Inexistent")).thenReturn(Optional.empty());
        
        List<String> headers = new LinkedList<>();
        headers.add("PKEY");
        headers.add("ISSUE_TYPE");
        headers.add("Done");
        headers.add("Doing");
        headers.add("To Do");

        AnalyticsTransitionsDataRow row = new AnalyticsTransitionsDataRow("I-2", "Inexistent"
                , asList(
                        null
                        , DateTimeUtils.parseDateTime("2017-09-26")
                        , DateTimeUtils.parseDateTime("2017-09-25")));
        
        AnalyticsTransitionsDataSet analyticDataset = new AnalyticsTransitionsDataSet("Inexistent", headers, asList(row));
        
        
        List<IssueKpiDataItemAdapter> issues = subject.getItems(Optional.of(analyticDataset));
        
        assertThat(issues.size(),is(1));
        assertThat(issues.get(0).getLevel(),is(KpiLevel.UNMAPPED));
    }
    
    @Test
    public void getIssuesFromService_inexistentLevelType() {
        KPIEnvironmentBuilder builder = new KPIEnvironmentBuilder().withIssueTransitionService(transitionService);
        
        builder.addStatus(1l, "Open", false)
                .addStatus(2l, "To Do", false)
                .addStatus(3l, "Doing", true)
                .addStatus(4l, "Done", false);
        
        builder.addFeatureType(1l, "Feature")
                .addSubtaskType(2l, "Continuos")
                .addSubtaskType(3l, "Subtask Continuous");
                
        
         builder.withMockingIssue("I-1", "Continuous",KpiLevel.UNMAPPED)
                    .setProjectKeyToCurrentIssue("PROJ")
                    .setCurrentStatusToCurrentIssue("Doing")
                    .addTransition("Open", "2020-01-01")
                    .addTransition("To Do", "2020-01-02")
                    .addTransition("Doing", "2020-01-03")
                    .addTransition("Done");
        
        builder.withMockingIssue("I-2", "Subtask Continunous",KpiLevel.UNMAPPED)
                .setProjectKeyToCurrentIssue("PROJ")
                .setCurrentStatusToCurrentIssue("Done")
                .addTransition("Open", "2020-01-01")
                .addTransition("To Do", "2020-01-02")
                .addTransition("Doing", "2020-01-03")
                .addTransition("Done", "2020-01-04")
                .setFatherToCurrentIssue("I-1");
        builder.withIssue("I-1").addChild("I-2");
        
        builder.withMockingIssue("I-3", "Feature", KpiLevel.FEATURES)
                .addTransition("Open", "2020-01-01")
                .addTransition("To Do", "2020-01-02")
                .addTransition("Doing", "2020-01-03")
                .addTransition("Done", "2020-01-04");
        
        List<IssueKpiDataItemAdapter> items = subject.getItems(builder.mockAllIssues(), ZoneId.systemDefault());
        assertThat(items.size(),is(1));
        
        IssueKpiDataItemAdapter issue = items.get(0);
        assertThat(issue.getIssueKey(),is("I-3"));
        assertThat(issue.getLevel(),is(KpiLevel.FEATURES));
        
        String featureType = issue.getIssueType().map(t -> t.getType()).orElse("Unmapped");
        assertThat(featureType,is("Feature"));
        
    }
    
    private void setupPriorityOrder() {
        String[] statuses = new String[] {"Done","Doing","To Do","Open"};
        StatusPriorityOrder statusPriorityOrder = new StatusPriorityOrder();
        statusPriorityOrder.setDemands(statuses);
        statusPriorityOrder.setTasks(statuses);
        statusPriorityOrder.setSubtasks(statuses);
        when(jiraProperties.getStatusPriorityOrder()).thenReturn(statusPriorityOrder);
    }
    
    private void setupIssueType() {
        
        JiraIssueTypeDto demand = new JiraIssueTypeDto(1L, "Demand", false);
        JiraIssueTypeDto os = new JiraIssueTypeDto(2L, "OS", false);
        
        Mockito.when(metadataService.getIssueTypeByName("Demand")).thenReturn(Optional.of(demand));
        Mockito.when(metadataService.getIssueTypeByName("OS")).thenReturn(Optional.of(os));
        
        
        IssueType issueType = Mockito.mock(IssueType.class);
        Mockito.when(jiraProperties.getIssuetype()).thenReturn(issueType);
        Mockito.when(issueType.getDemand()).thenReturn(new IssueTypeDetails(demand.getId()));
        Mockito.when(issueType.getFeatures()).thenReturn(Arrays.asList(new IssueTypeDetails(os.getId())));
        
    }
}
