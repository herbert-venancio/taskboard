package objective.taskboard.followup.kpi.services;

import static objective.taskboard.followup.kpi.KpiLevel.DEMAND;
import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;
import static objective.taskboard.followup.kpi.KpiLevel.SUBTASKS;
import static objective.taskboard.followup.kpi.KpiLevel.UNMAPPED;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.services.DSLKpi;
import objective.taskboard.followup.kpi.services.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.services.IssueKpiService;
import objective.taskboard.followup.kpi.services.IssuesAsserter;
import objective.taskboard.followup.kpi.services.KpiEnvironment;
import objective.taskboard.followup.kpi.services.snapshot.AnalyticsDataSetsGenerator;

public class IssueKpiServiceTest {

    @Test
    public void getIssues_analyticSet() {
        dslToAnalyticSets()
            .environment()
                .givenFeature("I-2")
                    .project("PROJ")
                    .type("Feature")
                    .withTransitions()
                        .status("To Do").date("2017-09-25")
                        .status("Doing").date("2017-09-26")
                        .status("Done").noDate()
                    .eoT()
                .eoI()
            .when()
                .appliesBehavior(getIssuesFromDataSet(FEATURES))
            .then()
                .amountOfIssueIs(1)
                .givenIssue("I-2")
                    .hasLevel(KpiLevel.FEATURES)
                    .hasType("Feature")
                    .atDate("2017-09-27").isOnStatus("Doing");
    }

    

    @Test
    public void getIssues_currentState() {
        KpiEnvironment context = dsl()
            .services()
                .projects()
                    .withKey("PROJ")
                        .startAt("2020-01-04")
                        .deliveredAt("2020-01-10")
                    .eoP()
                .eoPs()
            .eoS()
            .todayIs("2020-01-04")
            .givenFeature("I-1")
                .project("PROJ")
                .type("Task")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").date("2020-01-02")
                    .status("Doing").date("2020-01-03")
                    .status("Done").noDate()
                .eoT()
                .subtask("I-2")
                    .type("Alpha")
                    .project("PROJ")
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("Done").date("2020-01-04")
                    .eoT()
                    .worklogs()
                        .at("2020-01-03").timeSpentInHours(3.0)
                    .eoW()
                .endOfSubtask()
            .eoI();
        context
            .when()
                .appliesBehavior(getIssuesFromCurrentStateForProjectAndLevel("PROJ", DEMAND))
            .then()
                .amountOfIssueIs(0);
        context
            .when()
                .appliesBehavior(getIssuesFromCurrentStateForProjectAndLevel("PROJ", FEATURES))
            .then()
                .amountOfIssueIs(1)
                .givenIssue("I-1")
                    .hasLevel(FEATURES)
                    .hasType("Task")
                    .atDate("2020-01-04").isOnStatus("Doing").eoDc()
                    .atStatus("Doing").hasTotalEffortInHours(3.0).eoSa()
                    .hasChild("I-2")
                .eoIA();
        context
            .when()
                .appliesBehavior(getIssuesFromCurrentStateForProjectAndLevel("PROJ", SUBTASKS))
            .then()
                .amountOfIssueIs(1)
                .givenIssue("I-2")
                    .hasLevel(SUBTASKS)
                    .hasType("Alpha")
                    .atDate("2020-01-04").isOnStatus("Done").eoDc()
                    .atStatus("To Do").hasNoEffort().eoSa()
                    .atStatus("Doing").hasTotalEffortInHours(3.0).eoSa()
                    .atStatus("Done").hasNoEffort().eoSa();
        context
            .when()
                .appliesBehavior(getIssuesFromCurrentStateForProjectAndLevel("PROJ", UNMAPPED))
            .then()
                .amountOfIssueIs(0);
    }

    private ServeIssuesFromDatasets getIssuesFromDataSet(KpiLevel kpiLevel) {
        return new ServeIssuesFromDatasets(kpiLevel);
    }

    private ServeIssuesFromCurrentState getIssuesFromCurrentStateForProjectAndLevel(String projectKey, KpiLevel level) {
        return new ServeIssuesFromCurrentState(projectKey, level);
    }

    private class ServeIssuesFromCurrentState implements DSLSimpleBehaviorWithAsserter<IssuesAsserter> {
        private String projectKey;
        private KpiLevel level;
        private IssuesAsserter asserter;
        
        public ServeIssuesFromCurrentState(String projectKey, KpiLevel level) {
            this.projectKey = projectKey;
            this.level = level;
        }

        @Override
        public void behave(KpiEnvironment environment) {
            IssueKpiService subject = environment.services().issueKpi().buildServiceInstance();
            ZoneId timezone = environment.getTimezone();
            List<IssueKpi> issues = subject.getIssuesFromCurrentState(projectKey, timezone , level);
            this.asserter = new IssuesAsserter(issues, environment);
        }

        @Override
        public IssuesAsserter then() {
            return asserter;
        }
    }
    
    private class ServeIssuesFromDatasets implements DSLSimpleBehaviorWithAsserter<IssuesAsserter> {
        private KpiLevel level;
        private IssuesAsserter asserter;

        public ServeIssuesFromDatasets(KpiLevel level) {
            this.level = level;
        }

        @Override
        public void behave(KpiEnvironment environment) {
            AnalyticsDataSetsGenerator datasetFactory = new AnalyticsDataSetsGenerator(environment);
            Optional<AnalyticsTransitionsDataSet> dataSet = datasetFactory.getOptionalDataSetForLevel(level);
            environment.services().itemAdapterFactory().configureForDataSet(dataSet);
            
            IssueKpiService subject = environment.services().issueKpi().buildServiceInstance();

            this.asserter = new IssuesAsserter(subject.getIssues(dataSet),environment);
        }

        @Override
        public IssuesAsserter then() {
            return this.asserter;
        }
    }

    private KpiEnvironment dsl() {
        return new DSLKpi().environment()
            .withJiraProperties()
                .followUp()
                    .withExcludedStatuses("Open")
                .eof()
            .eoJp()
            .withKpiProperties()
                .atFeatureHierarchy("Doing")
                    .withChildrenType("Alpha")
                .eoH()
            .eoKP()
            .types()
                .addFeatures("Task")
                .addSubtasks("Alpha")
            .eoT()
            .statuses()
                .withProgressingStatuses("Doing")
                .withNotProgressingStatuses("Open", "To Do", "Done")
            .eoS();
    }
    
    private DSLKpi dslToAnalyticSets() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .withStatus("To Do").isNotProgressing()
            .withStatus("Doing").isProgressing()
            .withStatus("Done").isNotProgressing()
            .withDemandType("Demand")
            .withFeatureType("OS")
            .withFeatureType("Feature")
            .withSubtaskType("Subtask")
            .withJiraProperties()
                .withDemandStatusPriorityOrder("Done","Doing","To Do")
                .withFeaturesStatusPriorityOrder("Done","Doing","To Do")
                .withSubtaskStatusPriorityOrder("Done","Doing","To Do")
            .eoJp();
        return dsl;
    }

}
