package objective.taskboard.followup.kpi.touchTime;

import static objective.taskboard.utils.DateTimeUtils.determineTimeZoneId;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardOperationalPermission;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.jira.ProjectService;

@RestController
@RequestMapping(value = "/api/projects/{project}/followup/touchtime")
class TouchTimeKPIController {
	
    private ProjectDashboardOperationalPermission projectDashboardOperationalPermission;
    
    private ProjectService projectService;
   
    private Map<String,TouchTimeProvider<?>> providerMap;
    

    @Autowired
    public TouchTimeKPIController(ProjectDashboardOperationalPermission projectDashboardOperationalPermission,
			ProjectService projectService, TouchTimeByWeekDataProvider touchTimeByWeekDataProvider,
			TouchTimeKPIDataProvider touchTimeKpiDataProvider) {
		super();
		this.projectDashboardOperationalPermission = projectDashboardOperationalPermission;
		this.projectService = projectService;

    	this.providerMap = new LinkedHashMap<>();
    	this.providerMap.put("byIssues", touchTimeKpiDataProvider);
    	this.providerMap.put("byWeek", touchTimeByWeekDataProvider);
	}

    @GetMapping("{method}")
    public ResponseEntity<Object> getData(
    		@PathVariable("method") String method,
            @PathVariable("project") String projectKey,
            @RequestParam("timezone") String zoneId,
            @RequestParam("level") String level) {

        KpiLevel kpiLevel;
        try {
            validate(projectKey);
            kpiLevel = getLevel(level);
        } catch (KpiValidationException e) { //NOSONAR
            return new ResponseEntity<>(e.getMessage(),e.getStatus());
        }
        
        ZoneId timezone = determineTimeZoneId(zoneId);
        
        return getResponse(method, projectKey, kpiLevel, timezone);
    }

	private ResponseEntity<Object> getResponse(String method, String projectKey, KpiLevel kpiLevel, ZoneId timezone) {
		return Optional.ofNullable(providerMap.get(method))
				.map(provider -> getOkResponse(provider,projectKey, kpiLevel, timezone))
				.orElse(new ResponseEntity<>(String.format("Method not found: %s", method),HttpStatus.NOT_FOUND));
	}
	
	public ResponseEntity<Object> getOkResponse(TouchTimeProvider<?> provider, String projectKey, KpiLevel kpiLevel, ZoneId timezone){
		return new ResponseEntity<>(provider.getDataSet(projectKey, kpiLevel, timezone), HttpStatus.OK);
	}
    
    private KpiLevel getLevel(String level) throws KpiValidationException{
        try {
            return KpiLevel.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {//NOSONAR
            final String message = String.format("Invalid level value: %s.", level);
            throw new KpiValidationException(HttpStatus.BAD_REQUEST,message);
        }
    }
    
    private void validate(String projectKey) throws KpiValidationException {
        final String projectExceptionMessage = String.format("Project not found: %s.", projectKey);
        if (!projectDashboardOperationalPermission.isAuthorizedFor(projectKey))
            throw new KpiValidationException(HttpStatus.NOT_FOUND,projectExceptionMessage);
    
        if (!projectService.taskboardProjectExists(projectKey)) {
            throw new KpiValidationException(HttpStatus.NOT_FOUND,projectExceptionMessage);
        }
    }
    
}