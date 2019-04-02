package objective.taskboard.followup.kpi.touchtime;

import static objective.taskboard.followup.kpi.touchtime.helpers.TouchTimeByWeekHelperCalculator.averageEffortByWeekCalculator;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KpiTouchTimePropertiesMocker;
import objective.taskboard.followup.kpi.services.DSLKpi;
import objective.taskboard.followup.kpi.touchtime.helpers.GetTTByIssueDataSetProviderBehaviorBuilder;
import objective.taskboard.followup.kpi.touchtime.helpers.GetTTByWeekDataSetProviderBehaviorBuilder;
import objective.taskboard.followup.kpi.touchtime.helpers.GetTTDataSetThrowsProviderBehaviorBuilder;
import objective.taskboard.followup.kpi.touchtime.helpers.TTByIssueKpiDataPointBuilder;
import objective.taskboard.followup.kpi.touchtime.helpers.TTByWeekKpiDataPointBuilder;
import objective.taskboard.utils.DateTimeUtils;

@RunWith(MockitoJUnitRunner.class)
public class TouchTimeKpiProviderTest {
    @Test
    public void getTTByWeekSubtaskDataSet_happyPath() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TEST")
                        .startAt("2019-12-29")
                        .deliveredAt("2020-01-12")
                    .eoP()
                .eoPs()
            .eoS()
            .withKpiProperties(KpiTouchTimePropertiesMocker.withTouchTimeConfig()
                    .withChartStack("Development")
                        .types("Backend Development")
                        .statuses("Doing")
                    .eoS()
                    .withChartStack("Review")
                        .statuses("Reviewing")
                    .eoS())
            .givenSubtask("I-1")
                .type("Backend Development")
                .project("TEST")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").date("2020-01-02")
                    .status("Doing").date("2020-01-03")
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").noDate()
                .eoT()
                .worklogs()
                    .at("2020-01-03").timeSpentInHours(5.0)
                .eoW()
            .eoI()
            .givenSubtask("I-2")
                .type("Backend Development")
                .project("TEST")
                .withTransitions()
                    .status("Open").date("2020-01-02")
                    .status("To Do").date("2020-01-03")
                    .status("Doing").date("2020-01-04")
                    .status("To Review").date("2020-01-05")
                    .status("Reviewing").date("2020-01-06")
                    .status("Done").noDate()
                .eoT()
                .worklogs()
                    .at("2020-01-04").timeSpentInHours(8.0)
                    .at("2020-01-06").timeSpentInHours(5.0)
                .eoW()
            .eoI()
            .givenSubtask("I-3")
                .type("Alpha Bug")
                .project("TEST")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").date("2020-01-02")
                    .status("Doing").date("2020-01-03")
                    .status("To Review").date("2020-01-04")
                    .status("Reviewing").date("2020-01-05")
                    .status("Done").date("2020-01-06")
                .eoT()
                .worklogs()
                    .at("2020-01-03").timeSpentInHours(7.0)
                    .at("2020-01-05").timeSpentInHours(3.0)
                .eoW()
            .eoI()
            .todayIs("2020-01-07")
        .when()
            .appliesBehavior(new GetTTByWeekDataSetProviderBehaviorBuilder()
                    .forProject("TEST")
                    .withLevel(KpiLevel.SUBTASKS)
                    .withTimezone(DateTimeUtils.determineTimeZoneId("America/Sao_Paulo"))
                    .build())
        .then()
            .hasSize(4)
                .hasPoints(
                        weekDataPoint("2020-01-04")
                            .withStackName("Development")
                            .withEffortInHours(
                                    averageEffortByWeekCalculator()
                                        .addEffortByType(5.0)
                                        .addEffortByType(8.0)
                                        .addEffortByStatus(7.0)
                                        .totalSelectedIssuesInWeek(3)
                                        .calculate()),
                        weekDataPoint("2020-01-04")
                            .withStackName("Review")
                            .withNoEffort(),
                        weekDataPoint("2020-01-11")
                            .withStackName("Development")
                            .withEffortInHours(
                                    averageEffortByWeekCalculator()
                                        .addEffortByType(5.0)
                                        .addEffortByType(8.0)
                                        .addEffortByType(5.0)
                                        .addEffortByStatus(7.0)
                                        .totalSelectedIssuesInWeek(3)
                                        .calculate()),
                        weekDataPoint("2020-01-11")
                            .withStackName("Review")
                            .withEffortInHours(
                                    averageEffortByWeekCalculator()
                                        .addEffortByStatus(3.0)
                                        .totalSelectedIssuesInWeek(3)
                                        .calculate()));
    }

    @Test
    public void getTTByIssueSubtaskDataSet_happyPath () {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TEST")
                    .eoP()
                .eoPs()
            .eoS()
            .givenSubtask("I-1")
                .type("Backend Development")
                .project("TEST")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").date("2020-01-02")
                    .status("Doing").date("2020-01-03")
                    .status("To Review").noDate()
                    .status("Reviewing").noDate()
                    .status("Done").noDate()
                .eoT()
                .worklogs()
                    .at("2020-01-03").timeSpentInHours(5.0)
                .eoW()
            .eoI()
            .givenSubtask("I-2")
                .type("Backend Development")
                .project("TEST")
                .withTransitions()
                    .status("Open").date("2020-01-02")
                    .status("To Do").date("2020-01-03")
                    .status("Doing").date("2020-01-04")
                    .status("To Review").date("2020-01-05")
                    .status("Reviewing").date("2020-01-06")
                    .status("Done").noDate()
                .eoT()
                .worklogs()
                    .at("2020-01-04").timeSpentInHours(8.0)
                    .at("2020-01-06").timeSpentInHours(5.0)
                .eoW()
            .eoI()
            .givenSubtask("I-3")
                .type("Alpha Bug")
                .project("TEST")
                .withTransitions()
                    .status("Open").date("2020-01-01")
                    .status("To Do").date("2020-01-02")
                    .status("Doing").date("2020-01-03")
                    .status("To Review").date("2020-01-04")
                    .status("Reviewing").date("2020-01-05")
                    .status("Done").date("2020-01-06")
                .eoT()
                .worklogs()
                    .at("2020-01-03").timeSpentInHours(7.0)
                    .at("2020-01-05").timeSpentInHours(3.0)
                .eoW()
            .eoI()
            .todayIs("2020-01-07")
        .when()
            .appliesBehavior(new GetTTByIssueDataSetProviderBehaviorBuilder()
                    .forProject("TEST")
                    .withLevel(KpiLevel.SUBTASKS)
                    .withTimezone(DateTimeUtils.determineTimeZoneId("America/Sao_Paulo"))
                    .build())
        .then()
            .hasSize(3)
            .hasPoints(
                    issueDataPoint("I-1")
                        .withIssueType("Backend Development")
                        .withStack()
                            .withStackName("Doing")
                            .withEffortInHours(5.0)
                        .eoS()
                        .withStack()
                            .withStackName("Reviewing")
                            .withEffortInHours(0.0)
                        .eoS()
                        .withStartProgressingDate("2020-01-03")
                        .withEndProgressingDate("2020-01-07"),
                    issueDataPoint("I-2")
                        .withIssueType("Backend Development")
                        .withStack()
                            .withStackName("Doing")
                            .withEffortInHours(8.0)
                        .eoS()
                        .withStack()
                            .withStackName("Reviewing")
                            .withEffortInHours(5.0)
                        .eoS()
                        .withStartProgressingDate("2020-01-04")
                        .withEndProgressingDate("2020-01-07"),
                    issueDataPoint("I-3")
                        .withIssueType("Alpha Bug")
                        .withStack()
                            .withStackName("Doing")
                            .withEffortInHours(7.0)
                        .eoS()
                        .withStack()
                            .withStackName("Reviewing")
                            .withEffortInHours(3.0)
                        .eoS()
                        .withStartProgressingDate("2020-01-03")
                        .withEndProgressingDate("2020-01-06")
            );
    }

    @Test
    public void getTTByIssueDataSet_whenInvalidProject_thenThrows() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TEST")
                    .eoP()
                .eoPs()
            .eoS()
        .when()
            .expectExceptionFromBehavior(new GetTTByIssueDataSetProviderBehaviorBuilder()
                    .forProject("FOO")
                    .withLevel(KpiLevel.SUBTASKS)
                    .withTimezone(DateTimeUtils.determineTimeZoneId("America/Sao_Paulo"))
                    .build())
        .then()
            .isFromException(IllegalArgumentException.class)
            .hasMessage("Project with key 'FOO' not found");
    }

    @Test
    public void getTTByIssueDataSet_whenInvalidMethod_thenThrows() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TEST")
                    .eoP()
                .eoPs()
            .eoS()
        .when()
            .expectExceptionFromBehavior(new GetTTDataSetThrowsProviderBehaviorBuilder()
                    .forMethod("byFoo")
                    .forProject("TEST")
                    .withLevel(KpiLevel.SUBTASKS)
                    .withTimezone(DateTimeUtils.determineTimeZoneId("America/Sao_Paulo"))
                    .build())
        .then()
            .isFromException(IllegalArgumentException.class)
            .hasMessage("Method invalid");
    }

    @Test
    public void getTTByIssueDataSet_whenInvalidLevel_thenThrows() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TEST")
                    .eoP()
                .eoPs()
            .eoS()
        .when()
            .expectExceptionFromBehavior(new GetTTByIssueDataSetProviderBehaviorBuilder()
                    .forProject("TEST")
                    .withLevel(KpiLevel.UNMAPPED)
                    .withTimezone(DateTimeUtils.determineTimeZoneId("America/Sao_Paulo"))
                    .build())
        .then()
            .isFromException(IllegalArgumentException.class)
            .hasMessage("Empty level");
    }

    private TTByWeekKpiDataPointBuilder weekDataPoint(String weekDate) {
        return new TTByWeekKpiDataPointBuilder().withDate(weekDate);
    }

    private TTByIssueKpiDataPointBuilder issueDataPoint(String pKey) {
        return new TTByIssueKpiDataPointBuilder().withIssueKey(pKey);
    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .types()
                .addSubtasks("Backend Development", "Alpha Bug")
            .eoT()
            .statuses()
                .withProgressingStatuses("Doing", "Reviewing")
                .withNotProgressingStatuses("Open", "To Do", "To Review", "Done")
            .eoS()
            .withKpiProperties(KpiTouchTimePropertiesMocker.withTouchTimeConfig())
            .withJiraProperties()
                .withSubtaskStatusPriorityOrder("Done", "Reviewing", "To Review", "Doing", "To Do", "Open")
            .eoJp();
        return dsl;
    }
}
