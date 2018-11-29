package objective.taskboard.followup.kpi;

import static objective.taskboard.followup.kpi.KpiLevel.DEMAND;
import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static objective.taskboard.followup.kpi.KpiLevel.UNMAPPED;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.data.Issue;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.kpi.enviroment.KPIEnvironmentBuilder;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapterFactory;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.Clock;

@RunWith(MockitoJUnitRunner.class)
public class FullCycleEffortTest {
    
    private static final int HOUR_IN_MILIS = 1*60*60*1000;
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    
    @Mock
    private KPIProperties kpiProperties;
    
    @Mock
    private JiraProperties jiraProperties;
    
    @Mock
    private IssueBufferService issueBufferService;
    
    @Mock
    private IssueKpiDataItemAdapterFactory factory;
    
    @Mock
    private ProjectService projectService;
    
    @Mock
    private Clock clock;
    
    @InjectMocks
    private IssueKpiService service;
    
    private KPIEnvironmentBuilder builder;
    
    
    @Before
    public void setupEnviroment() {
        builder = new KPIEnvironmentBuilder(kpiProperties);
        configureTypes();
        configureStatuses();
        configureHierarchy();

        configureClock();
        configureProject();
        configureJiraProperties();
    }
    


    @Test
    public void fullTest() {
        mockDemand("PROJ-01");
        
        mockFeatures();
        
        mockSubtasks();
        
        mockTaskContinuous("Continuous","PROJ-20","PROJ-01","2018-01-01","2018-01-03","2018-01-10");
        mockTaskContinuous("SubTask Continuous","PROJ-21","PROJ-20","2018-01-01","2018-01-03","2018-01-10");
        
        addWorklogs();
        
        List<Issue> allIssues = builder.mockAllIssues();
        when(issueBufferService.getAllIssues()).thenReturn(allIssues);
        when(factory.getItems(allIssues ,ZONE_ID)).thenReturn(builder.buildAllIssuesAsAdapterUnless("PROJ-20","PROJ-21"));
        
        List<IssueKpi> issuesDemand = service.getIssuesFromCurrentState("PROJ", ZONE_ID, DEMAND);
        assertThat(issuesDemand.size(),is(1));
        IssueKpi demand = issuesDemand.get(0);
        
        assertHours(demand, "Doing", 189l);
        assertHours(demand, "UATing", 7l);
        
        List<IssueKpi> issuesFeatures = service.getIssuesFromCurrentState("PROJ", ZONE_ID, FEATURES);
        assertThat(issuesFeatures.size(),is(4));
        Map<String,IssueKpi> features = map(FEATURES, issuesFeatures);
        assertFeatureEffort(features.get("PROJ-02"),0l,12l,0l,0l);
        assertFeatureEffort(features.get("PROJ-03"),0l,12l,0l,0l);
        assertFeatureEffort(features.get("PROJ-04"),0l,15l,0l,0l);
        assertFeatureEffort(features.get("PROJ-05"),13l,51l,74l,12l);
        
        List<IssueKpi> issuesSubtasks = service.getIssuesFromCurrentState("PROJ", ZONE_ID, SUBTASKS);
        assertThat(issuesSubtasks.size(),is(14));
        Map<String,IssueKpi> subtasks = map(SUBTASKS, issuesSubtasks);
        assertSubtaskEffort(subtasks.get("PROJ-06"),5l, 2l,0l);
        assertSubtaskEffort(subtasks.get("PROJ-07"),7l, 4l,1l);
        assertSubtaskEffort(subtasks.get("PROJ-08"),10l,2l,0l);
        assertSubtaskEffort(subtasks.get("PROJ-09"),10l,2l,3l);
        assertSubtaskEffort(subtasks.get("PROJ-10"),6l, 0l,0l);
        assertSubtaskEffort(subtasks.get("PROJ-11"),5l, 0l,2l);
        assertSubtaskEffort(subtasks.get("PROJ-12"),10l,4l,1l);
        assertSubtaskEffort(subtasks.get("PROJ-13"),8l, 0l,2l);
        assertSubtaskEffort(subtasks.get("PROJ-14"),12l,0l,6l);
        assertSubtaskEffort(subtasks.get("PROJ-15"),20l,5l,1l);
        assertSubtaskEffort(subtasks.get("PROJ-16"),6l, 1l,1l);
        assertSubtaskEffort(subtasks.get("PROJ-17"),25l,0l,0l);
        assertSubtaskEffort(subtasks.get("PROJ-18"),21l,2l,0l);
        assertSubtaskEffort(subtasks.get("PROJ-19"),12l,0l,0l);
            
        List<IssueKpi> issuesUnmapped = service.getIssuesFromCurrentState("PROJ", ZONE_ID, UNMAPPED);
        assertThat(issuesUnmapped.size(),is(0));
    }

