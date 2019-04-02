package objective.taskboard.followup.kpi.cycletime;

import static objective.taskboard.followup.kpi.properties.KpiCycleTimePropertiesMocker.withSubtaskCycleTimeProperties;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardOperationalPermission;
import objective.taskboard.domain.IssueColorService;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KpiCycleTimeProperties;
import objective.taskboard.followup.kpi.services.DSLKpi;
import objective.taskboard.followup.kpi.services.KpiEnvironment;
import objective.taskboard.followup.kpi.services.RequestChartDataBehavior;
import objective.taskboard.followup.kpi.services.RequestChartDataBehaviorBuilder;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;
import objective.taskboard.utils.DateTimeUtils;

@RunWith(MockitoJUnitRunner.class)
public class CycleTimeControllerTest {

    @Test
    public void requestCycleTimeChartData_happyPath() {
        dsl()
            .environment()
                .services()
                    .projects()
                        .withKey("TEST")
                        .eoP()
                    .eoPs()
                    .issueColor()
                        .withProgressingStatusesColor("#ABABAB")
                        .withNonProgressingStatusesColor("#FEFEFE")
                    .eoIC()
                .eoS()
                .givenSubtask("I-1")
                    .type("Backend Development")
                    .project("TEST")
                    .withTransitions()
                        .status("Open").date("2020-01-01")
                        .status("To Do").date("2020-01-02")
                        .status("Doing").date("2020-01-03")
                        .status("To Review").date("2020-01-04")
                        .status("Reviewing").date("2020-01-05")
                        .status("Done").date("2020-01-06")
                    .eoT()
                .eoI()
                .givenSubtask("I-2")
                    .type("Alpha Bug")
                    .project("TEST")
                    .withTransitions()
                        .status("Open").date("2020-01-02")
                        .status("To Do").date("2020-01-03")
                        .status("Doing").date("2020-01-04")
                        .status("To Review").date("2020-01-05")
                        .status("Reviewing").date("2020-01-06")
                        .status("Done").date("2020-01-07")
                    .eoT()
                .eoI()
                .todayIs("2020-01-08")
            .when()
                .appliesBehavior(createRequestDataBehavior()
                        .forProject("TEST")
                        .withLevel("Subtasks")
                        .withTimezone("America/Sao_Paulo")
                        .withPermission()
                        .build())
            .then()
                .httpStatus(HttpStatus.OK)
                .bodyClass(List.class)
                .bodyAsJson("["
                    + "{"
                        + "\"issueKey\": \"I-1\","
                        + "\"issueType\": \"Backend Development\","
                        + "\"cycleTime\": 5,"
                        + "\"enterDate\": " + parseDateAsMillis("2020-01-02") + ","
                        + "\"exitDate\": " + parseDateAsMillis("2020-01-06") + ","
                        + "\"subCycles\": ["
                            + "{"
                                + "\"status\": \"To Do\","
                                + "\"color\": \"#FEFEFE\","
                                + "\"duration\": 1"
                            + "},"
                            + "{"
                                + "\"status\": \"Doing\","
                                + "\"color\": \"#ABABAB\","
                                + "\"duration\": 1"
                            + "},"
                            + "{"
                                + "\"status\": \"To Review\","
                                + "\"color\": \"#FEFEFE\","
                                + "\"duration\": 1"
                            + "},"
                            + "{"
                                + "\"status\": \"Reviewing\","
                                + "\"color\": \"#ABABAB\","
                                + "\"duration\": 1"
                            + "}"
                        + "]"
                    + "},"
                    + "{"
                        + "\"issueKey\": \"I-2\","
                        + "\"issueType\": \"Alpha Bug\","
                        + "\"cycleTime\": 5,"
                        + "\"enterDate\": " + parseDateAsMillis("2020-01-03") + ","
                        + "\"exitDate\": "+ parseDateAsMillis("2020-01-07") + ","
                        + "\"subCycles\": ["
                            + "{"
                                + "\"status\": \"To Do\","
                                + "\"color\": \"#FEFEFE\","
                                + "\"duration\": 1"
                            + "},"
                            + "{"
                                + "\"status\": \"Doing\","
                                + "\"color\": \"#ABABAB\","
                                + "\"duration\": 1"
                            + "},"
                            + "{"
                                + "\"status\": \"To Review\","
                                + "\"color\": \"#FEFEFE\","
                                + "\"duration\": 1"
                            + "},"
                            + "{"
                                + "\"status\": \"Reviewing\","
                                + "\"color\": \"#ABABAB\","
                                + "\"duration\": 1"
                            + "}"
                        + "]"
                    + "}"
                + "]");
    }

    private long parseDateAsMillis(String date) {
        return parseDateTime(date, "00:00:00", "America/Sao_Paulo").toEpochSecond() * 1000;
    }

    @Test
    public void requestCycleTimeChartData_whenNotHavePermission_thenStatusNotFound() {
        dsl()
        .environment()
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
                    .status("To Review").date("2020-01-04")
                    .status("Reviewing").date("2020-01-05")
                    .status("Done").date("2020-01-06")
                .eoT()
            .eoI()
            .givenSubtask("I-2")
                .type("Alpha Bug")
                .project("TEST")
                .withTransitions()
                    .status("Open").date("2020-01-02")
                    .status("To Do").date("2020-01-03")
                    .status("Doing").date("2020-01-04")
                    .status("To Review").date("2020-01-05")
                    .status("Reviewing").date("2020-01-06")
                    .status("Done").date("2020-01-07")
                .eoT()
            .eoI()
            .todayIs("2020-01-08")
        .when()
            .appliesBehavior(createRequestDataBehavior()
                    .forProject("TEST")
                    .withLevel("Subtasks")
                    .withTimezone("America/Sao_Paulo")
                    .withoutPermission()
                    .build())
        .then()
            .httpStatus(HttpStatus.NOT_FOUND)
            .bodyAsString("Project not found: TEST.");
    }

