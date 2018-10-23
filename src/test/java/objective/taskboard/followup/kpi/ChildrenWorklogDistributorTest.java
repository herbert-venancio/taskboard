package objective.taskboard.followup.kpi;

import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import objective.taskboard.followup.kpi.enviroment.KPIEnvironmentBuilder;

public class ChildrenWorklogDistributorTest {
    
    @Test
    public void distribution_happyDay() {
        KPIEnvironmentBuilder builder = new KPIEnvironmentBuilder();
        builder.addFeatureType(1l, "Feature")
               .addSubtaskType(2l, "Dev")
               .addSubtaskType(3l, "Alpha");
        
        builder.addSubtaskHierarchy(FEATURES,"Developing", "Dev")
               .addSubtaskHierarchy(FEATURES,"Alpha", "Alpha");
        
        builder.addStatus(1l,"Open", false)
               .addStatus(2l,"To Dev", false)
               .addStatus(3l,"Developing",true)
               .addStatus(4l,"To Alpha", false)
               .addStatus(5l,"Alpha", true)
               .addStatus(6l,"To Do", false)
               .addStatus(7l,"Doing", true)
               .addStatus(8l,"Done", false);
        
        builder.withMockingIssue("PROJ-01", "Feature", KpiLevel.FEATURES)
                .addTransition("Open","2020-01-01")
                .addTransition("To Dev","2020-01-05")
                .addTransition("Developing","2020-01-06")
                .addTransition("To Alpha","2020-01-07")
                .addTransition("Alpha","2020-01-08")
                .addTransition("Done","2020-01-09");
        
        builder.withMockingIssue("PROJ-02", "Dev", KpiLevel.SUBTASKS)
                .addTransition("To Do","2020-01-04")
                .addTransition("Doing","2020-01-06")
                .addTransition("Done","2020-01-08");
        
        builder.withMockingIssue("PROJ-03", "Alpha", KpiLevel.SUBTASKS)
                .addTransition("To Do","2020-01-04")
                .addTransition("Doing","2020-01-06")
                .addTransition("Done","2020-01-08");
        
        builder.withIssue("PROJ-02").addWorklog("2020-01-08", 300);
        builder.withIssue("PROJ-03").addWorklog("2020-01-08", 400);
        
        builder.withIssue("PROJ-01")
                .addChildren("PROJ-02","PROJ-03");
        
        IssueKpi kpi = builder.withIssue("PROJ-01").buildCurrentIssueAsKpi();
        ChildrenWorklogDistributor subject = new ChildrenWorklogDistributor(builder.getMockedKPIProperties().getFeaturesHierarchy());
        
        subject.distributeWorklogs(kpi);
        assertThat(kpi.getEffort("Developing"),is(300l));
        assertThat(kpi.getEffort("Alpha"),is(400l));
    }
    
    @Test
    public void distribution_fullConfig() {
        KPIEnvironmentBuilder builder = new KPIEnvironmentBuilder();
        builder.addFeatureType(1l, "Feature")
               .addSubtaskType(2l, "Dev")
               .addSubtaskType(3l, "Alpha");
        
        builder.addSubtaskHierarchy(FEATURES,"Developing", "Dev")
               .addSubtaskHierarchy(FEATURES,"Alpha", "Alpha")
               .addStatusHierarchy(FEATURES,"To Alpha","Doing");
        
        builder.addStatus(1L,"Open", false)
               .addStatus(2L,"To Dev", false)
               .addStatus(3L,"Developing",true)
               .addStatus(4L,"To Alpha", false)
               .addStatus(5L,"Alpha", true)
               .addStatus(6L,"To Do", false)
               .addStatus(7L,"Doing", true)
               .addStatus(8L,"Done", false);
        
        builder.withMockingIssue("PROJ-01", "Feature", KpiLevel.FEATURES)
                .addTransition("Open","2020-01-01")
                .addTransition("To Dev","2020-01-05")
                .addTransition("Developing","2020-01-06")
                .addTransition("To Alpha","2020-01-07")
                .addTransition("Alpha","2020-01-08")
                .addTransition("Done","2020-01-09");
        
        builder.withMockingIssue("PROJ-02", "Dev", KpiLevel.SUBTASKS)
                .addTransition("To Do","2020-01-04")
                .addTransition("Doing","2020-01-06")
                .addTransition("Done","2020-01-08");
                
        
        builder.withMockingIssue("PROJ-03", "Alpha", KpiLevel.SUBTASKS)
                .addTransition("To Do","2020-01-04")
                .addTransition("Doing","2020-01-06")
                .addTransition("Done","2020-01-08");
        
        builder.withIssue("PROJ-02").addWorklog("2020-01-08", 300);
        builder.withIssue("PROJ-03").addWorklog("2020-01-08", 400);
        
        builder.withIssue("PROJ-01")
                .addChildren("PROJ-02","PROJ-03");
        
        IssueKpi kpi = builder.withIssue("PROJ-01").buildCurrentIssueAsKpi();
        ChildrenWorklogDistributor subject = new ChildrenWorklogDistributor(builder.getMockedKPIProperties().getFeaturesHierarchy());
        
        subject.distributeWorklogs(kpi);
        assertThat(kpi.getEffort("Developing"),is(300l));
        assertThat(kpi.getEffort("Alpha"),is(400l));
        assertThat(kpi.getEffort("To Alpha"),is(700l));
    }

}
