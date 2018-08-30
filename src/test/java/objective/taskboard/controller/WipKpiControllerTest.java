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
import objective.taskboard.followup.WipChartDataSet;
import objective.taskboard.followup.WipKPIDataProvider;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;

@RunWith(MockitoJUnitRunner.class)
public class WipKpiControllerTest {
    
    @Mock
    private Authorizer authorizer;
    
    @Mock
    private ProjectService projectService;
    
    @Mock
    private WipKPIDataProvider wipDataProvider;

    @InjectMocks
    private WipKPIController subject;
    
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
        when(wipDataProvider.getWipChartDataSet(projectKey, level, timezone))
            .thenReturn(new WipChartDataSet(null));
    }
    
    @Test
    public void requestWipChartData_happydays() {
        AssertResponse.of(subject.data(projectKey, zoneId, level))
            .httpStatus(HttpStatus.OK)
            .bodyClass(WipChartDataSet.class);
    }
    
    @Test
    public void requestWipChartData_withoutPermission() {
        when(authorizer.hasPermissionInProject(DASHBOARD_TACTICAL, projectKey)).thenReturn(false);
        when(authorizer.hasPermissionInProject(DASHBOARD_OPERATIONAL, projectKey)).thenReturn(false);

        AssertResponse.of(subject.data(projectKey, zoneId, level))
            .httpStatus(HttpStatus.FORBIDDEN);
    }
    
    @Test
    public void requestWipChartData_withTacticalPermissionOnly() {
        when(authorizer.hasPermissionInProject(DASHBOARD_OPERATIONAL, projectKey)).thenReturn(false);
        
        AssertResponse.of(subject.data(projectKey, zoneId, level))
            .httpStatus(HttpStatus.OK)
            .bodyClass(WipChartDataSet.class);
    }
    
    @Test
    public void requestWipChartData_withOperationalPermissionOnly() {
        when(authorizer.hasPermissionInProject(DASHBOARD_TACTICAL, projectKey)).thenReturn(false);
        
        AssertResponse.of(subject.data(projectKey, zoneId, level))
            .httpStatus(HttpStatus.OK)
            .bodyClass(WipChartDataSet.class);
    }
    
    @Test
    public void requestWipChartData_projectDoesNotExists() {
        when(projectService.taskboardProjectExists(projectKey)).thenReturn(false);
        
        AssertResponse.of(subject.data(projectKey, zoneId, level))
            .httpStatus(HttpStatus.NOT_FOUND)
            .bodyAsString(String.format("Project not found: %s.", projectKey));
    }

}
