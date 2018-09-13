package objective.taskboard.controller;

import static objective.taskboard.repository.PermissionRepository.DASHBOARD_OPERATIONAL;
import static objective.taskboard.repository.PermissionRepository.DASHBOARD_TACTICAL;
import static org.mockito.Mockito.when;

import java.time.ZoneId;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import objective.taskboard.auth.Authorizer;
import objective.taskboard.followup.ThroughputKPIDataProvider;
import objective.taskboard.followup.kpi.ThroughputChartDataSet;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;

@RunWith(MockitoJUnitRunner.class)
public class ThroughputKPIControllerTest {
    
    @Mock
    private Authorizer authorizer;
    
    @Mock
    private ProjectService projectService;
    
    @Mock
    private ThroughputKPIDataProvider throughputDataProvider;

    @InjectMocks
    private ThroughputKPIController subject;
    
    private String projectKey;

    private String zoneId;

    private String level;
    
    @Before
    public void setup() {
        projectKey = "TEST";
        zoneId = "America/Sao_Paulo";
        final ZoneId timezone = ZoneId.of(zoneId);
        level = "Subtask";
        when(authorizer.hasPermissionInProject(DASHBOARD_TACTICAL, projectKey)).thenReturn(true);
        when(authorizer.hasPermissionInProject(DASHBOARD_OPERATIONAL, projectKey)).thenReturn(true);
        when(projectService.taskboardProjectExists(projectKey)).thenReturn(true);
        when(throughputDataProvider.getDataSet(projectKey, level, timezone))
            .thenReturn(new ThroughputChartDataSet(null));
    }
    
    @Test
    public void requestThroughputChartData_happydays() {
        AssertResponse.of(subject.data(projectKey, zoneId, level))
            .httpStatus(HttpStatus.OK)
            .bodyClass(ThroughputChartDataSet.class);
    }
    
    @Test
    public void requestThroughputChartData_withoutPermission() {
        when(authorizer.hasPermissionInProject(DASHBOARD_TACTICAL, projectKey)).thenReturn(false);
        when(authorizer.hasPermissionInProject(DASHBOARD_OPERATIONAL, projectKey)).thenReturn(false);

        AssertResponse.of(subject.data(projectKey, zoneId, level))
            .httpStatus(HttpStatus.NOT_FOUND);
    }
    
    @Test
    public void requestThroughputChartData_withTacticalPermissionOnly() {
        when(authorizer.hasPermissionInProject(DASHBOARD_OPERATIONAL, projectKey)).thenReturn(false);
        
        AssertResponse.of(subject.data(projectKey, zoneId, level))
            .httpStatus(HttpStatus.OK)
            .bodyClass(ThroughputChartDataSet.class);
    }
    
    @Test
    public void requestThroughputChartData_withOperationalPermissionOnly() {
        when(authorizer.hasPermissionInProject(DASHBOARD_TACTICAL, projectKey)).thenReturn(false);
        
        AssertResponse.of(subject.data(projectKey, zoneId, level))
            .httpStatus(HttpStatus.OK)
            .bodyClass(ThroughputChartDataSet.class);
    }
    
    @Test
    public void requestThroughputChartData_projectDoesNotExists() {
        when(projectService.taskboardProjectExists(projectKey)).thenReturn(false);
        
        AssertResponse.of(subject.data(projectKey, zoneId, level))
            .httpStatus(HttpStatus.NOT_FOUND)
            .bodyAsString(String.format("Project not found: %s.", projectKey));
    }

}
