package objective.taskboard.followup.kpi.cycletime;

import static objective.taskboard.followup.kpi.properties.KpiCycleTimePropertiesMocker.withSubtaskCycleTimeProperties;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import objective.taskboard.domain.IssueColorService;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.followup.kpi.properties.KpiCycleTimeProperties;

public class CycleTimeDataProviderTest {

    @Test
    public void getDataSet_happyDay() {
        dsl()
        .environment()
            .givenSubtask("I-1")
                .type("Development")
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2019-01-01")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").date("2019-01-03")
                    .status("To Review").date("2019-01-04")
                    .status("Reviewing").date("2019-01-05")
                    .status("Done").date("2019-01-06")
                    .status("Cancelled").noDate()
                .eoT()
            .eoI()
            .givenSubtask("I-2")
                .type("Development")
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2019-01-03")
                    .status("To Do").date("2019-01-04")
                    .status("Doing").date("2019-01-05")
                    .status("To Review").date("2019-01-06")
                    .status("Reviewing").date("2019-01-07")
                    .status("Done").date("2019-01-08")
                    .status("Cancelled").noDate()
                .eoT()
            .eoI()
            .withKpiProperties(
                withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
            )
        .when()
            .appliesBehavior(generateDataSet("PROJ",KpiLevel.SUBTASKS))
        .then()
            .dataSetHasTotalSize(2)
                .givenIssue("I-1")
                    .hasTotalCycleTime(5l)
                    .startsAt("2019-01-02")
                    .endsAt("2019-01-06")
                    .hasSubCycles()
                        .subCycle("To Do").hasCycleTimeInDays(1l).eoS()
                        .subCycle("Doing").hasCycleTimeInDays(1l).eoS()
                        .subCycle("To Review").hasCycleTimeInDays(1l).eoS()
                        .subCycle("Reviewing").hasCycleTimeInDays(1l).eoS()
                    .eoSC()
                .eoCK()
                .givenIssue("I-2")
                    .hasTotalCycleTime(5l)
                    .startsAt("2019-01-04")
                    .endsAt("2019-01-08")
                    .hasSubCycles()
                        .subCycle("To Do").hasCycleTimeInDays(1l).eoS()
                        .subCycle("Doing").hasCycleTimeInDays(1l).eoS()
                        .subCycle("To Review").hasCycleTimeInDays(1l).eoS()
                        .subCycle("Reviewing").hasCycleTimeInDays(1l).eoS()
                    .eoSC()
                .eoCK();
    }

    @Test
    public void getDataSet_whenNoIssues_thenEmptyDataSet() {
        dsl()
        .environment()
            .withKpiProperties(
                withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
            )
        .when()
            .appliesBehavior(generateDataSet("PROJ",KpiLevel.SUBTASKS))
        .then()
            .emptyDataSet();
    }

    @Test
    public void getDataSet_whenNoIssuesForLevel_thenEmptyDataSet(){
        dsl()
        .environment()
            .givenDemand("I-1")
                .type("Demand")
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2019-01-01")
                    .status("To Do").noDate()
                    .status("Doing").noDate()
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").noDate()
                    .status("Cancelled").noDate()
                .eoT()
            .eoI()
            .withKpiProperties(
                withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
            )
        .when()
            .appliesBehavior(generateDataSet("PROJ",KpiLevel.SUBTASKS))
        .then()
            .emptyDataSet();
    }

    @Test
    public void getDataSet_whenAllIssuesAreInProgress_thenEmptyDataSet(){
        dsl()
        .environment()
            .givenSubtask("I-1")
                .type("Development")
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2019-01-01")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").date("2019-01-03")
                    .status("To Review").date("2019-01-04")
                    .status("Reviewing").date("2019-01-05")
                    .status("Done").noDate()
                    .status("Cancelled").noDate()
                .eoT()
            .eoI()
            .givenSubtask("I-2")
                .type("Development")
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2019-01-03")
                    .status("To Do").date("2019-01-04")
                    .status("Doing").date("2019-01-05")
                    .status("To Review").date("2019-01-06")
                    .status("Reviewing").date("2019-01-07")
                    .status("Done").noDate()
                    .status("Cancelled").noDate()
                .eoT()
            .eoI()
            .withKpiProperties(
                withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
            )
        .when()
            .appliesBehavior(generateDataSet("PROJ",KpiLevel.SUBTASKS))
        .then()
            .emptyDataSet();
    }

    @Test
    public void getDataSet_whenCyclePropertiesUnconfigured_thenEmptyDataSet(){
        dsl()
        .environment()
            .givenSubtask("I-1")
                .type("Development")
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2019-01-01")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").date("2019-01-03")
                    .status("To Review").date("2019-01-04")
                    .status("Reviewing").date("2019-01-05")
                    .status("Done").noDate()
                    .status("Cancelled").noDate()
                .eoT()
            .eoI()
            .givenSubtask("I-2")
                .type("Development")
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2019-01-03")
                    .status("To Do").date("2019-01-04")
                    .status("Doing").date("2019-01-05")
                    .status("To Review").date("2019-01-06")
                    .status("Reviewing").date("2019-01-07")
                    .status("Done").noDate()
                    .status("Cancelled").noDate()
                .eoT()
            .eoI()
            .withKpiProperties(
                withSubtaskCycleTimeProperties()
            )
        .when()
            .appliesBehavior(generateDataSet("PROJ",KpiLevel.SUBTASKS))
        .then()
            .emptyDataSet();
    }

    @Test
    public void getDataSet_whenCyclePropertiesMisconfigured_thenEmptyDataSet(){
        dsl()
        .environment()
            .givenSubtask("I-1")
                .type("Development")
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2019-01-01")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").date("2019-01-03")
                    .status("To Review").date("2019-01-04")
                    .status("Reviewing").date("2019-01-05")
                    .status("Done").noDate()
                    .status("Cancelled").noDate()
                .eoT()
            .eoI()
            .givenSubtask("I-2")
                .type("Development")
                .project("PROJ")
                .withTransitions()
                    .status("Open").date("2019-01-03")
                    .status("To Do").date("2019-01-04")
                    .status("Doing").date("2019-01-05")
                    .status("To Review").date("2019-01-06")
                    .status("Reviewing").date("2019-01-07")
                    .status("Done").noDate()
                    .status("Cancelled").noDate()
                .eoT()
            .eoI()
            .withKpiProperties(
                withSubtaskCycleTimeProperties("Foo")
            )
        .when()
            .appliesBehavior(generateDataSet("PROJ",KpiLevel.SUBTASKS))
        .then()
            .emptyDataSet();
    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .services()
                .projects()
                    .withKey("PROJ")
                    .eoP()
                .eoPs()
            .eoS()
            .types()
                .addDemand("Demand")
                .addFeatures("Feature")
                .addSubtasks("Development")
            .eoT()
            .statuses()
                .withProgressingStatuses("Doing","Reviewing","Planning")
                .withNotProgressingStatuses("Open","To Do","To Plan","To Review","Cancelled","Done")
            .eoS();
        return dsl;
    }

    private GenerateCycleData generateDataSet(String projectKey, KpiLevel level) {
        return new GenerateCycleData(projectKey,level);
    }

    private class GenerateCycleData implements DSLSimpleBehaviorWithAsserter<CycleTimeKpiDataAsserter>{

        private String projectKey;
        private KpiLevel level;
        private CycleTimeKpiDataAsserter asserter;

        public GenerateCycleData(String projectKey, KpiLevel level) {
            this.projectKey = projectKey;
            this.level = level;
        }

        @Override
        public void behave(KpiEnvironment environment) {

            IssueKpiService issueKpiService = environment.services().issueKpi().getService();
            KpiCycleTimeProperties properties = environment.getKPIProperties(KpiCycleTimeProperties.class);
            IssueColorService colorService = environment.services().issueColor().getService();
            ZoneId timezone = environment.getTimezone();

            CycleTimeDataProvider subject = new CycleTimeDataProvider(issueKpiService, properties, colorService);
            List<CycleTimeKpi> dataSet = subject.getDataSet(projectKey, level, timezone);
            asserter = new CycleTimeKpiDataAsserter(dataSet);
        }

        @Override
        public CycleTimeKpiDataAsserter then() {
            return asserter;
        }
    }

    private class CycleTimeKpiDataAsserter {

        private List<CycleTimeKpi> dataSet;

        public CycleTimeKpiDataAsserter(List<CycleTimeKpi> dataSet) {
            this.dataSet = dataSet;
        }

        public void emptyDataSet() {
            assertThat(dataSet).hasSize(0);
        }

        public CycleTimeKpiAsserter<CycleTimeKpiDataAsserter> givenIssue(String pkey) {
            Optional<CycleTimeKpi> opKpi = dataSet.stream().filter(c -> c.getIssueKey().equals(pkey)).findFirst();
            assertThat(opKpi).as("Cycle Kpi for issue %s not found.",pkey).isPresent();
            return new CycleTimeKpiAsserter<CycleTimeKpiDataAsserter>(opKpi.get(), this);
        }

        public CycleTimeKpiDataAsserter dataSetHasTotalSize(int size) {
            assertThat(dataSet).hasSize(size);
            return this;
        }

    }

}