    @Test
    public void requestCycleTimeChartData_whenProjectDoesNotExists_thenStatusNotFound() {
        dsl()
        .environment()
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
                    .status("To Review").date("2020-01-04")
                    .status("Reviewing").date("2020-01-05")
                    .status("Done").date("2020-01-06")
                .eoT()
            .eoI()
            .givenSubtask("I-2")
                .type("Alpha Bug")
                .project("TEST")
                .withTransitions()
                    .status("Open").date("2020-01-02")
                    .status("To Do").date("2020-01-03")
                    .status("Doing").date("2020-01-04")
                    .status("To Review").date("2020-01-05")
                    .status("Reviewing").date("2020-01-06")
                    .status("Done").date("2020-01-07")
                .eoT()
            .eoI()
            .todayIs("2020-01-08")
        .when()
            .appliesBehavior(createRequestDataBehavior()
                    .forProject("NONEXISTENT")
                    .withLevel("Subtasks")
                    .withTimezone("America/Sao_Paulo")
                    .withPermission()
                    .build())
        .then()
            .httpStatus(HttpStatus.NOT_FOUND)
            .bodyAsString("Project not found: NONEXISTENT.");
    }

    @Test
    public void requestCycleTimeChartData_whenInvalidLevelValue_thenStatusBadRequest() {
        dsl()
        .environment()
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
                    .status("To Review").date("2020-01-04")
                    .status("Reviewing").date("2020-01-05")
                    .status("Done").date("2020-01-06")
                .eoT()
            .eoI()
            .givenSubtask("I-2")
                .type("Alpha Bug")
                .project("TEST")
                .withTransitions()
                    .status("Open").date("2020-01-02")
                    .status("To Do").date("2020-01-03")
                    .status("Doing").date("2020-01-04")
                    .status("To Review").date("2020-01-05")
                    .status("Reviewing").date("2020-01-06")
                    .status("Done").date("2020-01-07")
                .eoT()
            .eoI()
            .todayIs("2020-01-08")
        .when()
            .appliesBehavior(createRequestDataBehavior()
                    .forProject("TEST")
                    .withLevel("Foo")
                    .withTimezone("America/Sao_Paulo")
                    .withPermission()
                    .preventDataProviderMock()
                    .build())
        .then()
            .httpStatus(HttpStatus.BAD_REQUEST)
            .bodyAsString("Invalid level value: Foo.");
    }

    private DSLKpi dsl() {
        DSLKpi dsl = new DSLKpi();
        dsl.environment()
            .statuses()
                .withNotProgressingStatuses("Open","To Do","To Review","Done")
                .withProgressingStatuses("Doing","Reviewing")
            .eoS()
            .types()
                .addSubtasks("Backend Development","Alpha Bug")
            .eoT()
            .withKpiProperties(
                withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
            );
        return dsl;
    }

    private RequestCycleTimeDataBehaviorBuilder createRequestDataBehavior() {
        return new RequestCycleTimeDataBehaviorBuilder();
    }

    private class RequestCycleTimeDataBehaviorBuilder extends RequestChartDataBehaviorBuilder<RequestCycleTimeDataBehavior> {

        @Override
        public RequestCycleTimeDataBehavior doBuild() {
            return new RequestCycleTimeDataBehavior(projectKey, level, zoneId, hasPermission, preventProviderMock);
        }

    }

    private class RequestCycleTimeDataBehavior extends RequestChartDataBehavior<CycleTimeDataProvider> {

        public RequestCycleTimeDataBehavior(String projectKey, String level, String zoneId,
                boolean hasPermission, boolean preventProviderMock) {
            super(projectKey, level, zoneId, hasPermission, preventProviderMock);
        }

        @Override
        public void doBehave(KpiEnvironment environment, ProjectDashboardOperationalPermission permission, ProjectService projectService) {
            CycleTimeDataProvider dataProvider = mockProvider(environment);
            CycleTimeController subject = new CycleTimeController(permission, projectService, dataProvider);
            asserter = AssertResponse.of(subject.get(projectKey, zoneId, level));
        }

        @Override
        protected CycleTimeDataProvider mockProvider(KpiEnvironment environment) {
            CycleTimeDataProvider cycleTimeDataProvider = Mockito.mock(CycleTimeDataProvider.class);
            if (preventProviderMock) {
                return cycleTimeDataProvider;
            }
            ZoneId timezone = DateTimeUtils.determineTimeZoneId(zoneId);
            KpiLevel kpiLevel = KpiLevel.valueOf(level.toUpperCase());
            Map<KpiLevel, List<IssueKpi>> issues = environment.services().issueKpi().getIssuesByLevel();
            List<CycleTimeKpi> cycleTimeKpis = issues.get(kpiLevel).stream()
                .map(i -> transformIntoCycleTimeKpi(i, environment))
                .collect(Collectors.toList());
            Mockito.when(cycleTimeDataProvider.getDataSet(projectKey, kpiLevel, timezone)).thenReturn(cycleTimeKpis);
            return cycleTimeDataProvider;
        }

        private CycleTimeKpi transformIntoCycleTimeKpi(IssueKpi issue, KpiEnvironment environment) {
            Map<KpiLevel, Set<String>> cycleStatusMap = environment.getKPIProperties(KpiCycleTimeProperties.class).getCycleTime().toMap();
            IssueColorService colorService = environment.services().issueColor().getService();
            CycleTimeKpiFactory factory = new CycleTimeKpiFactory(cycleStatusMap, colorService);
            return factory.create(issue);
        }

    }
}