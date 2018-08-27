package objective.taskboard.project.config;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

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
import objective.taskboard.project.ProjectProfileItem;
import objective.taskboard.project.ProjectProfileItemRepository;
import objective.taskboard.repository.PermissionRepository;

@RestController
@RequestMapping("/ws/project/config/project-profile/")
public class ProjectProfileConfigurationController {
    
    private final ProjectService projectService;
    private final ProjectProfileItemRepository itemRepository;

    @Autowired
    public ProjectProfileConfigurationController(ProjectService projectService, ProjectProfileItemRepository itemRepository) {
        this.projectService = projectService;
        this.itemRepository = itemRepository;
    }

    @GetMapping("{projectKey}/data")
    public ResponseEntity<?> getData(@PathVariable("projectKey") String projectKey) {
        Optional<ProjectFilterConfiguration> project = projectService.getTaskboardProject(projectKey, PermissionRepository.ADMINISTRATIVE);

        if (!project.isPresent())
            return ResponseEntity.notFound().build();

        List<ProjectProfileItem> projectProfile = projectService.getProjectProfile(projectKey);

        List<ProjectProfileItemDto> items = projectProfile.stream().map(ProjectProfileItemDto::new).collect(toList());

        return ResponseEntity.ok(items);
    }
    
    @PutMapping("{projectKey}/items")
    @Transactional
    public ResponseEntity<?> updateItems(@PathVariable("projectKey") String projectKey, @RequestBody List<ProjectProfileItemDto> itemsDto) {
        Optional<ProjectFilterConfiguration> project = projectService.getTaskboardProject(projectKey, PermissionRepository.ADMINISTRATIVE);
        
        if (!project.isPresent())
            return ResponseEntity.notFound().build();
        
        List<ProjectProfileItem> existingItems = itemRepository.listByProject(project.get());

        addNewItems(itemsDto, project);
        updateExistingItems(itemsDto, existingItems);
        deleteMissingItems(itemsDto, existingItems);

        return ResponseEntity.ok().build();
    }

    private void addNewItems(List<ProjectProfileItemDto> itemsDto, Optional<ProjectFilterConfiguration> project) {
        itemsDto.stream()
            .filter(dto -> dto.id == null)
            .map(dto -> new ProjectProfileItem(project.get(), dto.roleName, dto.peopleCount, dto.allocationStart, dto.allocationEnd))
            .forEach(i -> itemRepository.add(i));
    }

    private void updateExistingItems(List<ProjectProfileItemDto> itemsDto, List<ProjectProfileItem> existingItems) {
        Map<Long, ProjectProfileItem> existingItemsById = existingItems.stream().collect(Collectors.toMap(i -> i.getId(), i -> i));
        
        itemsDto.stream()
            .filter(dto -> dto.id != null && existingItemsById.keySet().contains(dto.id))
            .forEach(dto -> {
                ProjectProfileItem item = existingItemsById.get(dto.id);
                
                item.setRoleName(dto.roleName);
                item.setPeopleCount(dto.peopleCount);
                item.setAllocationPeriod(dto.allocationStart, dto.allocationEnd);
            });
    }

    private void deleteMissingItems(List<ProjectProfileItemDto> itemsDto, List<ProjectProfileItem> existingItems) {
        Set<Long> updatedIds = itemsDto.stream().filter(dto -> dto.id != null).map(dto -> dto.id).collect(toSet());
        
        existingItems.stream()
                .filter(i -> !updatedIds.contains(i.getId()))
                .forEach(i -> itemRepository.remove(i));
    }

    public static class ProjectProfileItemDto {
        public Long id;
        public String roleName;
        public Double peopleCount;
        public LocalDate allocationStart;
        public LocalDate allocationEnd;
        
        public ProjectProfileItemDto() {}
        
        public ProjectProfileItemDto(ProjectProfileItem item) {
            this.id = item.getId();
            this.roleName = item.getRoleName();
            this.peopleCount = item.getPeopleCount();
            this.allocationStart = item.getAllocationStart();
            this.allocationEnd = item.getAllocationEnd();
        }
    }
}
