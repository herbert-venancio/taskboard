package objective.taskboard.followup.kpi.bugbyenvironment;

import static objective.taskboard.followup.kpi.properties.KpiBugByEnvironmentMocker.withBugTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardOperationalPermission;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.filters.KpiWeekRange;
import objective.taskboard.followup.kpi.properties.KpiBugByEnvironmentProperties;
import objective.taskboard.followup.kpi.services.DSLKpi;
import objective.taskboard.followup.kpi.services.KpiEnvironment;
import objective.taskboard.followup.kpi.services.RequestChartDataBehavior;
import objective.taskboard.followup.kpi.services.RequestChartDataBehaviorBuilder;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;
import objective.taskboard.utils.DateTimeUtils;
import objective.taskboard.utils.RangeUtils;

public class BugByEnvironmentControllerTest {

    @Test
    public void requestBugByEnvironmentData_happyDay() {
        dsl()
            .environment()
                .services()
                    .projects()
                        .withKey("PROJ")
                            .startAt("2019-04-21")
                            .deliveredAt("2019-04-27")
                        .eoP()
                    .eoPs()
                .eoS()
                .givenFeature("I-1")
                    .project("PROJ")
                    .type("Bug")
                    .withPreconfiguredTransition("Open and Closed at week of 2019-04-21")
                .eoI()
                .givenFeature("I-2")
                    .project("PROJ")
                    .type("Bug")
                    .withPreconfiguredTransition("Open and Closed at week of 2019-04-21")
                    .fields()
                        .field("clientEnvironment").value("Production")
                    .eoF()
                .eoI()
                .givenSubtask("I-3")
                    .project("PROJ")
                    .type("Alpha Bug")
                    .withPreconfiguredTransition("Open and Closed at week of 2019-04-21")
                .eoI()
                .givenSubtask("I-4")
                    .project("PROJ")
                    .type("Alpha Bug")
                    .withPreconfiguredTransition("Open and Closed at week of 2019-04-21")
                    .fields()
                        .field("clientEnvironment").value("Alpha")
                    .eoF()
                .eoI()
            .when()
                .appliesBehavior(createRequestDataBehavior()
                        .forProject("PROJ")
                        .withNoLevelConfigured()
                        .withTimezone("America/Sao_Paulo")
                        .withPermission()
                        .build())
            .then()
                .httpStatus(HttpStatus.OK)
                .bodyClass(List.class)
                .bodyAsJson("["
                    + "{"
                        + "\"date\": " + parseZonedDateAsMillis("2019-04-21", "America/Sao_Paulo") + ","
                        + "\"bugCategory\": \"Alpha\""+ ","
                        + "\"totalOfBugs\": 1"
                    + "},"
                    + "{"
                        + "\"date\": " + parseZonedDateAsMillis("2019-04-21", "America/Sao_Paulo") + ","
                        + "\"bugCategory\": \"Alpha Bug\""+ ","
                        + "\"totalOfBugs\": 1"
                    + "},"
                    + "{"
                        + "\"date\": " + parseZonedDateAsMillis("2019-04-21", "America/Sao_Paulo") + ","
                        + "\"bugCategory\": \"Bug\""+ ","
                        + "\"totalOfBugs\": 1"
                    + "},"
                    + "{"
                        + "\"date\": " + parseZonedDateAsMillis("2019-04-21", "America/Sao_Paulo") + ","
                        + "\"bugCategory\": \"Production\""+ ","
                        + "\"totalOfBugs\": 1"
                    + "}"
                + "]");
    }
    