    private void mockTaskContinuous(String type, String pKey, String fatherKey, String open, String doing, String done) {
        builder.withMockingIssue(pKey, type, KpiLevel.UNMAPPED)
            .addTransition("Open",open)
            .addTransition("Doing",doing)
            .addTransition("Done",done)
            .setFatherToCurrentIssue(fatherKey)
            .setProjectKeyToCurrentIssue("PROJ");
        builder.withIssue(fatherKey).addChild(pKey);
    }

    private void assertSubtaskEffort(IssueKpi subtask, long doingHours, long reviewingHours, long mergingHours) {
        assertHours(subtask, "Open", 0l);
        assertHours(subtask, "To Do", 0l);
        assertHours(subtask, "Doing", doingHours);
        assertHours(subtask, "To Review", 0l);
        assertHours(subtask, "Reviewing", reviewingHours);
        assertHours(subtask, "To Merge", 0l);
        assertHours(subtask, "Merging", mergingHours);
        assertHours(subtask, "Done", 0l);
        assertHours(subtask, "Cancelled", 0l);
    }

    private void assertFeatureEffort(IssueKpi feature,long planningHours, long developingHours, long internalQaingHours, long qaingHours) {
        assertHours(feature, "Open", 0l);
        assertHours(feature, "To Plan", 0l);
        assertHours(feature, "Planning", planningHours);
        assertHours(feature, "To Dev", 0l);
        assertHours(feature, "Developing", developingHours);
        assertHours(feature, "To Internal QA", 0l);
        assertHours(feature, "Internal QAing", internalQaingHours);
        assertHours(feature, "To Deploy", 0l);
        assertHours(feature, "To QA", 0l);
        assertHours(feature, "QAing", qaingHours);
        assertHours(feature, "Cancelled", 0l);
        assertHours(feature, "Done", 0l);
        
    }

    private Map<String, IssueKpi> map(KpiLevel level, List<IssueKpi> issues) {
        return issues.stream().collect(Collectors.toMap(IssueKpi::getIssueKey, Function.identity()));
    }

