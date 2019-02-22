package objective.taskboard.followup.kpi.touchtime;

import java.time.ZoneId;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.properties.KpiTouchTimePropertiesMocker;
import objective.taskboard.followup.kpi.touchtime.helpers.GenerateTTByIssueDataSetStrategyBehavior;
import objective.taskboard.followup.kpi.touchtime.helpers.TTByIssueKpiDataPointBuilder;

@RunWith(MockitoJUnitRunner.class)
public class TouchTimeByIssueKpiStrategyTest {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Test
    public void getTouchTimeChartDataSet_happyPath() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TASKB")
                        .startAt("2018-11-01")
                        .deliveredAt("2018-12-31")
                    .eoP()
                .eoPs()
            .eoS()
            .withKpiProperties(KpiTouchTimePropertiesMocker.withTouchTimeConfig())
            .withJiraProperties()
                .withSubtaskStatusPriorityOrder("Cancelled", "Done", "Reviewing", "To Review", "Doing", "To Do", "Open")
            .eoJp()
            .todayIs("2018-12-17")
            .givenIssue("I-1")
                .isFeature()
                .project("TASKB")
                .type("Task")
                .withTransitions()
                    .status("Open").date("2018-11-05")
                    .status("To Plan").date("2018-11-05")
                    .status("Planning").date("2018-11-05")
                    .status("To Dev").date("2018-11-06")
                    .status("Developing").date("2018-11-07")
                    .status("Done").date("2018-11-09")
                .eoT()
                .subtask("I-2")
                    .type("Tech Analysis")
                    .withTransitions()
                        .status("Open").date("2018-11-05")
                        .status("To Do").date("2018-11-05")
                        .status("Doing").date("2018-11-05")
                        .status("Done").date("2018-11-06")
                    .eoT()
                    .worklogs()
                        .at("2018-11-05").timeSpentInHours(8.0)
                        .at("2018-11-06").timeSpentInHours(2.5)
                    .eoW()
                .endOfSubtask()
            .eoI()
            .givenIssue("I-3")
                .isDemand()
                .project("TASKB")
                .type("Demand")
                .withTransitions()
                    .status("Open").date("2018-11-05")
                    .status("To Plan").date("2018-11-05")
                    .status("Planning").date("2018-11-05")
                    .status("To Dev").date("2018-11-06")
                    .status("Developing").date("2018-11-07")
                    .status("Done").date("2018-11-09")
                .eoT()
                .subtask("I-4")
                    .type("Backend Development")
                    .withTransitions()
                        .status("Open").date("2018-11-05")
                        .status("To Do").date("2018-11-06")
                        .status("Doing").date("2018-11-07")
                        .status("To Review").date("2018-11-08")
                        .status("Reviewing").date("2018-11-09")
                        .status("Done").date("2018-11-09")
                    .eoT()
                    .worklogs()
                        .at("2018-11-07").timeSpentInHours(2.0)
                        .at("2018-11-09").timeSpentInHours(4.0)
                    .eoW()
                .endOfSubtask()
            .eoI()
        .when()
            .appliesBehavior(new GenerateTTByIssueDataSetStrategyBehavior("TASKB", KpiLevel.SUBTASKS, ZONE_ID))
        .then()
            .hasSize(4)
            .hasPoints(
                    pointOf("I-2")
                        .withIssueType("Tech Analysis")
                        .withIssueStatus("Doing")
                        .withEffortInHours(10.5)
                        .withStartProgressingDate("2018-11-05")
                        .withEndProgressingDate("2018-11-06"),
                    pointOf("I-2")
                        .withIssueType("Tech Analysis")
                        .withIssueStatus("Reviewing")
                        .withEffortInHours(0)
                        .withStartProgressingDate("2018-11-05")
                        .withEndProgressingDate("2018-11-06"),
                    pointOf("I-4")
                        .withIssueType("Backend Development")
                        .withIssueStatus("Doing")
                        .withEffortInHours(2.0)
                        .withStartProgressingDate("2018-11-07")
                        .withEndProgressingDate("2018-11-09"),
                    pointOf("I-4")
                        .withIssueType("Backend Development")
                        .withIssueStatus("Reviewing")
                        .withEffortInHours(4.0)
                        .withStartProgressingDate("2018-11-07")
                        .withEndProgressingDate("2018-11-09")
            );
    }

    @Test
    public void getTouchTimeChartDataSet_whenNoIssues_thenEmptyDataSet() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TASKB")
                        .startAt("2018-11-01")
                        .deliveredAt("2018-12-31")
                    .eoP()
                .eoPs()
            .eoS()
            .todayIs("2018-12-17")
            .withJiraProperties()
                .withSubtaskStatusPriorityOrder("Cancelled", "Done", "Reviewing", "To Review", "Doing", "To Do", "Open")
            .eoJp()
            .withKpiProperties(KpiTouchTimePropertiesMocker.withTouchTimeConfig())
        .when()
            .appliesBehavior(new GenerateTTByIssueDataSetStrategyBehavior("TASKB", KpiLevel.SUBTASKS, ZONE_ID))
        .then()
            .emptyDataSet();
    }

    private TTByIssueKpiDataPointBuilder pointOf(String pKey) {
        return new TTByIssueKpiDataPointBuilder()
            .withIssueKey(pKey);
    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .types()
                .addFeatures("Task")
                .addSubtasks("Backend Development", "Tech Analysis")
            .eoT()
            .statuses()
                .withProgressingStatuses("Planning", "Developing", "Doing", "Reviewing")
                .withNotProgressingStatuses("Open", "To Plan", "To Dev", "To Do", "To Review", "Done")
            .eoS();
        return dsl;
    }
}
