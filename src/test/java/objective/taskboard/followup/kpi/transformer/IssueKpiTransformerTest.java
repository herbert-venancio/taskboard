package objective.taskboard.followup.kpi.transformer;

import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.KPIEnvironmentBuilder;
import objective.taskboard.followup.kpi.properties.KPIProperties;

@RunWith(MockitoJUnitRunner.class)
public class IssueKpiTransformerTest {
    
    @Mock
    private KPIProperties kpiProperties;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Before
    public void setup() {
        Mockito.when(kpiProperties.getProgressingStatuses()).thenReturn(Arrays.asList("Doing"));
    }
        
    @Test
    public void transformIssues_happyDay() {
        KPIEnvironmentBuilder builder = getDefaultEnvironment();
        
        builder.withMockingIssue("I-1", "Dev", KpiLevel.SUBTASKS)
                .setProjectKeyToCurrentIssue("PROJ")
                .setCurrentStatusToCurrentIssue("Doing")
                .addTransition("Open", "2020-01-01")
                .addTransition("To Do", "2020-01-02")
                .addTransition("Doing", "2020-01-03")
                .addTransition("Done");
        
        buildAlphaSubtask(builder);
                
        List<IssueKpi> issuesKpi = new IssueKpiTransformer(kpiProperties).withItems(builder.buildAllIssuesAsAdapter()).transform();
        assertThat(issuesKpi.size(),is(2));
        
        IssueKpi kpi1 = issuesKpi.get(0);
        
        assertThat(kpi1.getIssueKey(),is("I-1"));
        assertThat(kpi1.getIssueTypeName(),is("Dev"));
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
        assertThat(kpi2.getIssueTypeName(),is("Alpha"));
        assertThat(kpi2.getLevel(),is(SUBTASKS));
        
        assertFalse(kpi2.isOnStatusOnDay("Open", parseDateTime("2020-01-04")));
        assertFalse(kpi2.isOnStatusOnDay("To Do", parseDateTime("2020-01-04")));
        assertFalse(kpi2.isOnStatusOnDay("Doing", parseDateTime("2020-01-04")));
        assertTrue(kpi2.isOnStatusOnDay("Done", parseDateTime("2020-01-04")));
    }

    @Test
    public void wrongConfiguration_toMapHierarchically() {
        
        KPIEnvironmentBuilder builder = getDefaultEnvironment();
        
        buildAlphaSubtask(builder);
        
        IssueKpiTransformer transformer = new IssueKpiTransformer(kpiProperties)
                                                    .withItems(builder.buildAllIssuesAsAdapter())
                                                    .mappingHierarchically();
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("To map issues hierarchically, the original issues must be provided");
        transformer.transform();
    }
    
    @Test
    public void wrongConfiguration_toSetWorklogs() {
        KPIEnvironmentBuilder builder = getDefaultEnvironment();
        
        buildAlphaSubtask(builder);
        
        IssueKpiTransformer transformer = new IssueKpiTransformer(kpiProperties)
                                                  .withItems(builder.buildAllIssuesAsAdapter())
                                                  .settingWorklog();
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("To map the issues worklogs, the original issues must be provided");
        transformer.transform();
    }
    
    @Test
    public void simpleHierarch_checkWorklogs() {
        
        KPIEnvironmentBuilder builder = new KPIEnvironmentBuilder();
        builder.addStatus(1l,"To Do", false)
                .addStatus(2l,"Doing", true)
                .addStatus(3l,"To Review", false)
                .addStatus(4l,"Reviewing", true)
                .addStatus(5l,"To Alpha", false)
                .addStatus(6l,"Alpha", true)
                .addStatus(7l,"Done", false);
        
        builder.addFeatureType(2l, "Feature")
                .addSubtaskType(3l, "Development");
        
        builder.addStatusHierarchy(KpiLevel.DEMAND, "Doing", "Doing","Reviewing","Alpha");
        
        builder.withMockingIssue("PROJ-01", "Demand", KpiLevel.DEMAND)
                .addTransition("To Do","2020-01-01")
                .addTransition("Doing","2020-01-02")
                .addTransition("To Alpha","2020-01-03")
                .addTransition("Alpha","2020-01-04")
                .addTransition("Done","2020-01-05");
                
        builder.withIssue("PROJ-01").setCurrentStatusToCurrentIssue("Doing");
        
        IssueKpiDataItemAdapter demand = builder.withIssue("PROJ-01").buildCurrentIssueKPIAdapter();
        Issue demandIssue = builder.withIssue("PROJ-01").mockCurrentIssue();

        IssueKpiTransformer transformer = new IssueKpiTransformer(builder.getMockedKPIProperties());
        List<IssueKpi> issuesKpi = transformer
            .withItems(Arrays.asList(demand))
            .withOriginalIssues(Arrays.asList(demandIssue))
            .mappingHierarchically()
            .settingWorklog()
            .transform();
        
        assertThat(issuesKpi.size(), is(1));
        IssueKpi kpi = issuesKpi.get(0);
        
        assertThat(kpi.getIssueKey(),is("PROJ-01"));
        assertThat(kpi.getIssueTypeName(),is("Demand"));
        

    }

    private void buildAlphaSubtask(KPIEnvironmentBuilder builder) {
        builder.withMockingIssue("I-2", "Alpha", KpiLevel.SUBTASKS)
                .setProjectKeyToCurrentIssue("PROJ")
                .setCurrentStatusToCurrentIssue("Done")
                .addTransition("Open", "2020-01-01")
                .addTransition("To Do", "2020-01-02")
                .addTransition("Doing", "2020-01-03")
                .addTransition("Done", "2020-01-04");
    }

    private KPIEnvironmentBuilder getDefaultEnvironment() {
        KPIEnvironmentBuilder builder = new KPIEnvironmentBuilder(kpiProperties);
        builder.addStatus(1l, "Open", false)
                .addStatus(2l, "To Do", false)
                .addStatus(3l, "Doing", true)
                .addStatus(4l, "Done", false);
        
        builder.addSubtaskType(1l, "Dev")
                .addSubtaskType(2l, "Alpha");
        return builder;
    }
    
}
