package objective.taskboard.project.config;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static objective.taskboard.repository.PermissionRepository.ADMINISTRATIVE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

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
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.project.ProjectDefaultTeamByIssueType;
import objective.taskboard.team.UserTeamService;
import objective.taskboard.utils.NameableDto;

@RestController
@RequestMapping("/ws/project/{projectKey}/default-teams")
public class ProjectDefaultTeamsController {

    private final ProjectService projectService;
    private final MetadataService metaDataService;
    private final UserTeamService userTeamService;

    @Autowired
    public ProjectDefaultTeamsController(
            ProjectService projectService,
            MetadataService metaDataService,
            UserTeamService userTeamService) {
        this.projectService = projectService;
        this.metaDataService = metaDataService;
        this.userTeamService = userTeamService;
    }

    @GetMapping
    public ResponseEntity<?> getDefaultTeams(@PathVariable("projectKey") String projectKey) {
        Optional<ProjectFilterConfiguration> projectOpt = projectService.getTaskboardProject(projectKey, ADMINISTRATIVE);
        if (!projectOpt.isPresent())
            return new ResponseEntity<>("Project \""+ projectKey +"\" not found.", NOT_FOUND);

        ProjectFilterConfiguration project = projectOpt.get();

        List<ProjectTeamByIssueTypeDto> projectDefaultTeams = getDefaultTeamsSortedAndFilteredByIssueType(project);
        List<NameableDto<Long>> teams = getTeamsSortedByNameAsLoggedInUser();
        List<NameableDto<Long>> issueTypes = getIssueTypesSortedByNameAsLoggedInUser();
        return ResponseEntity.ok(new ProjectDefaultTeamsDto(project.getDefaultTeam(), projectDefaultTeams, teams, issueTypes));
    }

    private List<ProjectTeamByIssueTypeDto> getDefaultTeamsSortedAndFilteredByIssueType(ProjectFilterConfiguration project) {
        Map<Long, JiraIssueTypeDto> issueTypeMetadata = metaDataService.getIssueTypeMetadataAsLoggedInUser();

        return project.getTeamsByIssueTypes().stream()
                .filter(i -> isIssueTypeVisibleToUser(i.getIssueTypeId()))
                .map(i -> new ProjectTeamByIssueTypeDto(i, !isTeamVisibleToUser(i.getTeamId())))
                .sorted((a,b) -> issueTypeMetadata.get(a.issueTypeId).getName().compareTo(issueTypeMetadata.get(b.issueTypeId).getName()))
                .collect(toList());
    }

    private List<NameableDto<Long>> getTeamsSortedByNameAsLoggedInUser() {
        return userTeamService.getTeamsVisibleToLoggedInUser().stream()
                .map(team -> new NameableDto<Long>(team.getId(), team.getName()))
                .sorted(Comparator.comparing(dto -> dto.name))
                .collect(toList());
    }

    private List<NameableDto<Long>> getIssueTypesSortedByNameAsLoggedInUser() {
        return metaDataService.getIssueTypeMetadataAsLoggedInUser().values().stream()
                .map(issueType -> new NameableDto<Long>(issueType.getId(), issueType.getName()))
                .sorted(Comparator.comparing(dto -> dto.name))
                .collect(toList());
    }

    @PutMapping
    @Transactional
    public ResponseEntity<?> updateDefaultTeams(@PathVariable("projectKey") String projectKey, @RequestBody ProjectDefaultTeamsUpdateDto teamsUpdateDto) {
        Optional<ProjectFilterConfiguration> projectOpt = projectService.getTaskboardProject(projectKey, ADMINISTRATIVE);
        if (!projectOpt.isPresent())
            return new ResponseEntity<>("Project \""+ projectKey +"\" not found.", NOT_FOUND);

        if (teamsUpdateDto.defaultTeamId == null)
            return new ResponseEntity<>("\"defaultTeamId\" is required.", BAD_REQUEST);

        ProjectFilterConfiguration project = projectOpt.get();

        List<String> errors = validateIssueTypes(teamsUpdateDto, project);
        errors.addAll(validateTeams(teamsUpdateDto, project));

        if (!errors.isEmpty())
            return new ResponseEntity<>(errors, BAD_REQUEST);

        project.setDefaultTeam(teamsUpdateDto.defaultTeamId);
        deleteMissingTeamsByIssueType(teamsUpdateDto.defaultTeamsByIssueType, project);
        updateExistingTeamsByIssueType(teamsUpdateDto.defaultTeamsByIssueType, project);
        addNewTeamsByIssueType(teamsUpdateDto.defaultTeamsByIssueType, project);

        projectService.saveTaskboardProject(project);

        return ResponseEntity.ok().build();
    }

