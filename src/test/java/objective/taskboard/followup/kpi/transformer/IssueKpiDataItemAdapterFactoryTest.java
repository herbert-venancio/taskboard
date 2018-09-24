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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.AnalyticsTransitionsDataRow;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.FollowUpHelper;
import objective.taskboard.followup.IssueTransitionService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.jira.properties.JiraProperties.IssueType;
import objective.taskboard.jira.properties.JiraProperties.IssueType.IssueTypeDetails;
import objective.taskboard.jira.properties.StatusConfiguration.StatusPriorityOrder;
import objective.taskboard.utils.DateTimeUtils;

@RunWith(MockitoJUnitRunner.class)
public class IssueKpiDataItemAdapterFactoryTest {
    private static final long STATUS_DOING = 3l;
    private static final long STATUS_DONE = 4l;
    private static final String[] STATUSES = new String[] {"Done","Doing","To Do","Open"};

    private static final int DEMAND_TRANSITIONS_DATASET_INDEX = 0;

    @Mock
    private MetadataService metadataService;
    
    @Mock
    private JiraProperties jiraProperties;
    
    @Mock
    private IssueTransitionService transitionService;
    
    @InjectMocks
    private IssueKpiDataItemAdapterFactory subject = new IssueKpiDataItemAdapterFactory();
    
    @Before
    public void setup() {
        setupPriorityOrder();
        setupIssueType();
    }
    
    @Test
    public void convertIssues() { 
        Issue i1 = new IssueMockBuilder(STATUSES)
                .withKey("I-1")
                .withProjectKey("PROJ")
                .withType("Dev")
                .withLevel(SUBTASKS)
                .withStatusId(STATUS_DOING)
                .addTransition("Open", "2020-01-01")
                .addTransition("To Do", "2020-01-02")
                .addTransition("Doing", "2020-01-03")
                .setIssueTransitionService(transitionService) //TODO check best place
                .mockIssue();
        Issue i2 = new IssueMockBuilder(STATUSES)
                .withKey("I-2")
                .withProjectKey("PROJ")
                .withType("Alpha")
                .withLevel(SUBTASKS)
                .withStatusId(STATUS_DONE)
                .addTransition("Open", "2020-01-01")
                .addTransition("To Do", "2020-01-02")
                .addTransition("Doing", "2020-01-03")
                .addTransition("Done", "2020-01-04")
                .setIssueTransitionService(transitionService)
                .mockIssue();
        
        List<IssueKpiDataItemAdapter> items = subject.getItems(Arrays.asList(i1,i2), ZoneId.systemDefault());
        assertThat(items.size(),is(2));
        IssueKpiDataItemAdapter issue = items.get(0);
        assertThat(issue.getIssueKey(),is("I-1"));
        assertThat(issue.getIssueType(),is("Dev"));
        assertThat(issue.getLevel(),is(SUBTASKS));
        
        Map<String, ZonedDateTime> transitions = issue.getTransitions();
        assertThat(transitions.keySet().toString(),is("[Done, Doing, To Do, Open]"));
        assertThat(transitions.get("Open"),is(parseDateTime("2020-01-01")));
        assertThat(transitions.get("To Do"),is(parseDateTime("2020-01-02")));
        assertThat(transitions.get("Doing"),is(parseDateTime("2020-01-03")));
        assertNull(transitions.get("Done"));
        
        
        IssueKpiDataItemAdapter issue2 = items.get(1);
        assertThat(issue2.getIssueKey(),is("I-2"));
        assertThat(issue2.getIssueType(),is("Alpha"));
        assertThat(issue2.getLevel(),is(SUBTASKS));
        
        Map<String, ZonedDateTime> transitions2 = issue2.getTransitions();
        assertThat(transitions2.keySet().toString(),is("[Done, Doing, To Do, Open]"));
        assertThat(transitions2.get("Open"),is(parseDateTime("2020-01-01")));
        assertThat(transitions2.get("To Do"),is(parseDateTime("2020-01-02")));
        assertThat(transitions2.get("Doing"),is(parseDateTime("2020-01-03")));
        assertThat(transitions2.get("Done"),is(parseDateTime("2020-01-04")));
    }
    
    @Test
    public void getIssuesFromAnalytic_happyDay() {
        List<AnalyticsTransitionsDataSet> dataSets = FollowUpHelper.getDefaultAnalyticsTransitionsDataSet();
        
        List<IssueKpiDataItemAdapter> demands = subject.getItems(Optional.of(dataSets.get(DEMAND_TRANSITIONS_DATASET_INDEX)));
        assertThat(demands.size(),is(2));
        IssueKpiDataItemAdapter demand = demands.get(0);
        IssueKpiDataItemAdapter os = demands.get(1);
        
        assertThat(demand.getIssueKey(),is("I-1"));
        assertThat(demand.getIssueType(),is("Demand"));
        assertThat(demand.getLevel(),is(DEMAND));
        
        Map<String, ZonedDateTime> transitionsDemand = demand.getTransitions();
        assertThat(transitionsDemand.keySet().toString(),is("[Done, Doing, To Do]"));
        assertThat(transitionsDemand.get("To Do"),is(parseDateTime("2017-09-25")));
        assertThat(transitionsDemand.get("Doing"),is(parseDateTime("2017-09-26")));
        assertThat(transitionsDemand.get("Done"),is(parseDateTime("2017-09-27")));
        
        
        assertThat(os.getIssueKey(),is("I-4"));
        assertThat(os.getIssueType(),is("OS"));
        assertThat(os.getLevel(),is(FEATURES));
        
        Map<String, ZonedDateTime> transitionsOS = os.getTransitions();
        assertThat(transitionsOS.keySet().toString(),is("[Done, Doing, To Do]"));
        assertThat(transitionsOS.get("To Do"),is(parseDateTime("2017-09-25")));
        assertThat(transitionsOS.get("Doing"),is(parseDateTime("2017-09-26")));
        assertNull(transitionsOS.get("Done"));
        
    }
    
    @Test
    public void getIssuesFromAnalytic_emptySet() {
        List<AnalyticsTransitionsDataSet> dataSets = FollowUpHelper.getEmptyAnalyticsTransitionsDataSet();
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
    
    private void setupPriorityOrder() {
        StatusPriorityOrder statusPriorityOrder = new StatusPriorityOrder();
        statusPriorityOrder.setDemands(STATUSES);
        statusPriorityOrder.setTasks(STATUSES);
        statusPriorityOrder.setSubtasks(STATUSES);
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
