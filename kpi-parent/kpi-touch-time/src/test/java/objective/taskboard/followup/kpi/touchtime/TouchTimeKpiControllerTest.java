package objective.taskboard.followup.kpi.touchtime;

import static objective.taskboard.followup.kpi.touchtime.helpers.TouchTimeByWeekHelperCalculator.averageEffortByWeekCalculator;
import static objective.taskboard.utils.DateTimeUtils.parseStringToDate;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardOperationalPermission;
import objective.taskboard.followup.kpi.properties.KpiTouchTimeProperties;
import objective.taskboard.followup.kpi.properties.KpiTouchTimePropertiesMocker;
import objective.taskboard.followup.kpi.services.DSLKpi;
import objective.taskboard.followup.kpi.services.KpiDataService;
import objective.taskboard.followup.kpi.services.KpiEnvironment;
import objective.taskboard.followup.kpi.services.RequestChartDataBehavior;
import objective.taskboard.followup.kpi.services.RequestChartDataBehaviorBuilder;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;

@RunWith(MockitoJUnitRunner.class)
public class TouchTimeKpiControllerTest {
    @Test
    public void requestTouchTimeByIssueData_happyPath() {
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
            .appliesBehavior(createRequestTTByIssueDataBehavior()
                    .forProject("TEST")
                    .withLevel("Subtasks")
                    .withTimezone("America/Sao_Paulo")
                    .withPermission()
                    .build())
        .then()
            .httpStatus(HttpStatus.OK)
            .bodyClassWhenList(0, TouchTimeByIssueKpiDataPoint.class)
            .bodyAsJson(
                "["
                    + "{"
                        + "\"issueKey\": \"I-1\","
                        + "\"issueType\": \"Backend Development\","
                        + "\"startProgressingDate\": " + getDateInMiliseconds("2020-01-03") + ","
                        + "\"endProgressingDate\": " + getDateInMiliseconds("2020-01-07") + ","
                        + "\"stacks\": "
                            + "["
                                + "{"
                                    + "\"stackName\": \"Doing\","
                                    + "\"effortInHours\": 5.0"
                                + "},"
                                + "{"
                                    + "\"stackName\": \"Reviewing\","
                                    + "\"effortInHours\": 0.0"
                                + "}"
                            + "]"
                    + "},"
                    + "{"
                        + "\"issueKey\": \"I-2\","
                        + "\"issueType\": \"Backend Development\","
                        + "\"startProgressingDate\": " + getDateInMiliseconds("2020-01-04") + ","
                        + "\"endProgressingDate\": "  + getDateInMiliseconds("2020-01-07") + ","
                        + "\"stacks\": "
                            + "["
                                + "{"
                                    + "\"stackName\": \"Doing\","
                                    + "\"effortInHours\": 8.0"
                                + "},"
                                + "{"
                                    + "\"stackName\": \"Reviewing\","
                                    + "\"effortInHours\": 5.0"
                                + "}"
                            + "]"
                    + "},"
                    + "{"
                        + "\"issueKey\": \"I-3\","
                        + "\"issueType\": \"Alpha Bug\","
                        + "\"startProgressingDate\": " + getDateInMiliseconds("2020-01-03") + ","
                        + "\"endProgressingDate\": " + getDateInMiliseconds("2020-01-06") + ","
                        + "\"stacks\": "
                            + "["
                                + "{"
                                    + "\"stackName\": \"Doing\","
                                    + "\"effortInHours\": 7.0"
                                + "},"
                                + "{"
                                    + "\"stackName\": \"Reviewing\","
                                    + "\"effortInHours\": 3.0"
                                + "}"
                            + "]"
                    + "}"
              + "]");
    }

    @Test
    public void requestTouchTimeByWeekData_happyPath() {
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
            .appliesBehavior(createRequestTTByWeekDataBehavior()
                    .forProject("TEST")
                    .withLevel("Subtasks")
                    .withTimezone("America/Sao_Paulo")
                    .withPermission()
                    .build())
        .then()
            .httpStatus(HttpStatus.OK)
            .bodyClassWhenList(0, TouchTimeByWeekKpiDataPoint.class)
            .bodyAsJson(
                     "["
                        + "{"
                            + "\"date\" : " + getDateInMiliseconds("2020-01-04")+","
                            + "\"stackName\": \"Development\","
                            + "\"effortInHours\": " + averageEffortByWeekCalculator()
                                                        .addEffortByType(5.0)
                                                        .addEffortByType(8.0)
                                                        .addEffortByStatus(7.0)
                                                        .totalSelectedIssuesInWeek(3)
                                                        .calculate()
                        + "},"
                        + "{"
                            + "\"date\" : " + getDateInMiliseconds("2020-01-04")+","
                            + "\"stackName\": \"Review\","
                            + "\"effortInHours\": " + averageEffortByWeekCalculator()
                                                        .totalSelectedIssuesInWeek(3)
                                                        .calculate()
                        + "},"
                        + "{"
                            + "\"date\" : " + getDateInMiliseconds("2020-01-11")+","
                            + "\"stackName\": \"Development\","
                            + "\"effortInHours\": " + averageEffortByWeekCalculator()
                                                        .addEffortByType(5.0)
                                                        .addEffortByType(8.0)
                                                        .addEffortByType(5.0)
                                                        .addEffortByStatus(7.0)
                                                        .totalSelectedIssuesInWeek(3)
                                                        .calculate()
                        + "},"
                        + "{"
                            + "\"date\" : " + getDateInMiliseconds("2020-01-11")+","
                            + "\"stackName\": \"Review\","
                            + "\"effortInHours\": " + averageEffortByWeekCalculator()
                                                        .addEffortByStatus(3.0)
                                                        .totalSelectedIssuesInWeek(3)
                                                        .calculate()
                        + "}"
                    + "]");
    }

