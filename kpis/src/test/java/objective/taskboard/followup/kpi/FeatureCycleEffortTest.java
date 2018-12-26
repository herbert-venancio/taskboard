package objective.taskboard.followup.kpi;

import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.kpi.enviroment.KPIEnvironmentBuilder;

@RunWith(MockitoJUnitRunner.class)
public class FeatureCycleEffortTest {

    private KPIEnvironmentBuilder builder = new KPIEnvironmentBuilder();
    
    @Before
    public void setupEnviroment() {
        configureTypes(builder);
        configureFeatureStatuses(builder);
        configureSubtasksStatuses(builder);
        configureHierarchy(builder);
    }

    @Test
    public void happyDay() {
        
        builder.withMockingIssue("PROJ-01", "Feature", KpiLevel.FEATURES)
                .addTransition("Open","2020-01-01")
                .addTransition("To Plan","2020-01-02")
                .addTransition("Planning","2020-01-03")
                .addTransition("To Dev","2020-01-04")
                .addTransition("Developing","2020-01-05")
                .addTransition("To Internal QA","2020-01-06")
                .addTransition("Internal QAing","2020-01-07")
                .addTransition("To Deploy","2020-01-08")
                .addTransition("To QA","2020-01-09")
                .addTransition("QAing","2020-01-10")
                .addTransition("Done","2020-01-11")
                .addTransition("Cancelled");

        
        createSubtaskWithWorklog(builder,"PROJ-02","Feature Planning",200);
        createSubtaskWithWorklog(builder,"PROJ-03","Tech Planning",300);
        
        createSubtaskWithWorklog(builder,"PROJ-04","Alpha Bug",400);
        createSubtaskWithWorklog(builder,"PROJ-05","Sub-Task",500);
        createSubtaskWithWorklog(builder,"PROJ-06","Backend Development",600);
        createSubtaskWithWorklog(builder,"PROJ-07","Frontend Development",700);
        createSubtaskWithWorklog(builder,"PROJ-08","UX",800);
        
        createSubtaskWithWorklog(builder,"PROJ-09","Alpha Test",900);
        createSubtaskWithWorklog(builder,"PROJ-10","Functional Test",1000);
        createSubtaskWithWorklog(builder,"PROJ-11","Feature Review",1150);
        
        createSubtaskWithWorklog(builder,"PROJ-12","QA",1200);
        
                
        builder.withIssue("PROJ-01")
                .addChildren("PROJ-02","PROJ-03","PROJ-04","PROJ-05","PROJ-06","PROJ-07","PROJ-08")
                .addChildren("PROJ-09","PROJ-10","PROJ-11","PROJ-12");

        IssueKpi kpi = builder.withIssue("PROJ-01").buildCurrentIssueAsKpi();
        
        
        ChildrenWorklogDistributor.distributeWorklogs(builder.getMockedKPIProperties().getFeaturesHierarchy(), kpi);
        
        assertStatusWorklog(kpi.findStatus("Open"),0l);
        assertStatusWorklog(kpi.findStatus("To Plan"),0l);
        assertStatusWorklog(kpi.findStatus("Planning"),500l);
        assertStatusWorklog(kpi.findStatus("To Dev"),0l);
        assertStatusWorklog(kpi.findStatus("Developing"),3000l);
        assertStatusWorklog(kpi.findStatus("To Internal QA"),0l);
        assertStatusWorklog(kpi.findStatus("Internal QAing"),3050l);
        assertStatusWorklog(kpi.findStatus("To Deploy"),0l);
        assertStatusWorklog(kpi.findStatus("To QA"),0l);
        assertStatusWorklog(kpi.findStatus("QAing"),1200l);
        assertStatusWorklog(kpi.findStatus("Done"),0l);
        assertStatusWorklog(kpi.findStatus("Cancelled"),0l);
        
        
    }
    