    private List<String> validateIssueTypes(ProjectDefaultTeamsUpdateDto defaultTeamsUpdateDto, ProjectFilterConfiguration project) {
        List<Long> currentIssueTypes = project.getTeamsByIssueTypes().stream().map(i -> i.getIssueTypeId()).collect(toList());

        List<String> issueTypesErrors = new ArrayList<>();
        Set<Long> issueTypesUnique = new HashSet<>();

        defaultTeamsUpdateDto.defaultTeamsByIssueType.forEach(teamByIssueType -> {
                if (!isIssueTypeVisibleToUser(teamByIssueType.issueTypeId) && !currentIssueTypes.contains(teamByIssueType.issueTypeId))
                    issueTypesErrors.add("Issue Type with id \""+ teamByIssueType.issueTypeId +"\" doesn't exists.");
                else if (!issueTypesUnique.add(teamByIssueType.issueTypeId))
                    issueTypesErrors.add("Issue Type \"" + metaDataService.getIssueTypeByIdAsLoggedInUser(teamByIssueType.issueTypeId).getName() + "\" repeated.");
            });

        return issueTypesErrors.stream()
                .distinct()
                .sorted()
                .collect(toList());
    }

    private List<String> validateTeams(ProjectDefaultTeamsUpdateDto defaultTeamsUpdateDto, ProjectFilterConfiguration project) {
        List<Long> currentTeams = project.getTeamsByIssueTypes().stream().map(i -> i.getTeamId()).collect(toList());

        Set<String> teamsErrors = defaultTeamsUpdateDto.defaultTeamsByIssueType.stream()
                .filter(i -> !isTeamVisibleToUser(i.teamId) && !currentTeams.contains(i.teamId))
                .map(i -> "Team with id \""+ i.teamId +"\" doesn't exists.")
                .collect(toSet());

        if (!isTeamVisibleToUser(defaultTeamsUpdateDto.defaultTeamId) && !project.getDefaultTeam().equals(defaultTeamsUpdateDto.defaultTeamId))
            teamsErrors.add("Default Team with id \""+ defaultTeamsUpdateDto.defaultTeamId +"\" doesn't exists.");

        return Stream.of(teamsErrors)
                .flatMap(i -> i.stream())
                .sorted().collect(toList());
    }

    private void deleteMissingTeamsByIssueType(List<ProjectTeamByIssueTypeDto> itemsDto, ProjectFilterConfiguration project) {
        Set<Long> updatedIds = itemsDto.stream().filter(dto -> dto.id != null).map(dto -> dto.id).collect(toSet());

        project.getTeamsByIssueTypes().stream()
                .filter(i -> !updatedIds.contains(i.getId()) && userHasAccessToEdit(i))
                .collect(toList())
                .forEach(i -> project.removeDefaultTeamForIssueType(i));
    }

    private void updateExistingTeamsByIssueType(List<ProjectTeamByIssueTypeDto> itemsDto, ProjectFilterConfiguration project) {
        Map<Long, ProjectDefaultTeamByIssueType> currentItemsById = project.getTeamsByIssueTypes().stream().collect(toMap(i -> i.getId(), i -> i));

        itemsDto.stream()
            .filter(dto -> dto.id != null && currentItemsById.containsKey(dto.id))
            .forEach(dto -> {
                ProjectDefaultTeamByIssueType item = currentItemsById.get(dto.id);
                item.setIssueTypeId(dto.issueTypeId);
                item.setTeamId(dto.teamId);
            });
    }

    private void addNewTeamsByIssueType(List<ProjectTeamByIssueTypeDto> itemsDto, ProjectFilterConfiguration project) {
        itemsDto.stream()
            .filter(dto -> dto.id == null)
            .forEach(dto -> project.addProjectTeamForIssueType(dto.teamId, dto.issueTypeId));
    }

    private boolean userHasAccessToEdit(ProjectDefaultTeamByIssueType item) {
        return isTeamVisibleToUser(item.getTeamId()) && isIssueTypeVisibleToUser(item.getIssueTypeId());
    }

    private boolean isTeamVisibleToUser(Long teamId) {
        return userTeamService.getTeamVisibleToLoggedInUserById(teamId).isPresent();
    }

    private boolean isIssueTypeVisibleToUser(Long issueTypeId) {
        return metaDataService.issueTypeExistsByIdAsLoggedInUser(issueTypeId);
    }

    static class ProjectDefaultTeamsDto {
        public Long defaultTeamId;
        public List<ProjectTeamByIssueTypeDto> defaultTeamsByIssueType;
        public List<NameableDto<Long>> teams;
        public List<NameableDto<Long>> issueTypes;

        public ProjectDefaultTeamsDto(Long defaultTeamId, List<ProjectTeamByIssueTypeDto> defaultTeamsByIssueType,
                List<NameableDto<Long>> teams, List<NameableDto<Long>> issueTypes) {
            this.defaultTeamId = defaultTeamId;
            this.defaultTeamsByIssueType = defaultTeamsByIssueType;
            this.teams = teams;
            this.issueTypes = issueTypes;
        }
    }

    static class ProjectDefaultTeamsUpdateDto {
        public Long defaultTeamId;
        public List<ProjectTeamByIssueTypeDto> defaultTeamsByIssueType = new ArrayList<>();
    }

    static class ProjectTeamByIssueTypeDto {
        public Long id;
        public Long issueTypeId;
        public Long teamId;
        public Boolean isDisabled;

        public ProjectTeamByIssueTypeDto() {}
        public ProjectTeamByIssueTypeDto(ProjectDefaultTeamByIssueType item, boolean isDisabled) {
            this.id = item.getId();
            this.issueTypeId = item.getIssueTypeId();
            this.teamId = item.getTeamId();
            this.isDisabled = isDisabled;
        }
    }

}
