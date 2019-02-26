package objective.taskboard.followup.kpi;

import org.junit.Test;

import objective.taskboard.followup.kpi.enviroment.DSLBehavior;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.IssueKpiMocker;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;

public class ChildrenWorklogDistributorTest {

    @Test
    public void distribution_whenDistributingEffortByTypesOnly_thenShouldDistributeCorrectly() {
        defaultEnvironment()
        .withKpiProperties()
            .atFeatureHierarchy("Planning")
                .withChildrenType("Tech Analysis")
             .eoH()
            .atFeatureHierarchy("Developing")
                .withChildrenType("Backend Development")
            .eoH()
        .eoKP()
        .givenFeature("I-1")
            .project("PROJ")
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
        .when()
            .givenIssueKpi("I-1")
            .appliesBehavior(distributingWorklogsFromChildren())
        .then()
            .assertThat()
                .issueKpi("I-1")
                .atStatus("Planning").hasTotalEffortInHours(12.0).eoSa()
                .atStatus("Developing").hasTotalEffortInHours(20.0);
    }

    @Test
    public void distribution_whenDistributingEffortByStatusAndType_thenHappyPath() {
        defaultEnvironment()
        .withKpiProperties()
            .atFeatureHierarchy("Developing")
                .withChildrenStatus("Doing")
                .withChildrenStatus("Reviewing")
            .eoH()
            .atFeatureHierarchy("Planning")
                .withChildrenType("Tech Analysis")
            .eoH()
        .eoKP()
        .givenIssue("I-1")
            .project("PROJ")
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
        .when()
            .givenIssueKpi("I-1")
            .appliesBehavior(distributingWorklogsFromChildren())
        .then()
            .assertThat()
                .issueKpi("I-1")
                    .atStatus("Planning").hasTotalEffortInHours(12.0).eoSa()
                    .atStatus("Developing").hasTotalEffortInHours(20.0);
    }

    @Test
    public void distribution_whenDistributingEffortByTypeAndStatus_thenShouldIgnoreStatusToAvoidDuplicity() {
        defaultEnvironment()
        .withKpiProperties()
            .atFeatureHierarchy("Planning")
                .withChildrenType("Tech Analysis")
            .eoH()
            .atFeatureHierarchy("Developing")
                .withChildrenStatus("Doing")
                .withChildrenStatus("Reviewing")
            .eoH()
        .eoKP()
        .givenFeature("I-1")
            .project("PROJ")
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
        .when()
            .givenIssueKpi("I-1")
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

    private KpiEnvironment defaultEnvironment() {
        return new DSLKpi().environment()
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

            .todayIs("2018-12-17");
    }

    private class DistributeWorklogBehavior implements DSLBehavior<IssueKpiMocker> {

        @Override
        public void execute(KpiEnvironment environment, IssueKpiMocker issueKpiMocker) {
            IssueKpi issueKpi = issueKpiMocker.preventWorklogDistribution().buildIssueKpi();
            ChildrenWorklogDistributor.distributeWorklogs(environment.getKPIProperties().getFeaturesHierarchy(), issueKpi);
        }

    }

}
