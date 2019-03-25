package objective.taskboard.followup.kpi.leadTime;

import static objective.taskboard.followup.kpi.properties.KpiLeadTimePropertiesMocker.withSubtaskLeadTimeProperties;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import objective.taskboard.followup.kpi.KpiDataService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.followup.kpi.leadtime.LeadTimeKpi;
import objective.taskboard.followup.kpi.leadtime.LeadTimeKpiDataProvider;
import objective.taskboard.followup.kpi.properties.KpiLeadTimeProperties;

public class LeadTimeKpiDataProviderTest {

    @Test
    public void getDataSet_happyPath() {
        dsl().environment()
            .todayIs("2019-01-08")
            .withKpiProperties(
                withSubtaskLeadTimeProperties("Open", "To Do", "Doing", "To Review", "Reviewing")
            )
            .givenFeature("I-1")
                .project("TASKB")
                .type("Task")
                .withTransitions()
                    .status("Open").date("2019-01-01")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").date("2019-01-03")
                    .status("To Review").date("2019-01-07")
                    .status("Reviewing").date("2019-01-08")
                    .status("Done").noDate()
                    .status("Cancelled").noDate()
                .eoT()
                .subtask("I-2")
                    .type("Backend Development")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-03")
                        .status("To Review").date("2019-01-04")
                        .status("Reviewing").date("2019-01-05")
                        .status("Done").date("2019-01-06")
                        .status("Cancelled").noDate()
                    .eoT()
                .endOfSubtask()
                .subtask("I-3")
                    .type("Backend Development")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-04")
                        .status("To Review").date("2019-01-05")
                        .status("Reviewing").date("2019-01-06")
                        .status("Done").date("2019-01-07")
                        .status("Cancelled").noDate()
                    .eoT()
                .endOfSubtask()
                .subtask("I-4")
                    .type("Backend Development")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").noDate()
                        .status("To Review").noDate()
                        .status("Reviewing").noDate()
                        .status("Done").noDate()
                        .status("Cancelled").date("2019-01-04")
                    .eoT()
                .endOfSubtask()
                .subtask("I-5")
                    .type("Backend Development")
                    .withTransitions()
                        .status("Open").date("2019-01-01")
                        .status("To Do").date("2019-01-02")
                        .status("Doing").date("2019-01-03")
                        .status("To Review").date("2019-01-04")
                        .status("Reviewing").date("2019-01-05")
                        .status("Done").noDate()
                        .status("Cancelled").noDate()
                    .eoT()
                .endOfSubtask()
            .eoI()
        .when()
            .appliesBehavior(generateDataSet("TASKB", KpiLevel.SUBTASKS))
        .then()
            .dataSetHasTotalSize(3)
                .leadTimeForIssue("I-2")
                    .startsAt("2019-01-01")
                    .endsAt("2019-01-06")
                    .hasTotalLeadTime(6)
                    .hasType("Backend Development")
                    .hasLastStatus("Done")
                .eoLTKA()
                .leadTimeForIssue("I-3")
                    .startsAt("2019-01-01")
                    .endsAt("2019-01-07")
                    .hasTotalLeadTime(7)
                    .hasType("Backend Development")
                    .hasLastStatus("Done")
                .eoLTKA()
                .leadTimeForIssue("I-4")
                    .startsAt("2019-01-01")
                    .endsAt("2019-01-04")
                    .hasTotalLeadTime(4)
                    .hasType("Backend Development")
                    .hasLastStatus("Cancelled")
                .eoLTKA();
    }

    @Test
    public void getDataSet_whenNoIssues_thenEmptyDataSet() {
        dsl().environment()
            .withKpiProperties(
                withSubtaskLeadTimeProperties("Open", "To Do", "Doing", "To Review", "Reviewing")
            )
        .when()
            .appliesBehavior(generateDataSet("TASKB", KpiLevel.SUBTASKS))
        .then()
            .emptyDataSet();
    }

    @Test
    public void getDataSet_whenNoIssuesForLevel_thenEmptyDataSet() {
        dsl().environment()
            .todayIs("2019-01-10")
            .withKpiProperties(
                withSubtaskLeadTimeProperties("Open", "To Do", "Doing", "To Review", "Reviewing")
            )
            .givenDemand("I-1")
                .project("TASKB")
                .type("Demand")
                .withTransitions()
                    .status("Open").date("2019-01-01")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").date("2019-01-03")
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").noDate()
                    .status("Cancelled").noDate()
                .eoT()
            .eoI()
        .when()
            .appliesBehavior(generateDataSet("TASKB", KpiLevel.SUBTASKS))
        .then()
            .emptyDataSet();
    }