    @Test
    public void requestTouchTimeByIssueData_whenDoNotHavePermission_thenStatusNotFound() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TEST")
                    .eoP()
                .eoPs()
            .eoS()
            .when()
                .appliesBehavior(createRequestTTByIssueDataBehavior()
                        .forProject("TEST")
                        .withLevel("Subtasks")
                        .withTimezone("America/Sao_Paulo")
                        .withoutPermission()
                        .build())
            .then()
                .httpStatus(HttpStatus.NOT_FOUND)
                .bodyClass(String.class)
                .bodyAsJson("\"Project not found: TEST.\"");
    }

    @Test
    public void requestTouchTimeByIssueData_whenProjectDoesNotExists_thenStatusNotFound() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TEST")
                    .eoP()
                .eoPs()
            .eoS()
            .when()
                .appliesBehavior(createRequestTTByIssueDataBehavior()
                        .forProject("FOO")
                        .withLevel("Subtasks")
                        .withTimezone("America/Sao_Paulo")
                        .withPermission()
                        .build())
            .then()
                .httpStatus(HttpStatus.NOT_FOUND)
                .bodyClass(String.class)
                .bodyAsString("Project not found: FOO.");
    }

    @Test
    public void requestTouchTimeByIssueData_whenInvalidLevelValue_thenStatusBadRequest() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TEST")
                    .eoP()
                .eoPs()
            .eoS()
            .when()
                .appliesBehavior(createRequestTTByIssueDataBehavior()
                        .forProject("TEST")
                        .withLevel("Foo")
                        .withTimezone("America/Sao_Paulo")
                        .withPermission()
                        .build())
            .then()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .bodyClass(String.class)
                .bodyAsString("Invalid level value: Foo.");
    }

    @Test
    public void requestTouchTime_whenInvalidMethod_thenStatusNotFound() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("TEST")
                    .eoP()
                .eoPs()
            .eoS()
            .when()
                .appliesBehavior(createRequestTouchTimeDataBehavior()
                        .forMethod("foo")
                        .forProject("TEST")
                        .withLevel("Subtasks")
                        .withTimezone("America/Sao_Paulo")
                        .withPermission()
                        .build())
            .then()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .bodyClass(String.class)
                .bodyAsString("Method invalid");
    }

    private RequestTouchTimeDataBehaviorBuilder createRequestTouchTimeDataBehavior() {
        return new RequestTouchTimeDataBehaviorBuilder();
    }

    private RequestTouchTimeDataBehaviorBuilder createRequestTTByWeekDataBehavior() {
        return new RequestTouchTimeDataBehaviorBuilder().forMethod("byWeek");
    }

    private RequestTouchTimeDataBehaviorBuilder createRequestTTByIssueDataBehavior() {
        return new RequestTouchTimeDataBehaviorBuilder().forMethod("byIssue");
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

    private long getDateInMiliseconds(String date) {
        return parseStringToDate(date).getTime();
    }

    private class RequestTouchTimeDataBehaviorBuilder extends RequestChartDataBehaviorBuilder<RequestTouchTimeDataBehavior> {

        private String method;

        public RequestTouchTimeDataBehaviorBuilder forMethod(String method) {
            this.method = method;
            return this;
        }

        @Override
        protected RequestTouchTimeDataBehavior doBuild() {
            if (method == null) {
                Assertions.fail("Should set a method");
            }
            return new RequestTouchTimeDataBehavior(method, projectKey, level, zoneId, hasPermission, preventProviderMock);
        }

    }

    private class RequestTouchTimeDataBehavior extends RequestChartDataBehavior<TouchTimeKpiProvider> {

        private String requestMethod;

        public RequestTouchTimeDataBehavior(
                String requestMethod,
                String projectKey,
                String level,
                String zoneId,
                boolean hasPermission,
                boolean preventProviderMock) {
            super(projectKey, level, zoneId, hasPermission, preventProviderMock);
            this.requestMethod = requestMethod;
        }

        @Override
        public void doBehave(KpiEnvironment environment, ProjectDashboardOperationalPermission permission,
                ProjectService projectService) {
            TouchTimeKpiProvider dataProvider = mockProvider(environment);
            TouchTimeKpiController subject = new TouchTimeKpiController(permission, projectService, dataProvider);
            asserter = AssertResponse.of(subject.getData(requestMethod, projectKey, zoneId, level));

        }

        @Override
        protected TouchTimeKpiProvider mockProvider(KpiEnvironment environment) {
            KpiTouchTimeProperties touchTimeProperties = environment.getKPIProperties(KpiTouchTimeProperties.class);
                        
            KpiDataService kpiDataService = environment.services().kpiDataService().getService();
            
            JiraProperties jiraProperties = environment.getJiraProperties();
            MetadataService metadataService = environment.services().metadata().getService();
            TouchTimeByWeekKpiStrategyFactory byWeek = new TouchTimeByWeekKpiStrategyFactory(touchTimeProperties, kpiDataService, jiraProperties);
            TouchTimeByIssueKpiStrategyFactory byIssue = new TouchTimeByIssueKpiStrategyFactory(touchTimeProperties, kpiDataService, jiraProperties, metadataService);
            ProjectService projectService = environment.services().projects().getService();
            return new TouchTimeKpiProvider(byWeek, byIssue, projectService);
        }

    }
}
