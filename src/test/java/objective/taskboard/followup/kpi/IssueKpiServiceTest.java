package objective.taskboard.followup.kpi;

import static java.util.Arrays.asList;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_FEATURES;
import static objective.taskboard.followup.kpi.KpiLevel.DEMAND;
import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static objective.taskboard.followup.kpi.KpiLevel.UNMAPPED;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static objective.taskboard.utils.DateTimeUtils.parseStringToDate;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
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
import objective.taskboard.data.Worklog;
import objective.taskboard.followup.AnalyticsTransitionsDataRow;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.IssueTransitionService;
import objective.taskboard.followup.kpi.enviroment.KPIEnvironmentBuilder;
import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy;
import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy.Hierarchy;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.transformer.AnalyticDataRowAdapter;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapterFactory;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.DateTimeUtils;

@RunWith(MockitoJUnitRunner.class)
public class IssueKpiServiceTest {

    private static final long STATUS_OPEN = 1l;

    @Mock
    private JiraProperties jiraProperties;
    
    @Mock
    private KPIProperties kpiProperties;

    @Mock
    private IssueBufferService issueBufferService;
    
    @Mock
    private IssueKpiDataItemAdapterFactory factory;

    @Mock
    private IssueTransitionService transitionService;
    
    @InjectMocks
    private IssueKpiService subject = new IssueKpiService();
    
    @Before
    public void setup() {
        JiraProperties.Followup followup = Mockito.mock(JiraProperties.Followup.class);
        when(jiraProperties.getFollowup()).thenReturn(followup);
        when(followup.getStatusExcludedFromFollowup()).thenReturn(Arrays.asList(STATUS_OPEN));
        
        when(kpiProperties.getProgressingStatuses()).thenReturn(Arrays.asList("Doing"));
        
        Hierarchy hierarch = new Hierarchy();
        hierarch.setFatherStatus("Doing");
        hierarch.setChldrenTypeId(asList(2l));
        
        IssueTypeChildrenStatusHierarchy subtasksHierarchy = new IssueTypeChildrenStatusHierarchy();
        subtasksHierarchy.setHierarchies(Arrays.asList(hierarch));
        Mockito.when(kpiProperties.getFeaturesHierarchy()).thenReturn(subtasksHierarchy);
        
     }

    @Test
    public void getIssues_analyticSet() {
        List<String> headers = new LinkedList<>();
        headers.add("PKEY");
        headers.add("ISSUE_TYPE");
        headers.add("Done");
        headers.add("Doing");
        headers.add("To Do");

        AnalyticsTransitionsDataRow row = new AnalyticsTransitionsDataRow("I-2", "Feature"
                , asList(
                        null
                        , DateTimeUtils.parseDateTime("2017-09-26")
                        , DateTimeUtils.parseDateTime("2017-09-25")));
        
        AnalyticsTransitionsDataSet analyticDataset = new AnalyticsTransitionsDataSet(TYPE_FEATURES, headers, asList(row));
        Optional<AnalyticsTransitionsDataSet> optionalDs = Optional.of(analyticDataset);
        Optional<IssueTypeKpi> type = Optional.of(new IssueTypeKpi(1l, "Feature"));
        
        AnalyticDataRowAdapter issueAdapter = new AnalyticDataRowAdapter(row, type, asList("Done","Doing","To Do"), KpiLevel.FEATURES);
        
        Mockito.when(factory.getItems(optionalDs)).thenReturn(Arrays.asList(issueAdapter));
        
        List<IssueKpi> issues = subject.getIssues(optionalDs);
        
        assertThat(issues.size(),is(1));
        IssueKpi issue = issues.get(0);
        
        assertThat(issue.getIssueKey(),is("I-2"));
        assertThat(issue.getLevel(),is(FEATURES));
        assertTrue(issue.isOnStatusOnDay("Doing", parseDateTime("2017-09-27")));
        assertThat(issue.getIssueTypeName(),is("Feature"));
        
    }
    
    @Test
    public void getIssues_currentState() {
        Worklog worklog = new Worklog("a.developer", parseStringToDate("2020-01-03"), 300);
        
        KPIEnvironmentBuilder builder = new KPIEnvironmentBuilder(kpiProperties,transitionService);
        builder.addFeatureType(1l, "Dev")
                .addSubtaskType(2l, "Alpha");
        
        builder.addStatus(1l, "Open", false)
                .addStatus(2l, "To Do" , false)
                .addStatus(3l, "Doing", true)
                .addStatus(4l, "Done", false);
                
        builder.withMockingIssue("I-1", "Dev", KpiLevel.FEATURES)
                .setCurrentStatusToCurrentIssue("Doing")
                .setProjectKeyToCurrentIssue("PROJ")
                .addTransition("Open", "2020-01-01")
                .addTransition("To Do", "2020-01-02")
                .addTransition("Doing", "2020-01-03")
                .addTransition("Done");
          
        builder.withMockingIssue("I-2", "Alpha", SUBTASKS)
                .setCurrentStatusToCurrentIssue("Done")
                .setProjectKeyToCurrentIssue("PROJ")
                .setFatherToCurrentIssue("I-1")
                .addTransition("Open", "2020-01-01")
                .addTransition("To Do", "2020-01-02")
                .addTransition("Doing", "2020-01-03")
                .addTransition("Done", "2020-01-04")
                .addWorklog(worklog);
        
        List<Issue> issues = builder.mockAllIssues();
        ZoneId timezone = ZoneId.systemDefault();
        
        when(issueBufferService.getAllIssues()).thenReturn(issues);
        when(factory.getItems(issues, timezone)).thenReturn(builder.buildAllIssuesAsAdapter());
        
        
        Map<KpiLevel, List<IssueKpi>> issuesKpi = subject.getIssuesFromCurrentState("PROJ", timezone);
        
        assertThat(issuesKpi.keySet().size(),is(4));
        
        List<IssueKpi> demandsIssues = issuesKpi.get(DEMAND);
        List<IssueKpi> featuresIssues = issuesKpi.get(FEATURES);
        List<IssueKpi> subtasksIssues = issuesKpi.get(SUBTASKS);
        List<IssueKpi> unmappedIssues = issuesKpi.get(UNMAPPED);
        
        assertThat(demandsIssues.size(),is(0));
        assertThat(featuresIssues.size(),is(1));
        assertThat(subtasksIssues.size(),is(1));
        assertThat(unmappedIssues.size(),is(0));
        
        IssueKpi i1 = featuresIssues.get(0);
        assertThat(i1.getIssueKey(),is("I-1"));
        
        assertThat(i1.getLevel(),is(FEATURES));
        assertTrue(i1.isOnStatusOnDay("Doing", parseDateTime("2020-01-03")));
        assertThat(i1.getIssueTypeName(),is("Dev"));
        assertThat(i1.getEffort("Doing"), is(300l));
        
        IssueKpi i2 = subtasksIssues.get(0);
        assertThat(i2.getIssueKey(),is("I-2"));
        assertThat(i2.getLevel(),is(SUBTASKS));
        assertTrue(i2.isOnStatusOnDay("Done", parseDateTime("2020-01-04")));
        assertThat(i2.getEffort("Doing"), is(300l));
        assertThat(i2.getIssueTypeName(),is("Alpha"));
        
        assertThat(i2.getEffort("To Do"),is(0l));
        assertThat(i2.getEffort("Doing"),is(300l));
        assertThat(i2.getEffort("To Do"),is(0l));
        
        assertThat(i1.getChildren().size(),is(1));
        assertThat(i1.getChildren().get(0),is(i2));
        
    }
    
    
}