    @Test
    public void getDataSet_whenAllIssuesAreInProgress_thenEmptyDataSet() {
        dsl().environment()
            .todayIs("2019-01-10")
            .withKpiProperties(
                withSubtaskLeadTimeProperties("Open", "To Do", "Doing", "To Review", "Reviewing")
            )
            .givenDemand("I-1")
                .project("TASKB")
                .type("Demand")
                .withTransitions()
                    .status("Open").date("2019-01-01")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").date("2019-01-03")
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").noDate()
                    .status("Cancelled").noDate()
                .eoT()
            .eoI()
        .when()
            .appliesBehavior(generateDataSet("TASKB", KpiLevel.DEMAND))
        .then()
            .emptyDataSet();
    }

    @Test
    public void getDataSet_whenLeadPropertiesNotConfigured_thenEmptyDataSet() {
        dsl().environment()
            .todayIs("2019-01-10")
            .givenDemand("I-1")
                .project("TASKB")
                .type("Demand")
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
            .withKpiProperties(
                withSubtaskLeadTimeProperties()
            )
        .when()
            .appliesBehavior(generateDataSet("TASKB", KpiLevel.DEMAND))
        .then()
            .emptyDataSet();
    }

    @Test
    public void getDataSet_whenLeadPropertiesMisconfigured_thenEmptyDataSet() {
        dsl().environment()
            .todayIs("2019-01-10")
            .givenDemand("I-1")
                .project("TASKB")
                .type("Demand")
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
            .withKpiProperties(
                withSubtaskLeadTimeProperties("Foo")
            )
        .when()
            .appliesBehavior(generateDataSet("TASKB", KpiLevel.DEMAND))
        .then()
            .emptyDataSet();
    }

    private GenerateLeadTimeData generateDataSet(String projectKey, KpiLevel level) {
        return new GenerateLeadTimeData(projectKey, level);
    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .statuses()
                .withProgressingStatuses("Doing", "Reviewing")
                .withNotProgressingStatuses("Open", "To Do", "To Review", "Done", "Cancelled")
            .eoS()
            .types()
                .addSubtasks("Backend Development")
                .addFeatures("Task")
                .addDemand("Demand")
            .eoT();
        return dsl;
    }

    private class GenerateLeadTimeData implements DSLSimpleBehaviorWithAsserter<LeadTimeKpiDataAsserter>{

        private String projectKey;
        private KpiLevel level;
        private LeadTimeKpiDataAsserter asserter;

        public GenerateLeadTimeData(String projectKey, KpiLevel level) {
            this.projectKey = projectKey;
            this.level = level;
        }

        @Override
        public void behave(KpiEnvironment environment) {
            KpiDataService kpiDataService = environment.services().kpiDataService().getService();
            KpiLeadTimeProperties kpiProperties = environment.getKPIProperties(KpiLeadTimeProperties.class);
            ZoneId timezone = environment.getTimezone();

            LeadTimeKpiDataProvider subject = new LeadTimeKpiDataProvider(kpiDataService, kpiProperties);
            List<LeadTimeKpi> dataSet = subject.getDataSet(projectKey, level, timezone);
            asserter = new LeadTimeKpiDataAsserter(dataSet);
        }

        @Override
        public LeadTimeKpiDataAsserter then() {
            return asserter;
        }
    }

    private class LeadTimeKpiDataAsserter {

        private List<LeadTimeKpi> dataSet;

        public LeadTimeKpiDataAsserter(List<LeadTimeKpi> dataSet) {
            this.dataSet = dataSet;
        }

        public void emptyDataSet() {
            Assertions.assertThat(dataSet).hasSize(0);
        }

        public LeadTimeKpiAsserter<LeadTimeKpiDataAsserter> leadTimeForIssue(String pkey) {
            Optional<LeadTimeKpi> opKpi = dataSet.stream().filter(c -> c.getIssueKey().equals(pkey)).findFirst();
            Assertions.assertThat(opKpi).as("Lead Kpi for issue %s not found.",pkey).isPresent();
            return new LeadTimeKpiAsserter<LeadTimeKpiDataAsserter>(opKpi.get(), this);
        }

        public LeadTimeKpiDataAsserter dataSetHasTotalSize(int size) {
            Assertions.assertThat(dataSet).hasSize(size);
            return this;
        }

    }
}
