package objective.taskboard.project.config;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.repository.PermissionRepository;

@RestController
@RequestMapping("/ws/project/config/cluster/")
public class ProjectClusterController {

    private final ProjectService projectService;
    private final ProjectClusterService projectClusterService;

    @Autowired
    public ProjectClusterController(ProjectService projectService, ProjectClusterService projectClusterService) {
        this.projectService = projectService;
        this.projectClusterService = projectClusterService;
    }

    @GetMapping("{projectKey}")
    public ResponseEntity<List<ProjectClusterItemDto>> get(@PathVariable("projectKey") String projectKey) {
        Optional<ProjectFilterConfiguration> project = projectService.getTaskboardProject(projectKey, PermissionRepository.ADMINISTRATIVE);

        if (!project.isPresent())
            return ResponseEntity.notFound().build();

        return ResponseEntity.ok(projectClusterService.getItems(project.get()));
    }

    @PutMapping("{projectKey}")
    public ResponseEntity<?> update(@PathVariable("projectKey") String projectKey, @RequestBody List<ProjectClusterItemDto> clusterItemDtos) {
        Optional<ProjectFilterConfiguration> project = projectService.getTaskboardProject(projectKey, PermissionRepository.ADMINISTRATIVE);

        if (!project.isPresent())
            return ResponseEntity.notFound().build();

        projectClusterService.updateItems(project.get(), clusterItemDtos);

        return ResponseEntity.ok().build();
    }

}
