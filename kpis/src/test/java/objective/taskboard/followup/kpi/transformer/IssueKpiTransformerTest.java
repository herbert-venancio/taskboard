package objective.taskboard.followup.kpi.transformer;

import static java.util.Collections.emptyList;

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
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehavior;
import objective.taskboard.followup.kpi.enviroment.IssuesAsserter;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.utils.Clock;

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

        dsl()
            .environment()
                .givenIssue("I-1")
                    .type("Dev")
                    .isSubtask()
                    .project("PROJ")
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("Done").noDate()
                    .eoT()
                .eoI()
                .givenIssue("I-2")
                    .type("Alpha")
                    .isSubtask()
                    .project("PROJ")
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("Done").date("2020-01-04")
                    .eoT()
                .eoI()
                .todayIs("2020-01-05")
            .eoE()
            .when()
                .appliesBehavior(transformAllIssues())
            .then()
                .amountOfIssueIs(2)
                .givenIssue("I-1")
                    .hasType("Dev")
                    .hasLevel(KpiLevel.SUBTASKS)
                    .atDate("2020-01-01")
                        .isOnStatus("Open")
                        .isNotOnStatus("To Do")
                        .isNotOnStatus("Doing")
                        .isNotOnStatus("Done")
                    .eoDc()
                .eoIA()
                .givenIssue("I-2")
                    .hasType("Alpha")
                    .hasLevel(KpiLevel.SUBTASKS)
                    .atDate("2020-01-04")
                        .isNotOnStatus("Open")
                        .isNotOnStatus("To Do")
                        .isNotOnStatus("Doing")
                        .isOnStatus("Done");
    }

    @Test
    public void wrongConfiguration_toMapHierarchically() {
        dsl()
        .environment()
            .givenIssue("I-1")
                .type("Dev")
                .isSubtask()
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").date("2020-01-02")
                    .status("Doing").date("2020-01-03")
                    .status("Done").noDate()
                .eoT()
            .eoI()
            .givenIssue("I-2")
                .type("Alpha")
                .isSubtask()
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").date("2020-01-02")
                    .status("Doing").date("2020-01-03")
                    .status("Done").date("2020-01-04")
                .eoT()
            .eoI()
            .todayIs("2020-01-05")
        .eoE()
        .when()
            .expectExceptionFromBehavior(
                    transformAllIssues()
                        .mappingHierarchally()
                )
        .then()
            .isFromException(IllegalArgumentException.class)
            .hasMessage("To map issues hierarchically, the original issues must be provided");
    }

    @Test
    public void wrongConfiguration_toSetWorklogs() {
        dsl()
        .environment()
            .givenIssue("I-1")
                .type("Dev")
                .isSubtask()
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").date("2020-01-02")
                    .status("Doing").date("2020-01-03")
                    .status("Done").noDate()
                .eoT()
            .eoI()
            .givenIssue("I-2")
                .type("Alpha")
                .isSubtask()
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").date("2020-01-02")
                    .status("Doing").date("2020-01-03")
                    .status("Done").date("2020-01-04")
                .eoT()
            .eoI()
            .todayIs("2020-01-05")
        .eoE()
        .when()
            .expectExceptionFromBehavior(
                    transformAllIssues()
                        .settingWorklog()
                )
        .then()
            .isFromException(IllegalArgumentException.class)
            .hasMessage("To map the issues worklogs, the original issues must be provided");
    }

    @Test
    public void simpleHierarch_checkWorklogs() {

        dsl()
            .environment()
                .withKpiProperties()
                    .atDemandHierarchy("Doing")
                        .withChildrenStatus("Doing")
                    .eoH()
                    .atDemandHierarchy("Alpha")
                        .withChildrenStatus("Alpha")
                    .eoH()
                    .atFeatureHierarchy("Doing")
                        .withChildrenType("Dev")
                    .eoH()
                    .atFeatureHierarchy("Alpha")
                        .withChildrenType("Alpha")
                    .eoH()
                .eoKP()
                .givenIssue("PROJ-01")
                    .type("Demand")
                    .project("PROJ")
                    .isDemand()
                    .withTransitions()
                        .status("To Do").date("2020-01-01")
                        .status("Doing").date("2020-01-03")
                        .status("To Alpha").date("2020-01-05")
                        .status("Alpha").date("2020-01-07")
                        .status("Done").date("2020-01-09")
                    .eoT()
                .feature("PROJ-02")
                    .type("Feature")
                    .withTransitions()
                        .status("To Do").date("2020-01-01")
                        .status("Doing").date("2020-01-02")
                        .status("To Alpha").date("2020-01-03")
                        .status("Alpha").date("2020-01-04")
                        .status("Done").date("2020-01-05")
                    .eoT()
                    .subtask("PROJ-03")
                        .type("Dev")
                        .withTransitions()
                            .status("To Do").date("2020-01-01")
                            .status("Doing").date("2020-01-02")
                            .status("Done").date("2020-01-05")
                        .eoT()
                        .worklogs()
                            .at("2020-01-02").timeSpentInHours(2.0)
                        .eoW()
                    .endOfSubtask()
                    .subtask("PROJ-04")
                        .type("Alpha")
                        .withTransitions()
                            .status("To Do").date("2020-01-01")
                            .status("Doing").date("2020-01-02")
                            .status("Done").date("2020-01-05")
                        .eoT()
                        .worklogs()
                            .at("2020-01-02").timeSpentInHours(4.0)
                        .eoW()
                    .endOfSubtask()
                .endOfFeature()
            .eoI()
            .todayIs("2020-01-09")
        .when()
            .appliesBehavior(
                        transformAllIssues()
                            .completeTransformation()
                    )
        .then()
            .givenIssue("PROJ-01")
                .hasType("Demand")
                .hasLevel(KpiLevel.DEMAND)
                .atStatus("Doing").hasTotalEffortInHours(2.0).eoSa()
                .atStatus("Alpha").hasTotalEffortInHours(4.0).eoSa()
            .eoIA()
            .givenIssue("PROJ-02")
                .hasType("Feature")
                .hasLevel(KpiLevel.FEATURES)
                .atStatus("Doing").hasTotalEffortInHours(2.0).eoSa()
                .atStatus("Alpha").hasTotalEffortInHours(4.0).eoSa()
            .eoIA()
            .givenIssue("PROJ-03")
                .hasType("Dev")
                .hasLevel(KpiLevel.SUBTASKS)
                .atStatus("Doing").hasTotalEffortInHours(2.0).eoSa()
            .eoIA()
            .givenIssue("PROJ-04")
                .hasType("Alpha")
                .hasLevel(KpiLevel.SUBTASKS)
                .atStatus("Doing").hasTotalEffortInHours(4.0).eoSa()
            .eoIA();
    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .statuses()
                .withProgressingStatuses("Doing","Alpha")
                .withNotProgressingStatuses("Open","To Do","Done","To Alpha")
            .eoS()
            .types()
                .addDemand("Demand")
                .addFeatures("Feature")
                .addSubtasks("Dev","Alpha");
        return dsl;
    }

    private AllIssuesTransformer transformAllIssues(){
        return new AllIssuesTransformer();
    }

    private class AllIssuesTransformer implements DSLSimpleBehavior<IssuesAsserter> {
        private IssuesAsserter issuesAsserter;
        private boolean mappingHierarchally = false;
        private boolean setupWorklog = false;
        private boolean settingOriginalIssues = false;

        @Override
        public void behave(KpiEnvironment environment) {
            Clock clock = environment.getClock();

            IssueKpiTransformer transformer = getTransformer(environment, clock);

            List<IssueKpi> issuesKpi = transformer.transform();
            this.issuesAsserter = new IssuesAsserter(issuesKpi, environment);
        }

        private IssueKpiTransformer getTransformer(KpiEnvironment environment, Clock clock) {
            KPIProperties kpiProperties = environment.getKPIProperties();
            IssueKpiTransformer transformer = new IssueKpiTransformer(kpiProperties,clock);

            transformer.withItems(environment.getAllIssuesAdapters());
            transformer.withOriginalIssues(getOriginalIssues(environment));

            if(mappingHierarchally)
                transformer.mappingHierarchically();
            if(setupWorklog)
                transformer.settingWorklog();

            return transformer;
        }

        private List<Issue> getOriginalIssues(KpiEnvironment environment) {
            return settingOriginalIssues ? environment.mockAllIssues().collectIssuesMocked() : emptyList();
        }

        public AllIssuesTransformer completeTransformation() {
            settingWorklog();
            mappingHierarchally();
            this.settingOriginalIssues = true;
            return this;
        }

        public AllIssuesTransformer settingWorklog() {
            this.setupWorklog  = true;
            return this;
        }

        public AllIssuesTransformer mappingHierarchally() {
            this.mappingHierarchally  = true;
            return this;
        }

        @Override
        public IssuesAsserter then() {
            return issuesAsserter;
        }

    }
}
