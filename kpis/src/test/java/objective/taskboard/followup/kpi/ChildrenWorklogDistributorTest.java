package objective.taskboard.followup.kpi;

import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import objective.taskboard.followup.kpi.enviroment.DSLBehavior;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.KPIEnvironmentBuilder;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;

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
    
        ChildrenWorklogDistributor.distributeWorklogs(builder.getMockedKPIProperties().getFeaturesHierarchy(), kpi);
    
        assertThat(kpi.getEffort("Developing"),is(300l));
        assertThat(kpi.getEffort("Alpha"),is(400l));
    }

    @Test
    public void distribution_whenDistributingEffortByTypeAndStatus_thenShouldIgnoreStatusToAvoidDuplicity() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .withStatus("Open").isNotProgressing()
            .withStatus("To Plan").isNotProgressing()
            .withStatus("Planning").isProgressing()
            .withStatus("To Dev").isNotProgressing()
            .withStatus("Developing").isProgressing()
    
            .withStatus("To Do").isNotProgressing()
            .withStatus("Doing").isProgressing()
            .withStatus("To Review").isNotProgressing()
            .withStatus("Reviewing").isProgressing()
    
            .withStatus("Done").isNotProgressing()
    
            .withFeatureType("Task")
            .withSubtaskType("Backend Development")
            .withSubtaskType("Tech Analysis")
    
            .todayIs("2018-12-17")
            .givenIssue("I-1")
                .isFeature()
                .type("Task")
                .withTransitions()
                    .status("Open").date("2018-12-10")
                    .status("To Plan").date("2018-12-10")
                    .status("Planning").date("2018-12-10")
                    .status("To Dev").date("2018-12-11")
                    .status("Developing").date("2018-12-11")
                    .status("Done").date("2018-12-14")
                .eoT()
                .subtask("I-2")
                    .isSubtask()
                    .type("Tech Analysis")
                    .withTransitions()
                        .status("Open").date("2018-12-10")
                        .status("To Do").date("2018-12-10")
                        .status("Doing").date("2018-12-10")
                        .status("Done").date("2018-12-11")
                    .eoT()
                    .worklogs()
                        .at("2018-12-10").timeSpentInHours(8.0)
                        .at("2018-12-11").timeSpentInHours(4.0)
                    .eoW()
                .endOfSubtask()
                .subtask("I-3")
                    .isSubtask()
                    .type("Backend Development")
                    .withTransitions()
                        .status("Open").date("2018-12-10")
                        .status("To Do").date("2018-12-10")
                        .status("Doing").date("2018-12-11")
                        .status("To Review").date("2018-12-13")
                        .status("Reviewing").date("2018-12-13")
                        .status("Done").date("2018-12-13")
                    .eoT()
                    .worklogs()
                        .at("2018-12-11").timeSpentInHours(4.0)
                        .at("2018-12-12").timeSpentInHours(8.0)
                        .at("2018-12-13").timeSpentInHours(8.0)
                    .eoW()
                .endOfSubtask()
            .eoI()
            .givenKpiProperties()
                .atFeatureHierarchy("Planning").putChildrenType("Tech Analysis").and()
                .atFeatureHierarchy("Developing").putChildrenStatus("Doing").putChildrenStatus("Reviewing")
            .eoKp()
            .when()
                .issueKpi("I-1")
                .appliesBehavior(distributingWorklogsFromChildren())
            .then()
                .assertThat()
                    .issueKpi("I-1")
                    .atStatus("Planning").hasTotalEffortInHours(12.0).eoSa()
                    .atStatus("Developing").hasTotalEffortInHours(20.0);
    }

    private DistributeWorklogBehavior distributingWorklogsFromChildren() {
        return new DistributeWorklogBehavior();
    }

    private class DistributeWorklogBehavior implements DSLBehavior {

        @Override
        public void execute(KpiEnvironment environment, IssueKpi issueKpi) {
            ChildrenWorklogDistributor.distributeWorklogs(environment.getKPIProperties().getFeaturesHierarchy(), issueKpi);
        }

    }

}
