package objective.taskboard.followup.kpi.leadTime;

import static objective.taskboard.followup.kpi.leadTime.LeadTimeKpiAsserter.leadTimeKpi;
import static objective.taskboard.followup.kpi.properties.KpiLeadTimePropertiesMocker.withDemandLeadTimeProperties;
import static objective.taskboard.followup.kpi.properties.KpiLeadTimePropertiesMocker.withFeatureLeadTimeProperties;
import static objective.taskboard.followup.kpi.properties.KpiLeadTimePropertiesMocker.withSubtaskLeadTimeProperties;

import org.junit.Test;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehavior;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.followup.kpi.leadtime.LeadTimeKpi;
import objective.taskboard.followup.kpi.leadtime.LeadTimeKpiFactory;
import objective.taskboard.followup.kpi.properties.KpiLeadTimeProperties;

public class LeadTimeKpiTest {

    @Test
    public void leadTime_subtaskDone_happyPath() {
       dsl().environment()
           .givenSubtask("I-1")
               .type("Development")
               .project("TASKB")
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
               withSubtaskLeadTimeProperties("Open","To Do","Doing","To Review","Reviewing")
           )
       .then()
           .assertThat(leadTimeKpi("I-1"))
                   .startsAt("2019-01-01")
                   .endsAt("2019-01-06")
                   .hasTotalLeadTime(6)
                   .hasLastStatus("Done")
                   .hasType("Development");
    }

    @Test
    public void leadTime_feature_happyPath() {
       dsl().environment()
           .givenFeature("I-1")
               .type("Task")
               .project("TASKB")
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
               withFeatureLeadTimeProperties("Open","To Do","Doing","To Review","Reviewing")
           )
       .then()
           .assertThat(leadTimeKpi("I-1"))
                   .startsAt("2019-01-01")
                   .endsAt("2019-01-06")
                   .hasTotalLeadTime(6)
                   .hasLastStatus("Done")
                   .hasType("Task");
    }

    @Test
    public void leadTime_demand_happyPath() {
       dsl().environment()
           .givenDemand("I-1")
               .type("Demand")
               .project("TASKB")
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
               withDemandLeadTimeProperties("Open","To Do","Doing","To Review","Reviewing")
           )
       .then()
           .assertThat(leadTimeKpi("I-1"))
                   .startsAt("2019-01-01")
                   .endsAt("2019-01-06")
                   .hasTotalLeadTime(6)
                   .hasLastStatus("Done")
                   .hasType("Demand");
    }

    @Test
    public void leadTime_subtaskCancelled_happyPath() {
       dsl().environment()
           .givenSubtask("I-1")
               .type("Development")
               .project("TASKB")
               .withTransitions()
                   .status("Open").date("2019-01-01")
                   .status("To Do").noDate()
                   .status("Doing").noDate()
                   .status("To Review").noDate()
                   .status("Reviewing").noDate()
                   .status("Done").noDate()
                   .status("Cancelled").date("2019-01-06")
               .eoT()
           .eoI()
           .withKpiProperties(
               withSubtaskLeadTimeProperties("Open","To Do","Doing","To Review","Reviewing")
           )
       .then()
           .assertThat(leadTimeKpi("I-1"))
                   .startsAt("2019-01-01")
                   .endsAt("2019-01-06")
                   .hasTotalLeadTime(6)
                   .hasLastStatus("Cancelled")
                   .hasType("Development");
    }

    @Test
    public void leadTime_subtaskCancelledWithWorklogs_happyPath() {
       dsl().environment()
           .givenSubtask("I-1")
               .type("Development")
               .project("TASKB")
               .withTransitions()
                   .status("Open").date("2019-01-01")
                   .status("To Do").date("2019-01-02")
                   .status("Doing").date("2019-01-03")
                   .status("To Review").noDate()
                   .status("Reviewing").noDate()
                   .status("Done").noDate()
                   .status("Cancelled").date("2019-01-06")
               .eoT()
               .worklogs()
                   .at("2019-01-03").timeSpentInHours(5.0)
               .eoW()
           .eoI()
           .withKpiProperties(
               withSubtaskLeadTimeProperties("Open","To Do","Doing","To Review","Reviewing")
           )
       .then()
           .assertThat(leadTimeKpi("I-1"))
                   .startsAt("2019-01-01")
                   .endsAt("2019-01-06")
                   .hasTotalLeadTime(6)
                   .hasLastStatus("Cancelled")
                   .hasType("Development");
    }

    @Test
    public void leadTime_whenJumpingStatus_withWorklogOnThem_thenHappyPath() {
        dsl().environment()
            .givenSubtask("I-1")
                .type("Development")
                .project("TASKB")
                .withTransitions()
                    .status("Open").date("2019-01-01")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").noDate()
                    .status("To Review").noDate()
                    .status("Reviewing").date("2019-01-05")
                    .status("Done").date("2019-01-06")
                    .status("Cancelled").noDate()
                .eoT()
                .worklogs()
                    .at("2019-01-03").timeSpentInHours(1)
                .eoW()
            .eoI()
            .withKpiProperties(
                withSubtaskLeadTimeProperties("Open","To Do","Doing","To Review","Reviewing")
            )
        .then()
            .assertThat(leadTimeKpi("I-1"))
                    .startsAt("2019-01-01")
                    .endsAt("2019-01-06")
                    .hasTotalLeadTime(6)
                    .hasLastStatus("Done")
                    .hasType("Development");
    }

    @Test
    public void leadTime_whenIssueOpenWithoutProgress_thenThrowException() {
        dsl().environment()
            .givenSubtask("I-1")
                .type("Development")
                .project("TASKB")
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
                withSubtaskLeadTimeProperties("Open","To Do","Doing","To Review","Reviewing")
            )
        .when()
            .expectExceptionFromBehavior(generateLeadTimeKpiForIssue("I-1"))
        .then()
            .isFromException(IllegalArgumentException.class)
            .hasMessage("Invalid exit date.");
    }

    @Test
    public void leadTime_whenIssueNotCompletedYet_withWorklogOnThem_thenThrowException() {
        dsl().environment()
            .givenSubtask("I-1")
                .type("Development")
                .project("TASKB")
                .withTransitions()
                    .status("Open").date("2019-01-01")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").noDate()
                    .status("To Review").noDate()
                    .status("Reviewing").date("2019-01-05")
                    .status("Done").noDate()
                    .status("Cancelled").noDate()
                .eoT()
                .worklogs()
                    .at("2019-01-03").timeSpentInHours(1)
                .eoW()
            .eoI()
            .withKpiProperties(
                withSubtaskLeadTimeProperties("Open","To Do","Doing","To Review","Reviewing")
            )
        .when()
            .expectExceptionFromBehavior(generateLeadTimeKpiForIssue("I-1"))
        .then()
            .isFromException(IllegalArgumentException.class)
            .hasMessage("Invalid exit date.");
    }

    @Test
    public void leadTime_whenAllTransitionsWereMadeAtTheSameDay_thenLeadTimeIsOne() {
        dsl().environment()
            .givenSubtask("I-1")
                .type("Development")
                .project("TASKB")
                .withTransitions()
                    .status("Open").date("2019-01-01")
                    .status("To Do").date("2019-01-01")
                    .status("Doing").date("2019-01-01")
                    .status("To Review").date("2019-01-01")
                    .status("Reviewing").date("2019-01-01")
                    .status("Done").date("2019-01-01")
                    .status("Cancelled").noDate()
                .eoT()
            .eoI()
            .withKpiProperties(
                withSubtaskLeadTimeProperties("Open","To Do","Doing","To Review","Reviewing")
            )
        .then()
            .assertThat(leadTimeKpi("I-1"))
                    .startsAt("2019-01-01")
                    .endsAt("2019-01-01")
                    .hasTotalLeadTime(1)
                    .hasLastStatus("Done")
                    .hasType("Development");
    }

    @Test
    public void leadTime_whenWorklogRegisteredAfterIssueIsClosed_thenEndDateUsedForLeadTimeCalculationIsTheWorklogDate() {
        dsl().environment()
            .givenSubtask("I-1")
                .type("Development")
                .project("TASKB")
                .withTransitions()
                    .status("Open").date("2019-01-01")
                    .status("To Do").date("2019-01-02")
                    .status("Doing").date("2019-01-03")
                    .status("To Review").date("2019-01-04")
                    .status("Reviewing").date("2019-01-05")
                    .status("Done").date("2019-01-06")
                    .status("Cancelled").noDate()
                .eoT()
                .worklogs()
                    .at("2019-01-07").timeSpentInHours(5.0)
                .eoW()
            .eoI()
            .withKpiProperties(
                withSubtaskLeadTimeProperties("Open","To Do","Doing","To Review","Reviewing")
            )
        .then()
            .assertThat(leadTimeKpi("I-1"))
                    .startsAt("2019-01-01")
                    .endsAt("2019-01-07")
                    .hasTotalLeadTime(7)
                    .hasLastStatus("Done")
                    .hasType("Development");
    }

    @Test
    public void leadTime_whenWorklogRegisteredBeforeIssueCreationDate_thenStartDateUsedForLeadTimeCalculationIsTheWorklogDate() {
        dsl().environment()
            .givenSubtask("I-1")
                .type("Development")
                .project("TASKB")
                .withTransitions()
                    .status("Open").date("2019-01-02")
                    .status("To Do").date("2019-01-03")
                    .status("Doing").date("2019-01-04")
                    .status("To Review").date("2019-01-05")
                    .status("Reviewing").date("2019-01-06")
                    .status("Done").date("2019-01-07")
                    .status("Cancelled").noDate()
                .eoT()
                .worklogs()
                    .at("2019-01-01").timeSpentInHours(5.0)
                .eoW()
            .eoI()
            .withKpiProperties(
                withSubtaskLeadTimeProperties("Open","To Do","Doing","To Review","Reviewing")
            )
        .then()
            .assertThat(leadTimeKpi("I-1"))
                    .startsAt("2019-01-01")
                    .endsAt("2019-01-07")
                    .hasTotalLeadTime(7)
                    .hasLastStatus("Done")
                    .hasType("Development");
    }

    private DSLSimpleBehavior generateLeadTimeKpiForIssue(String pKey) {
        return new DSLSimpleBehavior() {

            @Override
            public void behave(KpiEnvironment environment) {
                DSLKpi kpiContext = environment.then();
                IssueKpi kpi = kpiContext.getIssueKpi(pKey);
                LeadTimeKpiFactory factory = new LeadTimeKpiFactory(
                        environment.getKPIProperties(KpiLeadTimeProperties.class).getLeadTime().toMap(),
                        environment.getTimezone());
                factory.create(kpi);
            }
        };
  }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .statuses()
                .withProgressingStatuses("Doing","Reviewing")
                .withNotProgressingStatuses("Open","To Do","To Review","Cancelled","Done")
            .eoS()
            .withSubtaskType("Development")
            .withFeatureType("Task")
            .withDemandType("Demand");
        return dsl;
    }
}