    @Test
    public void requestBugByEnvironmentChartData_whenNotHavePermission_thenStatusNotFound() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("PROJ")
                        .startAt("2019-04-21")
                        .deliveredAt("2019-04-27")
                    .eoP()
                .eoPs()
            .eoS()
            .givenFeature("I-1")
                .project("PROJ")
                .type("Bug")
                .withPreconfiguredTransition("Open and Closed at week of 2019-04-21")
            .eoI()
            .givenFeature("I-2")
                .project("PROJ")
                .type("Bug")
                .withPreconfiguredTransition("Open and Closed at week of 2019-04-21")
                .fields()
                    .field("clientEnvironment").value("Production")
                .eoF()
            .eoI()
            .givenSubtask("I-3")
                .project("PROJ")
                .type("Alpha Bug")
                .withPreconfiguredTransition("Open and Closed at week of 2019-04-21")
            .eoI()
            .givenSubtask("I-4")
                .project("PROJ")
                .type("Alpha Bug")
                .withPreconfiguredTransition("Open and Closed at week of 2019-04-21")
                .fields()
                    .field("clientEnvironment").value("Alpha")
                .eoF()
            .eoI()
        .when()
            .appliesBehavior(createRequestDataBehavior()
                    .forProject("PROJ")
                    .withNoLevelConfigured()
                    .withTimezone("America/Sao_Paulo")
                    .withoutPermission()
                    .build())
        .then()
            .httpStatus(HttpStatus.NOT_FOUND)
            .bodyAsString("Project not found: PROJ.");
    }
    
    @Test
    public void requestBugByEnvironmentChartData_whenProjectDoesNotExists_thenStatusNotFound() {
        dsl().environment()
            .services()
                .projects()
                    .withKey("PROJ")
                        .startAt("2019-04-21")
                        .deliveredAt("2019-04-27")
                    .eoP()
                .eoPs()
            .eoS()
            .givenFeature("I-1")
                .project("PROJ")
                .type("Bug")
                .withPreconfiguredTransition("Open and Closed at week of 2019-04-21")
            .eoI()
            .givenFeature("I-2")
                .project("PROJ")
                .type("Bug")
                .withPreconfiguredTransition("Open and Closed at week of 2019-04-21")
                .fields()
                    .field("clientEnvironment").value("Production")
                .eoF()
            .eoI()
            .givenSubtask("I-3")
                .project("PROJ")
                .type("Alpha Bug")
                .withPreconfiguredTransition("Open and Closed at week of 2019-04-21")
            .eoI()
            .givenSubtask("I-4")
                .project("PROJ")
                .type("Alpha Bug")
                .withPreconfiguredTransition("Open and Closed at week of 2019-04-21")
                .fields()
                    .field("clientEnvironment").value("Alpha")
                .eoF()
            .eoI()
        .when()
            .appliesBehavior(createRequestDataBehavior()
                    .forProject("NONEXISTENT")
                    .withNoLevelConfigured()
                    .withTimezone("America/Sao_Paulo")
                    .withPermission()
                    .build())
        .then()
            .httpStatus(HttpStatus.NOT_FOUND)
            .bodyAsString("Project not found: NONEXISTENT."); 
    }
    
    private long parseZonedDateAsMillis(String date, String timezone) {
        return DateTimeUtils.parseDateTime(date, "00:00:00", timezone).toEpochSecond() * 1000;
    }
    
    private RequestBugByEnvironmentDataBehaviorBuilder createRequestDataBehavior() {
        return new RequestBugByEnvironmentDataBehaviorBuilder();
    }

    
    private class RequestBugByEnvironmentDataBehaviorBuilder extends RequestChartDataBehaviorBuilder<RequestBugByEnvironmentDataBehavior> {

        @Override
        public RequestBugByEnvironmentDataBehavior doBuild() {
            return new RequestBugByEnvironmentDataBehavior(projectKey, zoneId, hasPermission, preventProviderMock);
        }
        
    }
    
    private class RequestBugByEnvironmentDataBehavior extends RequestChartDataBehavior<BugByEnvironmentDataProvider> {

        public RequestBugByEnvironmentDataBehavior(
                String projectKey, String zoneId,
                boolean hasPermission, 
                boolean preventProviderMock) {
            super(projectKey, zoneId, hasPermission, preventProviderMock);
        }

        @Override
        public void doBehave(KpiEnvironment environment, ProjectDashboardOperationalPermission permission, ProjectService projectService) {
            BugByEnvironmentDataProvider dataProvider = mockProvider(environment);
            BugByEnvironmentController subject = new BugByEnvironmentController(permission, projectService, dataProvider);
            asserter = AssertResponse.of(subject.get(projectKey, zoneId));
        }

        @Override
        protected BugByEnvironmentDataProvider mockProvider(KpiEnvironment environment) {
            BugByEnvironmentDataProvider bugsByEnvironmentDataProvider = Mockito.mock(BugByEnvironmentDataProvider.class);
            if (preventProviderMock) {
                return bugsByEnvironmentDataProvider;
            }
            ZoneId timezone = DateTimeUtils.determineTimeZoneId(zoneId);

            MetadataService metadataService = environment.services().metadata().getService();
            List<IssueKpi> issues = environment.services().issueKpi().getAllIssues();
            KpiBugByEnvironmentProperties properties = environment.getKPIProperties(KpiBugByEnvironmentProperties.class);
            
            LocalDate startDateOfWeek = LocalDate.parse("2019-04-21");
            LocalDate endDateOfWeek = LocalDate.parse("2019-04-27");
            
            Range<LocalDate> projectRange = RangeUtils.between(startDateOfWeek, endDateOfWeek);
            KpiWeekRange weekRange = new KpiWeekRange(projectRange, timezone);
            
            Instant dateInstant = startDateOfWeek.atStartOfDay(timezone).toInstant();
            List<BugByEnvironmentDataPoint> bugByEnvironmentPoints = new BugCounterCalculator(metadataService, properties, issues)
                                                                                .getBugsCategorizedOnWeek(weekRange)
                                                                                .entrySet().stream()
                                                                                .map( entry -> new BugByEnvironmentDataPoint(dateInstant, entry.getKey(), entry.getValue()))
                                                                                .sorted()
                                                                                .collect(Collectors.toList());
            
            Mockito.when(bugsByEnvironmentDataProvider.getDataSet(projectKey, timezone)).thenReturn(bugByEnvironmentPoints);
            return bugsByEnvironmentDataProvider;
        }


    }
    
    private DSLKpi dsl() {
        return new DSLKpi()
                    .environment()
                        .types()
                            .addFeatures("Bug")
                            .addSubtasks("Alpha Bug")
                        .eoT()
                        .statuses()
                            .withNotProgressingStatuses("Open","To Do","Done")
                            .withProgressingStatuses("Doing")
                       .eoS()
                       .withJiraProperties()
                           .followUp().withExcludedStatuses("Open").eof()
                       .eoJp()
                       .withKpiProperties()
                           .environmentField("clientEnvironment")
                       .eoKP()
                       .withKpiProperties(withBugTypes("Bug","Alpha Bug"))
                       .preConfigureTransitions("Open and Closed at week of 2019-04-21")
                           .status("Open").date("2019-04-21")
                           .status("To Do").date("2019-04-23")
                           .status("Doing").date("2019-04-25")
                           .status("Done").date("2019-04-26")
                       .eoSt()
                    .eoE();
    }

}