    @Test
    public void variousSubtasksStatuses() {
        
        builder.withMockingIssue("PROJ-01", "Feature", KpiLevel.FEATURES)
                .addTransition("Open","2020-01-01")
                .addTransition("To Plan","2020-01-02")
                .addTransition("Planning","2020-01-03")
                .addTransition("To Dev","2020-01-04")
                .addTransition("Developing","2020-01-05")
                .addTransition("To Internal QA","2020-01-06")
                .addTransition("Internal QAing","2020-01-07")
                .addTransition("To Deploy","2020-01-08")
                .addTransition("To QA","2020-01-09")
                .addTransition("QAing","2020-01-10")
                .addTransition("Done","2020-01-11")
                .addTransition("Cancelled");

        //Open Issue
        builder.withMockingIssue("PROJ-02", "Feature Planning", KpiLevel.SUBTASKS)
                .addTransition("Open","2020-01-01")
                .addTransition("To Do")
                .addTransition("Doing")
                .addTransition("To Review")
                .addTransition("Reviewing")
                .addTransition("To Merge")
                .addTransition("Merging")
                .addTransition("Done")
                .addTransition("Cancelled");
        builder.withIssue("PROJ-02").addWorklog("2020-01-03",100);
                
        //Straight to close
        builder.withMockingIssue("PROJ-03", "Alpha Bug", KpiLevel.SUBTASKS)
                .addTransition("Open","2020-01-01")
                .addTransition("To Do")
                .addTransition("Doing","2020-01-03")
                .addTransition("To Review")
                .addTransition("Reviewing")
                .addTransition("To Merge")
                .addTransition("Merging")
                .addTransition("Done","2020-01-03")
                .addTransition("Cancelled");
        
        builder.withIssue("PROJ-03").addWorklog("2020-01-03",100);
        
        //Skipping queue
        builder.withMockingIssue("PROJ-04", "Backend Development", KpiLevel.SUBTASKS)
                .addTransition("Open","2020-01-01")
                .addTransition("To Do")
                .addTransition("Doing","2020-01-03")
                .addTransition("To Review")
                .addTransition("Reviewing","2020-01-04")
                .addTransition("To Merge")
                .addTransition("Merging")
                .addTransition("Done","2020-01-05")
                .addTransition("Cancelled");
        
        builder.withIssue("PROJ-04").addWorklog("2020-01-04",100);
        
        //Two progressing status same day
        builder.withMockingIssue("PROJ-05", "Alpha Test", KpiLevel.SUBTASKS)
                .addTransition("Open","2020-01-01")
                .addTransition("To Do")
                .addTransition("Doing","2020-01-03")
                .addTransition("To Review")
                .addTransition("Reviewing","2020-01-03")
                .addTransition("To Merge")
                .addTransition("Merging")
                .addTransition("Done","2020-01-05")
                .addTransition("Cancelled");
        builder.withIssue("PROJ-05").addWorklog("2020-01-03",100);
                
        builder.withIssue("PROJ-01")
                .addChildren("PROJ-02","PROJ-03","PROJ-04","PROJ-05");

        IssueKpi kpi = builder.withIssue("PROJ-01").buildCurrentIssueAsKpi();
        

        ChildrenWorklogDistributor.distributeWorklogs(builder.getMockedKPIProperties().getFeaturesHierarchy(), kpi);
        
        assertStatusWorklog(kpi.findStatus("Open"),0l);
        assertStatusWorklog(kpi.findStatus("To Plan"),0l);
        assertStatusWorklog(kpi.findStatus("Planning"),100l);
        assertStatusWorklog(kpi.findStatus("To Dev"),0l);
        assertStatusWorklog(kpi.findStatus("Developing"),200l);
        assertStatusWorklog(kpi.findStatus("To Internal QA"),0l);
        assertStatusWorklog(kpi.findStatus("Internal QAing"),100l);
        assertStatusWorklog(kpi.findStatus("To Deploy"),0l);
        assertStatusWorklog(kpi.findStatus("To QA"),0l);
        assertStatusWorklog(kpi.findStatus("QAing"),0l);
        assertStatusWorklog(kpi.findStatus("Done"),0l);
        assertStatusWorklog(kpi.findStatus("Cancelled"),0l);
    }
    
