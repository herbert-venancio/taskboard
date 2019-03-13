package objective.taskboard.followup.kpi.leadTime;

import static objective.taskboard.followup.kpi.properties.KpiLeadTimePropertiesMocker.withSubtaskLeadTimeProperties;

import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardOperationalPermission;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.DSLKpi;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.followup.kpi.enviroment.RequestChartDataBehavior;
import objective.taskboard.followup.kpi.enviroment.RequestChartDataBehaviorBuilder;
import objective.taskboard.followup.kpi.leadtime.LeadTimeKpi;
import objective.taskboard.followup.kpi.leadtime.LeadTimeKpiController;
import objective.taskboard.followup.kpi.leadtime.LeadTimeKpiDataProvider;
import objective.taskboard.followup.kpi.leadtime.LeadTimeKpiFactory;
import objective.taskboard.followup.kpi.properties.KpiLeadTimeProperties;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;
import objective.taskboard.utils.DateTimeUtils;

public class LeadTimeControllerTest {

    @Test
    public void requestLeadTimeChartData_happyPath() {
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
                        .status("Done").noDate()
                        .status("Cancelled").date("2020-01-07")
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
                        + "\"leadTime\": 6,"
                        + "\"enterDate\": " + parseZonedDateAsMillis("2020-01-01", "America/Sao_Paulo") + ","
                        + "\"exitDate\": " + parseZonedDateAsMillis("2020-01-06", "America/Sao_Paulo") + ","
                        + "\"lastStatus\": \"Done\""
                    + "},"
                    + "{"
                        + "\"issueKey\": \"I-2\","
                        + "\"issueType\": \"Alpha Bug\","
                        + "\"leadTime\": 6,"
                        + "\"enterDate\": " + parseZonedDateAsMillis("2020-01-02", "America/Sao_Paulo") + ","
                        + "\"exitDate\": "+ parseZonedDateAsMillis("2020-01-07", "America/Sao_Paulo") + ","
                        + "\"lastStatus\": \"Cancelled\""
                    + "}"
                + "]");
    }

    @Test
    public void requestLeadTimeChartData_whenNotHavePermission_thenStatusNotFound() {
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
    public void requestLeadTimeChartData_whenProjectDoesNotExists_thenStatusNotFound() {
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
    public void requestLeadTimeChartData_whenInvalidLevelValue_thenStatusBadRequest() {
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
                .withNotProgressingStatuses("Open","To Do","To Review","Done","Cancelled")
                .withProgressingStatuses("Doing","Reviewing")
            .eoS()
            .types()
                .addSubtasks("Backend Development","Alpha Bug")
            .eoT()
            .withKpiProperties(
                withSubtaskLeadTimeProperties("Open","To Do","Doing","To Review","Reviewing")
            );
        return dsl;
    }

    private RequestLeadTimeDataBehaviorBuilder createRequestDataBehavior() {
        return new RequestLeadTimeDataBehaviorBuilder();
    }

    private long parseZonedDateAsMillis(String date, String timezone) {
        return DateTimeUtils.parseDateTime(date, "00:00:00", timezone).toEpochSecond() * 1000;
    }

    private class RequestLeadTimeDataBehaviorBuilder extends RequestChartDataBehaviorBuilder<RequestLeadTimeDataBehavior> {

        @Override
        public RequestLeadTimeDataBehavior doBuild() {
            return new RequestLeadTimeDataBehavior(projectKey, level, zoneId, hasPermission, preventProviderMock);
        }

    }

    private class RequestLeadTimeDataBehavior extends RequestChartDataBehavior<LeadTimeKpiDataProvider> {

        public RequestLeadTimeDataBehavior(String projectKey, String level, String zoneId,
                boolean hasPermission, boolean preventProviderMock) {
            super(projectKey, level, zoneId, hasPermission, preventProviderMock);
        }

        @Override
        public void doBehave(KpiEnvironment environment, ProjectDashboardOperationalPermission permission, ProjectService projectService) {
            LeadTimeKpiDataProvider dataProvider = mockProvider(environment);
            LeadTimeKpiController subject = new LeadTimeKpiController(permission, projectService, dataProvider);
            asserter = AssertResponse.of(subject.get(projectKey, zoneId, level));
        }

        @Override
        protected LeadTimeKpiDataProvider mockProvider(KpiEnvironment environment) {
            LeadTimeKpiDataProvider leadTimeDataProvider = Mockito.mock(LeadTimeKpiDataProvider.class);
            if (preventProviderMock) {
                return leadTimeDataProvider;
            }
            ZoneId timezone = DateTimeUtils.determineTimeZoneId(zoneId);
            KpiLevel kpiLevel = KpiLevel.valueOf(level.toUpperCase());
            Map<KpiLevel, List<IssueKpi>> issues = environment.services().issueKpi().getIssuesByLevel();
            List<LeadTimeKpi> leadTimeKpis = issues.get(kpiLevel).stream()
                .map(i -> transformIntoLeadTimeKpi(i, environment, timezone))
                .collect(Collectors.toList());
            Mockito.when(leadTimeDataProvider.getDataSet(projectKey, kpiLevel, timezone)).thenReturn(leadTimeKpis);
            return leadTimeDataProvider;
        }

        private LeadTimeKpi transformIntoLeadTimeKpi(IssueKpi issue, KpiEnvironment environment, ZoneId timezone) {
            Map<KpiLevel, Set<String>> leadStatusMap = environment.getKPIProperties(KpiLeadTimeProperties.class).getLeadTime().toMap();
            LeadTimeKpiFactory factory = new LeadTimeKpiFactory(leadStatusMap);
            return factory.create(issue);
        }

    }
}