    private void addWorklogs() {
        builder.withIssue("PROJ-06")
                .addWorklog("2018-01-20",getHourInMilis(5))
                .addWorklog("2018-01-25",getHourInMilis(2));
        
        builder.withIssue("PROJ-07")
                .addWorklog("2018-01-02",getHourInMilis(7))
                .addWorklog("2018-01-03",getHourInMilis(4))
                .addWorklog("2018-01-05",getHourInMilis(1));
        
        builder.withIssue("PROJ-08")
                .addWorklog("2018-01-05",getHourInMilis(10))
                .addWorklog("2018-01-06",getHourInMilis(2));

        builder.withIssue("PROJ-09")
                .addWorklog("2018-01-04",getHourInMilis(10))
                .addWorklog("2018-01-05",getHourInMilis(2))
                .addWorklog("2018-01-06",getHourInMilis(3));

        builder.withIssue("PROJ-10")
                .addWorklog("2018-01-02",getHourInMilis(6));
        
        builder.withIssue("PROJ-11")
                .addWorklog("2018-01-02",getHourInMilis(5))
                .addWorklog("2018-01-04",getHourInMilis(2));
        
        builder.withIssue("PROJ-12")
                .addWorklog("2018-01-05",getHourInMilis(10))
                .addWorklog("2018-01-09",getHourInMilis(4))
                .addWorklog("2018-01-10",getHourInMilis(1));
        
        builder.withIssue("PROJ-13")
                .addWorklog("2018-01-07",getHourInMilis(2))
                .addWorklog("2018-01-08",getHourInMilis(6))
                .addWorklog("2018-01-10",getHourInMilis(1))
                .addWorklog("2018-01-12",getHourInMilis(1));
        
        builder.withIssue("PROJ-14")
                .addWorklog("2018-01-08",getHourInMilis(12))
                .addWorklog("2018-01-12",getHourInMilis(6));
        
        builder.withIssue("PROJ-15")
                .addWorklog("2018-01-12",getHourInMilis(20))
                .addWorklog("2018-01-14",getHourInMilis(5))
                .addWorklog("2018-01-16",getHourInMilis(1));
        
        builder.withIssue("PROJ-16")
                .addWorklog("2018-01-11",getHourInMilis(6))
                .addWorklog("2018-01-13",getHourInMilis(1))
                .addWorklog("2018-01-16",getHourInMilis(1));
        
        builder.withIssue("PROJ-17")
                .addWorklog("2018-01-11",getHourInMilis(25));
        
        builder.withIssue("PROJ-18")
                .addWorklog("2018-01-16",getHourInMilis(21))
                .addWorklog("2018-01-20",getHourInMilis(2));
        
        builder.withIssue("PROJ-19")
                .addWorklog("2018-01-17",getHourInMilis(10))
                .addWorklog("2018-01-18",getHourInMilis(2));
        
        builder.withIssue("PROJ-21")
            .addWorklog("2018-01-05",getHourInMilis(8));
    }
    
    private void assertHours(IssueKpi kpi, String status, long hours) {
        assertThat(kpi.getEffort(status) / HOUR_IN_MILIS, Matchers.is(hours));
    }
    
    private int getHourInMilis(int hours) {
        return hours*HOUR_IN_MILIS;
    }

    private void mockSubtasks() {

        mockSubtask("PROJ-06","PROJ-01","UAT",
                "2018-01-17","2018-01-18","2018-01-20","2018-01-25","2018-01-25",null,null,"2018-02-02",null);
        
        mockSubtask("PROJ-07","PROJ-02","Frontend Development",
                "2018-01-02","2018-01-02","2018-01-03","2018-01-03","2018-01-03","2018-01-04","2018-01-04","2018-01-07",null);
        
        mockSubtask("PROJ-08","PROJ-03","Backend Development",
                "2018-01-04","2018-01-04","2018-01-05","2018-01-05","2018-01-06",null,null,null,"2018-01-09");
        
        mockSubtask("PROJ-09","PROJ-04","Subtask",
                "2018-01-04","2018-01-04","2018-01-04","2018-01-05","2018-01-05","2018-01-05","2018-01-06","2018-01-06",null);
        
        mockSubtask("PROJ-10","PROJ-05","Tech Planning",
                "2018-01-02","2018-01-02","2018-01-02",null,null,null,null,"2018-01-03",null);
        
        mockSubtask("PROJ-11","PROJ-05","Feature Planning",
                "2018-01-03","2018-01-03","2018-01-03",null,null,null,null,"2018-01-03",null);
        
        mockSubtask("PROJ-12","PROJ-05","Backend Development",
                "2018-01-03","2018-01-03","2018-01-05","2018-01-07","2018-01-09","2018-01-10","2018-01-10","2018-01-10",null);
        
        mockSubtask("PROJ-13","PROJ-05","Frontend Development",
                "2018-01-05","2018-01-06","2018-01-07","2018-01-10","2018-01-10","2018-01-10","2018-01-10","2018-01-11",null);
        
        mockSubtask("PROJ-14","PROJ-05","UX",
                "2018-01-07","2018-01-07","2018-01-08","2018-01-10","2018-01-12","2018-01-12","2018-01-12","2018-01-12",null);
        
        mockSubtask("PROJ-15","PROJ-05","Alpha Test",
                "2018-01-10","2018-01-10","2018-01-12","2018-01-14","2018-01-14","2018-01-16","2018-01-16","2018-01-16",null);
        
        mockSubtask("PROJ-16","PROJ-05","Alpha Bug",
                "2018-01-09","2018-01-09","2018-01-11","2018-01-13","2018-01-13","2018-01-16","2018-01-16","2018-01-17",  null);
        
        mockSubtask("PROJ-17","PROJ-05","Functional Test",
                "2018-01-10","2018-01-10","2018-01-11",null,null,null,null,"2018-01-15",null);
        
        mockSubtask("PROJ-18","PROJ-05","Functional Review",
                "2018-01-10","2018-01-10","2018-01-16","2018-01-18","2018-01-20",null,null,null,null);
        
        mockSubtask("PROJ-19","PROJ-05","QA",
                "2018-01-15","2018-01-17","2018-01-17",null,null,null,null,"2018-01-20",null);
    }