    @Test
    public void multipleWorklogs() {

        builder.withMockingIssue("PROJ-01", "Feature", KpiLevel.FEATURES)
                .addTransition("Open","2020-01-01")
                .addTransition("To Plan","2020-01-02")
                .addTransition("Planning","2020-01-03")
                .addTransition("To Dev","2020-01-04")
                .addTransition("Developing","2020-01-05")
                .addTransition("To Internal QA","2020-01-06")
                .addTransition("Internal QAing","2020-01-07")
                .addTransition("To Deploy","2020-01-08")
                .addTransition("To QA","2020-01-09")
                .addTransition("QAing","2020-01-10")
                .addTransition("Done","2020-01-11")
                .addTransition("Cancelled");

        //Open Issue
        builder.withMockingIssue("PROJ-02", "Feature Planning", KpiLevel.SUBTASKS)
                .addTransition("Open","2020-01-01")
                .addTransition("To Do")
                .addTransition("Doing")
                .addTransition("To Review")
                .addTransition("Reviewing")
                .addTransition("To Merge")
                .addTransition("Merging")
                .addTransition("Done")
                .addTransition("Cancelled");
        
        builder.withIssue("PROJ-02")
                .addWorklog("2020-01-03",100)
                .addWorklog("2020-01-06", 300);
        
                
        builder.withIssue("PROJ-01")
                .addChildren("PROJ-02");

        IssueKpi kpi = builder.withIssue("PROJ-01").buildCurrentIssueAsKpi();
        
        ChildrenWorklogDistributor.distributeWorklogs(builder.getMockedKPIProperties().getFeaturesHierarchy(), kpi);
        
        assertStatusWorklog(kpi.findStatus("Open"),0l);
        assertStatusWorklog(kpi.findStatus("To Plan"),0l);
        assertStatusWorklog(kpi.findStatus("Planning"),400l);
        assertStatusWorklog(kpi.findStatus("To Dev"),0l);
        assertStatusWorklog(kpi.findStatus("Developing"),0l);
        assertStatusWorklog(kpi.findStatus("To Internal QA"),0l);
        assertStatusWorklog(kpi.findStatus("Internal QAing"),0l);
        assertStatusWorklog(kpi.findStatus("To Deploy"),0l);
        assertStatusWorklog(kpi.findStatus("To QA"),0l);
        assertStatusWorklog(kpi.findStatus("QAing"),0l);
        assertStatusWorklog(kpi.findStatus("Done"),0l);
        assertStatusWorklog(kpi.findStatus("Cancelled"),0l);
    }
    

    private void createSubtaskWithWorklog(KPIEnvironmentBuilder builder, String pkey, String type, int time) {
        builder.withMockingIssue(pkey, type, KpiLevel.SUBTASKS)
                .addTransition("Open","2020-01-01")
                .addTransition("To Do","2020-01-02")
                .addTransition("Doing","2020-01-03")
                .addTransition("To Review","2020-01-04")
                .addTransition("Reviewing","2020-01-05")
                .addTransition("To Merge","2020-01-06")
                .addTransition("Merging","2020-01-07")
                .addTransition("Done","2020-01-11")
                .addTransition("Cancelled");
       builder.addWorklog("2020-01-03",time);
    }
    
    private void assertStatusWorklog(Optional<StatusTransition> statusTransition, Long totalEffort) {
        assertEquals(totalEffort, statusTransition.map(s -> s.getEffort()).orElse(0l));
    }

    private void configureFeatureStatuses(KPIEnvironmentBuilder builder) {
        builder.addStatus(1l,"Open",false)
                .addStatus(2l,"To Plan",false)
                .addStatus(3l,"Planning",true)
                .addStatus(4l,"To Dev",false)
                .addStatus(5l,"Developing",true)
                .addStatus(6l,"To Internal QA",false)
                .addStatus(7l,"Internal QAing",true)
                .addStatus(8l,"To Deploy",false)
                .addStatus(9l,"To QA",false)
                .addStatus(10l,"QAing",true)
                .addStatus(11l,"Done",false)
                .addStatus(12l,"Cancelled",false);
    }
    
    private void configureSubtasksStatuses(KPIEnvironmentBuilder builder) {
        builder.addStatus(1l,"To Do",false)
                .addStatus(2l,"Doing",true)
                .addStatus(3l,"To Review",false)
                .addStatus(4l,"Reviewing",true)
                .addStatus(5l,"To Merge",false)
                .addStatus(6l,"Merging",true);
    }

    private void configureHierarchy(KPIEnvironmentBuilder builder) {
        builder.addSubtaskHierarchy(FEATURES,"QAing","QA")
                .addSubtaskHierarchy(FEATURES,"Internal QAing","Alpha Test", "Functional Test", "Feature Review")
                .addSubtaskHierarchy(FEATURES,"Developing","Alpha Bug", "Sub-Task","Backend Development", "Frontend Development", "UX")
                .addSubtaskHierarchy(FEATURES,"Planning","Feature Planning", "Tech Planning");
    }

    private void configureTypes(KPIEnvironmentBuilder builder) {
        builder.addFeatureType(2l, "Feature")
                .addSubtaskType(3l, "QA")
                .addSubtaskType(4l, "UAT")
                .addSubtaskType(5l, "Alpha Test")
                .addSubtaskType(6l, "Functional Test")
                .addSubtaskType(7l, "Feature Review")
                .addSubtaskType(8l, "Alpha Bug")
                .addSubtaskType(9l, "Sub-Task")
                .addSubtaskType(10l, "Backend Development")
                .addSubtaskType(11l, "Frontend Development")
                .addSubtaskType(12l, "UX")
                .addSubtaskType(13l, "Feature Planning")
                .addSubtaskType(14l, "Tech Planning");
    }

}
