package objective.taskboard.project.config.changeRequest;

import static objective.taskboard.auth.authorizer.Permissions.PROJECT_ADMINISTRATION;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.AuthorizedProjectsService;

@RestController
@RequestMapping("/ws/project/config/change-request")
class ChangeRequestController {

    private ChangeRequestService changeRequestService;
    private AuthorizedProjectsService authorizedProjectsService;

    @Autowired
    public ChangeRequestController(ChangeRequestService changeRequestService,
            AuthorizedProjectsService authorizedProjectsService) {
        this.changeRequestService = changeRequestService;
        this.authorizedProjectsService = authorizedProjectsService;
    }

    @GetMapping("{projectKey}")
    public ResponseEntity<?> getAll(@PathVariable("projectKey") String projectKey) {
        Optional<ProjectFilterConfiguration> project = authorizedProjectsService.getTaskboardProject(projectKey,
                PROJECT_ADMINISTRATION);

        if (!project.isPresent())
            return ResponseEntity.notFound().build();
        List<ChangeRequest> items = changeRequestService.listByProject(project.get());

        return ResponseEntity.ok(ChangeRequestDto.toDto(items));
    }

    @PutMapping("{projectKey}")
    public ResponseEntity<?> update(@PathVariable("projectKey") String projectKey,
            @RequestBody List<ChangeRequestDto> changeRequestsDtos) {
        Optional<ProjectFilterConfiguration> project = authorizedProjectsService.getTaskboardProject(projectKey,
                PROJECT_ADMINISTRATION);

        if (!project.isPresent())
            return ResponseEntity.notFound().build();

        changeRequestService.updateItems(project.get(), ChangeRequestDto.fromDto(changeRequestsDtos));

        return ResponseEntity.ok().build();
    }

    public static class ChangeRequestDto {
        public Long id;
        public String project;
        public String name;
        public LocalDate date;
        public int budgetIncrease;
        public boolean isBaseline;

        public static List<ChangeRequestDto> toDto(List<ChangeRequest> changeRequests) {
            return changeRequests.stream().map(cr -> from(cr)).collect(Collectors.toList());
        }

        public static List<ChangeRequest> fromDto(List<ChangeRequestDto> changeRequests) {
            return changeRequests.stream().map(cr -> from(cr)).collect(Collectors.toList());
        }

        public static ChangeRequestDto from(ChangeRequest changeRequest) {
            ChangeRequestDto changeRequestDto = new ChangeRequestDto();
            changeRequestDto.id = changeRequest.getId();
            changeRequestDto.name = changeRequest.getName();
            changeRequestDto.date = changeRequest.getDate();
            changeRequestDto.budgetIncrease = changeRequest.getBudgetIncrease();
            changeRequestDto.isBaseline = changeRequest.isBaseline();
            changeRequestDto.project = changeRequest.getProject().getProjectKey();

            return changeRequestDto;
        }

        public static ChangeRequest from(ChangeRequestDto changeRequestDto) {
            ChangeRequest changeRequest = new ChangeRequest();
            changeRequest.setId(changeRequestDto.id);
            changeRequest.setName(changeRequestDto.name);
            changeRequest.setDate(changeRequestDto.date);
            changeRequest.setBudgetIncrease(changeRequestDto.budgetIncrease);
            changeRequest.setIsBaseline(changeRequestDto.isBaseline);

            return changeRequest;
        }
    }
}