    private void mockFeatures() {
        mockFeature("PROJ-02","PROJ-01","Bug",
                "2018-01-01","2018-01-01",null,"2018-01-02","2018-01-03","2018-01-04",
                "2018-01-04","2018-01-05","2018-01-05","2018-01-06","2018-01-07",null);
        
        mockFeature("PROJ-03","PROJ-01","Bug",
                "2018-01-04",null,null,"2018-01-04",null,null,
                null,null,null,null,null,"2018-01-09");
        
        mockFeature("PROJ-04","PROJ-01","Task",
                "2018-01-03","2018-01-03","2018-01-04","2018-01-04","2018-01-04",null,
                null,null,null,null,"2018-01-05",null);
        
        mockFeature("PROJ-05","PROJ-01","Feature",
                "2018-01-02","2018-01-02","2018-01-02","2018-01-03","2018-01-03","2018-01-10",
                "2018-01-10","2018-01-15","2018-01-15","2018-01-17","2018-01-20",null);
    }
    
    
    
    private void mockSubtask(String pKey, String father, String type, String... dates) {
        if(dates.length != 9)
            throw new IllegalArgumentException("Feature demands 9 transitiosn dates. Use null when a transition shouldnt occur");
        builder.withMockingIssue(pKey, type, KpiLevel.SUBTASKS)
                .addTransition("Open",dates[0])
                .addTransition("To Do",dates[1])
                .addTransition("Doing",dates[2])
                .addTransition("To Review",dates[3])
                .addTransition("Reviewing",dates[4])
                .addTransition("To Merge",dates[5])
                .addTransition("Merging",dates[6])
                .addTransition("Done",dates[7])
                .addTransition("Cancelled",dates[8])
                .setFatherToCurrentIssue(father)
                .setProjectKeyToCurrentIssue("PROJ");
        
        builder.withIssue(father).addChild(pKey);
    }

    private void mockFeature(String pkey,String father, String type, String...dates) {
        if(dates.length != 12)
            throw new IllegalArgumentException("Feature demands 12 transitiosn dates. Use null when a transition shouldnt occur");
        builder.withMockingIssue(pkey, type, KpiLevel.FEATURES)
                .addTransition("Open",dates[0])
                .addTransition("To Plan",dates[1])
                .addTransition("Planning",dates[2])
                .addTransition("To Dev",dates[3])
                .addTransition("Developing",dates[4])
                .addTransition("To Internal QA",dates[5])
                .addTransition("Internal QAing",dates[6])
                .addTransition("To Deploy",dates[7])
                .addTransition("To QA",dates[8])
                .addTransition("QAing",dates[9])
                .addTransition("Done",dates[10])
                .addTransition("Cancelled",dates[11])
                .setFatherToCurrentIssue(father)
                .setProjectKeyToCurrentIssue("PROJ");
        
        builder.withIssue(father).addChild(pkey);
    }
    
