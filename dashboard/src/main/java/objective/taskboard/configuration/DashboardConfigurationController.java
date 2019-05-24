package objective.taskboard.configuration;

import java.net.URI;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.auth.authorizer.permission.ProjectAdministrationPermission;
import objective.taskboard.configuration.exception.DashboardConfigurationDuplicateException;
import objective.taskboard.configuration.exception.DashboardConfigurationNotFoundException;
import objective.taskboard.configuration.exception.DashboardConfigurationParsingDtoException;

@RestController
@RequestMapping("/ws/dashboard/{projectKey}/config")
public class DashboardConfigurationController {

    private final DashboardConfigurationService dashboardConfigurationService;
    private final ProjectAdministrationPermission projectAdministrationPermission;

    @Autowired
    public DashboardConfigurationController(DashboardConfigurationService dashboardConfigurationService, ProjectAdministrationPermission projectAdministrationPermission) {
        this.dashboardConfigurationService = dashboardConfigurationService;
        this.projectAdministrationPermission = projectAdministrationPermission;
    }

    @GetMapping
    public ResponseEntity<Object> retrieve(@PathVariable String projectKey) {
        Optional<DashboardConfiguration> configuration = dashboardConfigurationService.retrieveConfiguration(projectKey);
        if (!configuration.isPresent())
            return ResponseEntity.notFound().build();
            
        return ResponseEntity.ok(DashboardConfigurationDto.of(configuration.get())); 
    }

    @PostMapping
    public ResponseEntity<Object> create(
            @PathVariable String projectKey,
            @RequestBody DashboardConfigurationDto dto) {
        if (!projectAdministrationPermission.isAuthorizedFor(projectKey))
            return ResponseEntity.notFound().build();

        DashboardConfiguration persistedConfiguration;
        try {
            persistedConfiguration = dashboardConfigurationService.persistConfigurationForProject(
                    projectKey, DashboardConfigurationDto.parse(dto));
        } catch (DashboardConfigurationParsingDtoException exp) { //NOSONAR
            return ResponseEntity.badRequest().body(exp.getMessage());
        } catch (DashboardConfigurationDuplicateException exp) { //NOSONAR
            return ResponseEntity.unprocessableEntity().body(exp.getMessage());
        }
        URI location = URI.create(String.format("/ws/dashboard/%s/config/%d", projectKey, persistedConfiguration.getId()));
        return ResponseEntity.created(location).body(DashboardConfigurationDto.of(persistedConfiguration));
    }

    @PutMapping
    public ResponseEntity<Object> update(
            @PathVariable String projectKey,
            @RequestBody DashboardConfigurationDto dto) {
        if (!projectAdministrationPermission.isAuthorizedFor(projectKey))
            return ResponseEntity.notFound().build();

        DashboardConfiguration updatedConfiguration;
        try {
            updatedConfiguration = dashboardConfigurationService.updateConfigurationForProject(projectKey, DashboardConfigurationDto.parse(dto));
        } catch (DashboardConfigurationParsingDtoException exp) { //NOSONAR
            return ResponseEntity.badRequest().body(exp.getMessage());
        } catch (DashboardConfigurationNotFoundException exp) { //NOSONAR
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(exp.getMessage());
        }
        return ResponseEntity.ok(DashboardConfigurationDto.of(updatedConfiguration));
    }
}
