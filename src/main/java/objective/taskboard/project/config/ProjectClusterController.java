package objective.taskboard.project.config;

import static java.util.stream.Collectors.toList;

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
import objective.taskboard.project.config.ProjectClusterService.ProjectClusterItem;
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

        List<ProjectClusterItemDto> itemDtos = projectClusterService.getItems(project.get()).stream()
                .map(item -> new ProjectClusterItemDto(item))
                .collect(toList());

        return ResponseEntity.ok(itemDtos);
    }

    @PutMapping("{projectKey}")
    public ResponseEntity<?> update(@PathVariable("projectKey") String projectKey, @RequestBody List<ProjectClusterItemDto> clusterItemDtos) {
        Optional<ProjectFilterConfiguration> project = projectService.getTaskboardProject(projectKey, PermissionRepository.ADMINISTRATIVE);

        if (!project.isPresent())
            return ResponseEntity.notFound().build();

        List<ProjectClusterItem> items = clusterItemDtos.stream()
            .map(itemDto -> itemDto.toObject())
            .collect(toList());

        projectClusterService.updateItems(project.get(), items);

        return ResponseEntity.ok().build();
    }

    protected static class ProjectClusterItemDto {
        public String issueType;
        public String sizing;
        public Double effort;
        public Double cycle;

        public ProjectClusterItemDto() {}

        public ProjectClusterItemDto(ProjectClusterItem object) {
            this.issueType = object.getIssueType();
            this.sizing = object.getSizing();
            this.effort = object.getEffort();
            this.cycle = object.getCycle();
        }

        public ProjectClusterItem toObject() {
            return new ProjectClusterItem(issueType, sizing, effort, cycle);
        }
    }

}
