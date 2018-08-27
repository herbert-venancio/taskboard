package objective.taskboard.project.config;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.repository.PermissionRepository.ADMINISTRATIVE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.domain.Project;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.ProjectService;

@RestController
@RequestMapping("/ws/project/config")
public class ProjectConfigurationController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("items")
    public List<ProjectListItemDto> getItems() {
        return projectService.getTaskboardProjects(ADMINISTRATIVE).stream()
                .map(ProjectListItemDto::from)
                .collect(toList());
    }

    @GetMapping("edit/{projectKey}/init-data")
    public ResponseEntity<?> editGetInitData(@PathVariable("projectKey") String projectKey) {
        Optional<ProjectFilterConfiguration> project = projectService.getTaskboardProject(projectKey, ADMINISTRATIVE);
        
        if (!project.isPresent())
            return ResponseEntity.notFound().build();
        
        List<LocalDate> availableBaselineDates = projectService.getAvailableBaselineDates(projectKey);
        ProjectConfigurationDto configDto = ProjectConfigurationDto.from(project.get());

        return ResponseEntity.ok(new ProjectConfigurationDataDto(availableBaselineDates, configDto));
    }

    @PostMapping("edit/{projectKey}")
    public ResponseEntity<?> editUpdate(@PathVariable("projectKey") String projectKey, @RequestBody ProjectConfigurationDto configDto) {
        Optional<ProjectFilterConfiguration> optConfiguration = projectService.getTaskboardProject(projectKey, ADMINISTRATIVE);
        if (!optConfiguration.isPresent())
            return ResponseEntity.notFound().build();

        ProjectFilterConfiguration project = optConfiguration.get();

        project.setStartDate(configDto.startDate);
        project.setDeliveryDate(configDto.deliveryDate);
        project.setArchived(configDto.isArchived);
        project.setRiskPercentage(configDto.risk == null ? null : configDto.risk.divide(BigDecimal.valueOf(100)));
        project.setProjectionTimespan(configDto.projectionTimespan);
        project.setDefaultTeam(configDto.defaultTeam);
        project.setBaselineDate(configDto.baselineDate);

        projectService.saveTaskboardProject(project);

        return ResponseEntity.ok().build();
    }

    @GetMapping("{projectKey}/name")
    public ResponseEntity<?> getName(@PathVariable("projectKey") String projectKey){
        Optional<ProjectFilterConfiguration> project = projectService.getTaskboardProject(projectKey, ADMINISTRATIVE);

        String errorMessage = "Project \""+ projectKey +"\" not found.";
        if (!project.isPresent())
            return new ResponseEntity<>(errorMessage, NOT_FOUND);

        Optional<Project> jiraProject = projectService.getJiraProjectAsUser(projectKey);
        if (!jiraProject.isPresent())
            throw new IllegalStateException(errorMessage);

        return ResponseEntity.ok(jiraProject.get().getName());
    }

    public static class ProjectListItemDto {
        public String projectKey;
        public Boolean isArchived;
        
        public static ProjectListItemDto from(ProjectFilterConfiguration configuration) {
            ProjectListItemDto dto = new ProjectListItemDto();
            dto.projectKey = configuration.getProjectKey();
            dto.isArchived = configuration.isArchived();
            return dto;
        }
    }
    
    public static class ProjectConfigurationDataDto {
        public List<LocalDate> availableBaselineDates;
        public ProjectConfigurationDto config;
        
        public ProjectConfigurationDataDto(List<LocalDate> availableBaselineDates, ProjectConfigurationDto config) {
            this.availableBaselineDates = availableBaselineDates;
            this.config = config;
        }
    }

    public static class ProjectConfigurationDto {
        public String projectKey;
        public LocalDate startDate;
        public LocalDate deliveryDate;
        public boolean isArchived;
        public BigDecimal risk;
        public Integer projectionTimespan;
        public Long defaultTeam;
        public LocalDate baselineDate;

        public static ProjectConfigurationDto from(ProjectFilterConfiguration project) {
            ProjectConfigurationDto dto = new ProjectConfigurationDto();
            dto.projectKey = project.getProjectKey();
            dto.startDate = project.getStartDate().orElse(null);
            dto.deliveryDate = project.getDeliveryDate().orElse(null);
            dto.isArchived = project.isArchived();
            dto.risk = project.getRiskPercentage().multiply(BigDecimal.valueOf(100));
            dto.projectionTimespan = project.getProjectionTimespan();
            dto.defaultTeam = project.getDefaultTeam();
            dto.baselineDate = project.getBaselineDate().orElse(null);

            return dto;
        }
    }
}