    private void mockDemand(String pkey) {
        builder.withMockingIssue(pkey, "Demand", KpiLevel.DEMAND)
                .addTransition("Open","2018-01-01")
                .addTransition("To Do","2018-01-05")
                .addTransition("Doing","2018-01-09")
                .addTransition("To UAT","2018-01-12")
                .addTransition("UATing","2018-01-15")
                .addTransition("Done","2018-01-30")
                .addTransition("Cancelled","2018-02-02")
                .setCurrentStatusToCurrentIssue("Cancelled")
                .setProjectKeyToCurrentIssue("PROJ")
                .setCurrentStatusToCurrentIssue("Cancelled");
    }

    private void configureStatuses() {
        builder.addStatus(1l,"Open",false)
                .addStatus(2l,"To Do",false)
                .addStatus(3l,"Doing",true)
                .addStatus(4l,"To UAT",false)
                .addStatus(5l,"UATing",true)
                .addStatus(6l,"Done",false)
                .addStatus(7l,"Cancelled",false);
        
        builder.addStatus(8l, "To Plan", false)
                .addStatus(9l, "Planning", true)
                .addStatus(10l, "To Dev", false)
                .addStatus(11l, "Developing", true)
                .addStatus(12l, "To Internal QA", false)
                .addStatus(13l, "Internal QAing", true)
                .addStatus(14l, "To Deploy", false)
                .addStatus(15l, "To QA", false)
                .addStatus(16l, "QAing", true);
                
        builder.addStatus(17l, "To Review", false)
                .addStatus(18l, "Reviewing", true)
                .addStatus(19l, "To Merge", false)
                .addStatus(20l, "Merging", true);
    }
    

    private void configureHierarchy() {
        builder.addSubtaskHierarchy(FEATURES,"QAing","QA")
                .addSubtaskHierarchy(FEATURES,"Internal QAing","Alpha Test", "Functional Test", "Functional Review")
                .addSubtaskHierarchy(FEATURES,"Developing","Alpha Bug", "Subtask","Backend Development", "Frontend Development", "UX")
                .addSubtaskHierarchy(FEATURES,"Planning","Feature Planning", "Tech Planning");
        
        builder.addSubtaskHierarchy(DEMAND, "UATing", "UAT")
                .addStatusHierarchy(DEMAND, "Doing", "Planning","To Dev","Developing","To Internal QA","Internal QAing","QAing");
                
        builder.getMockedKPIProperties();
    }

    private void configureTypes() {
        builder.addFeatureType(2l, "Feature")
                .addFeatureType(3l, "Bug")
                .addFeatureType(4l, "Task")
                .addSubtaskType(5l, "UAT")
                .addSubtaskType(6l, "Frontend Development")
                .addSubtaskType(7l, "Backend Development")
                .addSubtaskType(8l, "Subtask")
                .addSubtaskType(9l, "Tech Planning")
                .addSubtaskType(10l, "Feature Planning")
                .addSubtaskType(11l, "UX")
                .addSubtaskType(12l, "Alpha Test")
                .addSubtaskType(13l, "Alpha Bug")
                .addSubtaskType(14l, "Functional Test")
                .addSubtaskType(15l, "Functional Review")
                .addSubtaskType(16l, "QA");
        
    }
    
    private void configureProject() {
        ProjectFilterConfiguration project = Mockito.mock(ProjectFilterConfiguration.class);
        when(project.getStartDate()).thenReturn(Optional.of(parseDateTime("2018-01-01").toLocalDate()));
        when(project.getDeliveryDate()).thenReturn(Optional.of(parseDateTime("2018-02-02").toLocalDate()));
        
        when(projectService.getTaskboardProjectOrCry("PROJ")).thenReturn(project);
    }

    private void configureJiraProperties() {
        JiraProperties.Followup followup = new JiraProperties.Followup();
        when(jiraProperties.getFollowup()).thenReturn(followup);
    }
    
    private void configureClock() {
        when(clock.now()).thenReturn(parseDateTime("2018-02-02").toInstant());
    }
    
}
