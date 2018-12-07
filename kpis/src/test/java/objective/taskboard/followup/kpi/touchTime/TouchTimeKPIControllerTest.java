package objective.taskboard.followup.kpi.touchTime;

import static org.mockito.Mockito.when;

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

    @InjectMocks
    private TouchTimeKPIController subject;

    @Test
    public void requestTouchTimeChartData_happyPath() {
        final String projectKey = "TEST";
        final String level = "Subtasks";
        final String zoneId = "America/Sao_Paulo";

        final List<TouchTimeDataPoint> emptyList = Arrays.asList(
                new TouchTimeDataPoint("I-1", "Backend Development", "Doing", 5.0),
                new TouchTimeDataPoint("I-2", "Backend Development", "Doing", 8.0),
                new TouchTimeDataPoint("I-3", "Backend Development", "Reviewing", 2.0));
        configureTestEnvironmentForProject(projectKey)
            .projectExists()
            .withOperationPermission()
            .withSubtaskDataSet(new TouchTimeChartDataSet(emptyList));

        AssertResponse.of(subject.byIssues(projectKey, zoneId, level))
            .httpStatus(HttpStatus.OK)
            .bodyClass(TouchTimeChartDataSet.class)
            .bodyAsJson(
                    "{\"points\":"
                        + "["
                            + "{"
                                + "\"issueKey\": \"I-1\","
                                + "\"issueType\": \"Backend Development\","
                                + "\"issueStatus\": \"Doing\","
                                + "\"effortInHours\": 5.0"
                            + "},"
                            + "{"
                                + "\"issueKey\": \"I-2\","
                                + "\"issueType\": \"Backend Development\","
                                + "\"issueStatus\": \"Doing\","
                                + "\"effortInHours\":8.0"
                            + "},"
                            + "{"
                                + "\"issueKey\": \"I-3\","
                                + "\"issueType\": \"Backend Development\","
                                + "\"issueStatus\": \"Reviewing\","
                                + "\"effortInHours\": 2.0"
                            + "}"
                        + "]"
                   + "}");
    }

    @Test
    public void requestTouchTimeChartData_whenNotHavePermission_thenStatusNotFound() {
        final String projectKey = "TEST";
        final String level = "Subtasks";
        final String zoneId = "America/Sao_Paulo";
        
        configureTestEnvironmentForProject(projectKey)
            .projectExists()
            .withoutOperationalPermisson();

        AssertResponse.of(subject.byIssues(projectKey, zoneId, level))
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

        AssertResponse.of(subject.byIssues(projectKey, zoneId, level))
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

        AssertResponse.of(subject.byIssues(projectKey, zoneId, level))
            .httpStatus(HttpStatus.BAD_REQUEST)
            .bodyAsString(String.format("Invalid level value: %s.", level));
    }

    private TestEnvironmentDSL configureTestEnvironmentForProject(String projectKey) {
        return new TestEnvironmentDSL(projectKey);
    }

    private class TestEnvironmentDSL {

        private String projectKey;

        public TestEnvironmentDSL(String projectKey) {
            super();
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

        public TestEnvironmentDSL withSubtaskDataSet(TouchTimeChartDataSet dataset) {
            when(touchTimeKpiDataProvider.getDataSet(projectKey, KpiLevel.SUBTASKS, ZONE_ID))
                .thenReturn(dataset);
            return this;
        }
    }
}