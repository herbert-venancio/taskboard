package objective.taskboard.followup.kpi.touchTime;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static objective.taskboard.utils.DateTimeUtils.parseStringToDate;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardOperationalPermission;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;

@RunWith(MockitoJUnitRunner.class)
public class TouchTimeKPIControllerTest {

    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Mock
    private ProjectDashboardOperationalPermission projectDashboardOperationalPermission;

    @Mock
    private ProjectService projectService;

    @Mock
    private TouchTimeKPIDataProvider touchTimeKpiDataProvider;
    
    @Mock
    private TouchTimeByWeekDataProvider touchTimeByWeekDataProvider;
    
    @InjectMocks
    private TouchTimeKPIController subject;

    @Test
    public void requestTouchTimeChartData_happyPath() {
        final String projectKey = "TEST";
        final String level = "Subtasks";
        final String zoneId = "America/Sao_Paulo";

        final Instant startProgressingDate = parseDateTime("2018-12-06").toInstant();
        final Instant endProgressingDate = parseDateTime("2018-12-07").toInstant();
        final List<TouchTimeDataPoint> issuesList = Arrays.asList(
                new TouchTimeDataPoint("I-1", "Backend Development", "Doing", 5.0, startProgressingDate, endProgressingDate),
                new TouchTimeDataPoint("I-2", "Backend Development", "Doing", 8.0, startProgressingDate, endProgressingDate),
                new TouchTimeDataPoint("I-3", "Backend Development", "Reviewing", 2.0, startProgressingDate, endProgressingDate));
        configureTestEnvironmentForProject(projectKey)
            .projectExists()
            .withOperationPermission()
            .withSubtaskDataSet(new TouchTimeChartDataSet(issuesList));

        AssertResponse.of(subject.getData("byissues",projectKey, zoneId, level))
            .httpStatus(HttpStatus.OK)
            .bodyClass(TouchTimeChartDataSet.class)
            .bodyAsJson(
                    "{\"points\":"
                        + "["
                            + "{"
                                + "\"issueKey\": \"I-1\","
                                + "\"issueType\": \"Backend Development\","
                                + "\"issueStatus\": \"Doing\","
                                + "\"effortInHours\": 5.0,"
                                + "\"startProgressingDate\": 1544061600000,"
                                + "\"endProgressingDate\": 1544148000000"
                            + "},"
                            + "{"
                                + "\"issueKey\": \"I-2\","
                                + "\"issueType\": \"Backend Development\","
                                + "\"issueStatus\": \"Doing\","
                                + "\"effortInHours\":8.0,"
                                + "\"startProgressingDate\": 1544061600000,"
                                + "\"endProgressingDate\": 1544148000000"
                            + "},"
                            + "{"
                                + "\"issueKey\": \"I-3\","
                                + "\"issueType\": \"Backend Development\","
                                + "\"issueStatus\": \"Reviewing\","
                                + "\"effortInHours\": 2.0,"
                                + "\"startProgressingDate\": 1544061600000,"
                                + "\"endProgressingDate\": 1544148000000"
                            + "}"
                        + "]"
                   + "}");
    }
    
    @Test
    public void requestTouchTimeByWeekChartData_happyPath() {
        final String projectKey = "TEST";
        final String level = "Subtasks";
        final String zoneId = "America/Sao_Paulo";

        final List<TouchTimeChartByWeekDataPoint> emptyList = Arrays.asList(
                new TouchTimeChartByWeekDataPoint(parseStringToDate("2018-11-18"),"Doing",5d),
                new TouchTimeChartByWeekDataPoint(parseStringToDate("2018-11-18"),"Reviewing",10d),
                new TouchTimeChartByWeekDataPoint(parseStringToDate("2018-11-25"),"Doing",15d),
                new TouchTimeChartByWeekDataPoint(parseStringToDate("2018-11-25"),"Reviewing",20d)
                );
        
        configureTestEnvironmentForProject(projectKey)
            .projectExists()
            .withOperationPermission()
            .withSubtaskByWeekDataSet(new TouchTimeChartByWeekDataSet(emptyList));

        AssertResponse.of(subject.getData("byWeek",projectKey, zoneId, level))
            .httpStatus(HttpStatus.OK)
            .bodyClass(TouchTimeChartByWeekDataSet.class)
            .bodyAsJson(
                    "{\"points\":"
                        + "["
                            + "{"
                                + "\"date\" : "+ getDateInMiliseconds("2018-11-18")+","
                                + "\"status\": \"Doing\","
                                + "\"effort\": 5.0"
                            + "},"
                            + "{"
                                + "\"date\" : "+ getDateInMiliseconds("2018-11-18")+","
                                + "\"status\": \"Reviewing\","
                                + "\"effort\": 10.0"
                            + "},"
                            + "{"
                                + "\"date\" : "+ getDateInMiliseconds("2018-11-25")+","
                                + "\"status\": \"Doing\","
                                + "\"effort\": 15.0"
                            + "},"
                            + "{"
                                + "\"date\" : "+ getDateInMiliseconds("2018-11-25")+","
                                + "\"status\": \"Reviewing\","
                                + "\"effort\": 20.0"
                            + "}"
                        + "]"
                   + "}");
    }

