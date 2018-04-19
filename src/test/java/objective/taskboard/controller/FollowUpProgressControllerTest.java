package objective.taskboard.controller;

import static objective.taskboard.config.CacheConfiguration.DASHBOARD_PROGRESS_DATA;
import static objective.taskboard.repository.PermissionRepository.DASHBOARD_TACTICAL;
import static objective.taskboard.utils.DateTimeUtils.determineTimeZoneId;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cache.CacheManager;
import org.springframework.cache.guava.GuavaCacheManager;

import objective.taskboard.auth.Authorizer;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.EffortHistoryRow;
import objective.taskboard.followup.FollowUpDataSnapshot;
import objective.taskboard.followup.FollowUpDataSnapshotHistory;
import objective.taskboard.followup.data.ProgressData;
import objective.taskboard.followup.impl.FollowUpDataProviderFromCurrentState;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;

@RunWith(MockitoJUnitRunner.class)
public class FollowUpProgressControllerTest {

    private static final String PROJECT_KEY = "TEST";
    private static final LocalDate START_DATE = LocalDate.of(2017, 12, 30);
    private static final LocalDate END_DATE = LocalDate.of(2017, 12, 31);
    private static final Integer PROJECT_PROJECTION_TIMESPAN = 12;
    private static final String ZONE_ID = "America/Sao_Paulo";

    @Mock
    private FollowUpDataProviderFromCurrentState providerFromCurrentState;

    @Mock
    private ProjectFilterConfigurationCachedRepository projects;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Authorizer authorizer;

    @InjectMocks
    private FollowUpProgressController subject;

    @Mock
    private FollowUpDataSnapshot snapshot;

    @Mock
    private FollowUpDataSnapshotHistory snapshotHistory;

    private ProjectFilterConfiguration project;

    @Before
    public void setup() {
        setupValidProject();

        MockitoAnnotations.initMocks(this);
        when(authorizer.hasPermissionInProject(DASHBOARD_TACTICAL, PROJECT_KEY)).thenReturn(true);
        when(cacheManager.getCache(DASHBOARD_PROGRESS_DATA)).thenReturn(new GuavaCacheManager(DASHBOARD_PROGRESS_DATA).getCache(DASHBOARD_PROGRESS_DATA));
        when(projects.getProjectByKey(PROJECT_KEY)).thenReturn(Optional.of(project));
        when(snapshotHistory.getHistoryRows()).thenReturn(Arrays.asList(new EffortHistoryRow(LocalDate.now())));
        when(snapshot.hasClusterConfiguration()).thenReturn(true);
        when(snapshot.getHistory()).thenReturn(Optional.of(snapshotHistory));
        when(providerFromCurrentState.getJiraData(new String[]{PROJECT_KEY}, determineTimeZoneId(ZONE_ID))).thenReturn(snapshot);

        subject.initCache();
    }

    private void setupValidProject() {
        project = new ProjectFilterConfiguration(PROJECT_KEY);
        project.setStartDate(START_DATE);
        project.setDeliveryDate(END_DATE);
        project.setProjectionTimespan(PROJECT_PROJECTION_TIMESPAN);
    }

    @Test
    public void ifAllValidationsWereSuccessful_returnOkAndTheCorrectValue() {
        final Integer projectionTimespan = 20;

        ProgressData responseBody = (ProgressData) AssertResponse.of(subject.progress(PROJECT_KEY, ZONE_ID, Optional.of(projectionTimespan)))
                .httpStatus(OK)
                .bodyClass(ProgressData.class)
                .getResponse().getBody();

        assertEquals(projectionTimespan, responseBody.projectionTimespan);
    }

    @Test
    public void ifProjectionParameterWasNotPassed_returnOkAndTheCorrectValueWithDefaultProjectProjection() {
        ProgressData responseBody = (ProgressData) AssertResponse.of(subject.progress(PROJECT_KEY, ZONE_ID, Optional.empty()))
                .httpStatus(OK)
                .bodyClass(ProgressData.class)
                .getResponse().getBody();

        assertEquals(PROJECT_PROJECTION_TIMESPAN, responseBody.projectionTimespan);
    }

    @Test
    public void ifUserHasNoPermissionInTactical_returnResourceNotFound() throws Exception {
        when(authorizer.hasPermissionInProject(DASHBOARD_TACTICAL, PROJECT_KEY)).thenReturn(false);

        AssertResponse.of(subject.progress(PROJECT_KEY, ZONE_ID, Optional.empty()))
                .httpStatus(NOT_FOUND)
                .bodyAsString("Resource not found.");
    }

    @Test
    public void ifSomeErrorOccur_returnInternalServerError() throws Exception {
        when(projects.getProjectByKey(PROJECT_KEY)).thenReturn(null);

        AssertResponse.of(subject.progress(PROJECT_KEY, ZONE_ID, Optional.empty()))
                .httpStatus(INTERNAL_SERVER_ERROR)
                .bodyAsString("Unexpected behavior. Please, report this error to the administrator.");
    }

    @Test
    public void ifProjectNotFound_returnNotFound() throws Exception {
        when(projects.getProjectByKey(PROJECT_KEY)).thenReturn(Optional.empty());

        AssertResponse.of(subject.progress(PROJECT_KEY, ZONE_ID, Optional.empty()))
                .httpStatus(NOT_FOUND)
                .bodyAsString("Project not found: "+ PROJECT_KEY +".");
    }

    @Test
    public void ifProjectionTimespanIsNegativeOrZero_returnNotFound() throws Exception {
        AssertResponse.of(subject.progress(PROJECT_KEY, ZONE_ID, Optional.of(-1)))
                .httpStatus(BAD_REQUEST)
                .bodyAsString("The projection timespan should be a positive number.");

        AssertResponse.of(subject.progress(PROJECT_KEY, ZONE_ID, Optional.of(0)))
                .httpStatus(BAD_REQUEST)
                .bodyAsString("The projection timespan should be a positive number.");
    }

    @Test
    public void ifProjectDeliveryDateIsNull_returnInternalServerError() throws Exception {
        project.setDeliveryDate(null);

        AssertResponse.of(subject.progress(PROJECT_KEY, ZONE_ID, Optional.empty()))
                .httpStatus(INTERNAL_SERVER_ERROR)
                .bodyAsString("The project "+ PROJECT_KEY +" has no delivery date.");
    }

    @Test
    public void ifProjectStartDateIsNull_returnInternalServerError() throws Exception {
        project.setStartDate(null);

        AssertResponse.of(subject.progress(PROJECT_KEY, ZONE_ID, Optional.empty()))
                .httpStatus(INTERNAL_SERVER_ERROR)
                .bodyAsString("The project "+ PROJECT_KEY +" has no start date.");
    }

    @Test
    public void ifSnapshotHasNoClusterConfiguration_returnInternalServerError() throws Exception {
        when(snapshot.hasClusterConfiguration()).thenReturn(false);

        AssertResponse.of(subject.progress(PROJECT_KEY, ZONE_ID, Optional.empty()))
                .httpStatus(INTERNAL_SERVER_ERROR)
                .bodyAsString("No cluster configuration found for project "+ PROJECT_KEY +".");
    }

    @Test
    public void ifSnapshotHasNoProgressHistory_returnInternalServerError() throws Exception {
        when(snapshot.getHistory()).thenReturn(Optional.empty());

        AssertResponse.of(subject.progress(PROJECT_KEY, ZONE_ID, Optional.empty()))
                .httpStatus(INTERNAL_SERVER_ERROR)
                .bodyAsString("No progress history found for project "+ PROJECT_KEY +".");
    }

}
