package objective.taskboard.followup.kpi.cycletime;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardOperationalPermission;
import objective.taskboard.domain.IssueColorService;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;
import objective.taskboard.utils.DateTimeUtils;

@RunWith(MockitoJUnitRunner.class)
public class CycleTimeControllerTest {

    @Mock
    ProjectService projectService;

    @Mock
    private ProjectDashboardOperationalPermission projectDashboardOperationalPermission;

    @Mock
    private CycleTimeDataProvider cycleTimeDataProvider;

    @InjectMocks
    private CycleTimeController subject;

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
            .withKpiProperties()
                .withSubtaskCycleTimeProperties("To Do","Doing","To Review","Reviewing")
            .eoKP();
        return dsl;
    }

    private RequestDataBehaviorBuilder createRequestDataBehavior() {
        return new RequestDataBehaviorBuilder();
    }

    public class RequestDataBehaviorBuilder {
        private String projectKey;
        private String level;
        private String zoneId;
        private Boolean hasPermission;
        private boolean preventProviderMock = false;
        public RequestDataBehaviorBuilder forProject(String projectKey) {
            this.projectKey = projectKey;
            return this;
        }
        public RequestDataBehaviorBuilder preventDataProviderMock() {
            this.preventProviderMock  = true;
            return this;
        }
        public RequestDataBehaviorBuilder withLevel(String level) {
            this.level = level;
            return this;
        }
        public RequestDataBehaviorBuilder withTimezone(String timezone) {
            this.zoneId = timezone;
            return this;
        }
        public RequestDataBehaviorBuilder withPermission() {
            this.hasPermission = true;
            return this;
        }
        public RequestDataBehaviorBuilder withoutPermission() {
            this.hasPermission = false;
            return this;
        }
        public RequestDataBehavior build() {
            validate();
            return new RequestDataBehavior(projectKey, level, zoneId, hasPermission, preventProviderMock);
        }
        private void validate() {
            if (projectKey == null) {
                Assertions.fail("Project key not configured");
            }
            if (level == null) {
                Assertions.fail("Level not configured");
            }
            if (zoneId == null) {
                Assertions.fail("ZoneId not configured");
            }
            if (hasPermission == null) {
                Assertions.fail("Permission not configured");
            }
        }
    }
    public class RequestDataBehavior implements DSLSimpleBehaviorWithAsserter<AssertResponse> {
        private final String projectKey;
        private final String level;
        private final String zoneId;
        private final boolean hasPermission;
        private boolean preventProviderMock;
        private AssertResponse asserter;
        public RequestDataBehavior(String projectKey, String level, String zoneId, boolean hasPermission, boolean preventProviderMock) {
            this.projectKey = projectKey;
            this.level = level;
            this.zoneId = zoneId;
            this.hasPermission = hasPermission;
            this.preventProviderMock = preventProviderMock;
        }

        @Override
        public void behave(KpiEnvironment environment) {
            ProjectDashboardOperationalPermission projectDashboardOperationalPermission = mockPermission();
            CycleTimeDataProvider dataProvider = mockProvider(environment);
            ProjectService projectService = environment.services().projects().getService();
            CycleTimeController subject = new CycleTimeController(projectDashboardOperationalPermission, projectService, dataProvider);
            asserter = AssertResponse.of(subject.get(projectKey, zoneId, level));
        }

        private CycleTimeDataProvider mockProvider(KpiEnvironment environment) {
            CycleTimeDataProvider cycleTimeDataProvider = Mockito.mock(CycleTimeDataProvider.class);
            if (preventProviderMock) {
                return cycleTimeDataProvider;
            }
            ZoneId timezone = DateTimeUtils.determineTimeZoneId(zoneId);
            KpiLevel kpiLevel = KpiLevel.valueOf(level.toUpperCase());
            Map<KpiLevel, List<IssueKpi>> issues = environment.services().issueKpi().getIssuesByLevel();
            List<CycleTimeKpi> cycleTimeKpis = issues.get(kpiLevel).stream()
                .map(i -> transformIntoCycleTimeKpi(i, environment, timezone))
                .collect(Collectors.toList());
            Mockito.when(cycleTimeDataProvider.getDataSet(projectKey, kpiLevel, timezone)).thenReturn(cycleTimeKpis);
            return cycleTimeDataProvider;
        }

        private CycleTimeKpi transformIntoCycleTimeKpi(IssueKpi issue, KpiEnvironment environment, ZoneId timezone) {
            Map<KpiLevel, Set<String>> cycleStatusMap = environment.withKpiProperties().getCycleStatusMap();
            IssueColorService colorService = environment.services().issueColor().getService();
            CycleTimeKpiFactory factory = new CycleTimeKpiFactory(cycleStatusMap, timezone, colorService);
            return factory.create(issue);
        }

        private ProjectDashboardOperationalPermission mockPermission() {
            ProjectDashboardOperationalPermission projectDashboardOperationalPermission = Mockito.mock(ProjectDashboardOperationalPermission.class);
            Mockito.when(projectDashboardOperationalPermission.isAuthorizedFor(projectKey)).thenReturn(hasPermission);
            return projectDashboardOperationalPermission;
        }

        @Override
        public AssertResponse then() {
            return asserter;
        }
    }
}