	private long getDateInMiliseconds(String date) {
		return parseStringToDate(date).getTime();
	}

    @Test
    public void requestTouchTimeChartData_whenNotHavePermission_thenStatusNotFound() {
        final String projectKey = "TEST";
        final String level = "Subtasks";
        final String zoneId = "America/Sao_Paulo";
        
        configureTestEnvironmentForProject(projectKey)
            .projectExists()
            .withoutOperationalPermisson();

        AssertResponse.of(subject.getData("byissues",projectKey, zoneId, level))
            .httpStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    public void requestTouchTimeChartData_whenProjectDoesNotExists_thenStatusNotFound() {
        final String projectKey = "FOO";
        final String level = "Subtasks";
        final String zoneId = "America/Sao_Paulo";

        configureTestEnvironmentForProject(projectKey)
            .projectDoesntExist()
            .withOperationPermission();

        AssertResponse.of(subject.getData("byissues",projectKey, zoneId, level))
            .httpStatus(HttpStatus.NOT_FOUND)
            .bodyAsString(String.format("Project not found: %s.", projectKey));
    }

    @Test
    public void requestTouchTimeChartData_whenInvalidLevelValue_thenStatusBadRequest() {
        final String projectKey = "TEST";
        final String level = "Foo";
        final String zoneId = "America/Sao_Paulo";

        configureTestEnvironmentForProject(projectKey)
            .projectExists()
            .withOperationPermission();

        AssertResponse.of(subject.getData("byissues",projectKey, zoneId, level))
            .httpStatus(HttpStatus.BAD_REQUEST)
            .bodyAsString(String.format("Invalid level value: %s.", level));
    }
    
    @Test
    public void requestTouchTime_whenInvalidMethod_thenStatusNotFound() {
        final String projectKey = "TEST";
        final String level = "SUBTASKS";
        final String zoneId = "America/Sao_Paulo";

        configureTestEnvironmentForProject(projectKey)
            .projectExists()
            .withOperationPermission();

        AssertResponse.of(subject.getData("inexistent",projectKey, zoneId, level))
            .httpStatus(HttpStatus.NOT_FOUND)
            .bodyAsString("Method not found: inexistent");
    }

    private TestEnvironmentDSL configureTestEnvironmentForProject(String projectKey) {
        return new TestEnvironmentDSL(projectKey);
    }

    private class TestEnvironmentDSL {

        private String projectKey;

        public TestEnvironmentDSL(String projectKey) {
            this.projectKey = projectKey;
        }

        public TestEnvironmentDSL projectExists() {
            when(projectService.taskboardProjectExists(projectKey)).thenReturn(true);
            return this;
        }

        public TestEnvironmentDSL projectDoesntExist() {
            when(projectService.taskboardProjectExists(projectKey)).thenReturn(false);
            return this;
        }

        public TestEnvironmentDSL withOperationPermission() {
            when(projectDashboardOperationalPermission.isAuthorizedFor(projectKey)).thenReturn(true);
            return this;
        }

        public TestEnvironmentDSL withoutOperationalPermisson() {
            when(projectDashboardOperationalPermission.isAuthorizedFor(projectKey)).thenReturn(false);
            return this;
        }
        
        public TestEnvironmentDSL withSubtaskByWeekDataSet(TouchTimeChartByWeekDataSet dataset) {
            when(touchTimeByWeekDataProvider.getDataSet(projectKey, KpiLevel.SUBTASKS, ZONE_ID))
                .thenReturn(dataset);
            return this;
        }

        public TestEnvironmentDSL withSubtaskDataSet(TouchTimeChartDataSet dataset) {
            when(touchTimeKpiDataProvider.getDataSet(projectKey, KpiLevel.SUBTASKS, ZONE_ID))
                .thenReturn(dataset);
            return this;
        }
    }
}
