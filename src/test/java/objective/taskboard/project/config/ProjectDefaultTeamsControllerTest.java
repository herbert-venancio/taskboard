package objective.taskboard.project.config;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_ADMINISTRATION;
import static org.assertj.core.util.Lists.emptyList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import objective.taskboard.data.Team;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.AuthorizedProjectsService;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.project.ProjectDefaultTeamByIssueType;
import objective.taskboard.project.config.ProjectDefaultTeamsController.ProjectTeamByIssueTypeDto;
import objective.taskboard.project.config.ProjectDefaultTeamsController.ProjectDefaultTeamsDto;
import objective.taskboard.project.config.ProjectDefaultTeamsController.ProjectDefaultTeamsUpdateDto;
import objective.taskboard.team.UserTeamPermissionService;
import objective.taskboard.team.UserTeamService;

@RunWith(MockitoJUnitRunner.class)
public class ProjectDefaultTeamsControllerTest {

    private static final Long ID1 = 1L;
    private static final Long ID2 = 2L;
    private static final Long ID3 = 3L;
    private static final Long ID_NOT_REGISTERED = 4L;

    private static final Long INEXISTENT_ID1 = 5L;
    private static final Long INEXISTENT_ID2 = 6L;

    private static final String PROJECT_KEY = "PROJECT_TEST";
    private static final String INEXISTENT_PROJECT_KEY = "INEXISTENT_PROJECT";

    private ProjectDefaultTeamByIssueType PROJECT_TEAM_BY_ISSUE_TYPE_1 = mock(ProjectDefaultTeamByIssueType.class);
    private ProjectDefaultTeamByIssueType PROJECT_TEAM_BY_ISSUE_TYPE_2 = mock(ProjectDefaultTeamByIssueType.class);
    private ProjectDefaultTeamByIssueType PROJECT_TEAM_BY_ISSUE_TYPE_3 = mock(ProjectDefaultTeamByIssueType.class);

    private ProjectFilterConfiguration PROJECT_MOCK = mock(ProjectFilterConfiguration.class);
    private ProjectFilterConfiguration INEXISTENT_PROJECT = null;

    private ProjectService projectService = mock(ProjectService.class);
    private AuthorizedProjectsService authorizedProjectsService = mock(AuthorizedProjectsService.class);
    private MetadataService metaDataService = mock(MetadataService.class);
    private UserTeamService userTeamService = mock(UserTeamService.class);
    private UserTeamPermissionService userTeamPermissionService = mock(UserTeamPermissionService.class);

    private ProjectDefaultTeamsController subject;

    @Before
    public void setup() {
        subject = new ProjectDefaultTeamsController(projectService, authorizedProjectsService, metaDataService, userTeamService, userTeamPermissionService);
    }

    @Test
    public void giverCorrectProjectKey_whenTriesToGetData_returnOkWithOrderedValues_withItemsOrderedByIssueTypeName() {
        setupDefaultMocks();

        ResponseEntity<?> response = subject.getDefaultTeams(PROJECT_KEY);
        assertDataEquals(response,
                 "defaultTeamId: 1\n"
                +"items:\n"
                    +"-[id=1, issueTypeId=1, teamId=1],\n"
                    +"-[id=2, issueTypeId=2, teamId=2],\n"
                    +"-[id=3, issueTypeId=3, teamId=3]\n"
                +"teams:\n"
                    +"-[id: 1, name: Team Name1],\n"
                    +"-[id: 2, name: Team Name2],\n"
                    +"-[id: 3, name: Team Name3],\n"
                    +"-[id: 4, name: Team Name4]\n"
                +"issueTypes:\n"
                    +"-[id: 1, name: IT Name 1],\n"
                    +"-[id: 2, name: IT Name 2],\n"
                    +"-[id: 3, name: IT Name 3],\n"
                    +"-[id: 4, name: IT Name 4]");
    }

    @Test
    public void givenIssueTypeThatUserDoesntHavePermissionToEdit_whenTriesToGetData_returnOkWithOrderedValues_withoutThatIssueType() {
        // ID1 removed from issue types visible to user
        setupMetaDataService(ID_NOT_REGISTERED, ID2, ID3);
        setupTeamRepo(ID_NOT_REGISTERED, ID2, ID3, ID1);
        setupProjectMock(PROJECT_KEY, defaultItemsListNonOrdered());

        ResponseEntity<?> response = subject.getDefaultTeams(PROJECT_KEY);
        assertDataEquals(response,
                 "defaultTeamId: 1\n"
                +"items:\n"
                    +"-[id=2, issueTypeId=2, teamId=2],\n"
                    +"-[id=3, issueTypeId=3, teamId=3]\n"
                +"teams:\n"
                    +"-[id: 1, name: Team Name1],\n"
                    +"-[id: 2, name: Team Name2],\n"
                    +"-[id: 3, name: Team Name3],\n"
                    +"-[id: 4, name: Team Name4]\n"
                +"issueTypes:\n"
                    +"-[id: 2, name: IT Name 2],\n"
                    +"-[id: 3, name: IT Name 3],\n"
                    +"-[id: 4, name: IT Name 4]");
    }

    @Test
    public void givenInvalidProjectOrUserWithoutPermission_whenTriesToGetData_returnErrorProjectNotFound() {
        setupDefaultMocks();

        ResponseEntity<?> response = subject.getDefaultTeams(INEXISTENT_PROJECT_KEY);
        assertResponse(NOT_FOUND, "Project \""+ INEXISTENT_PROJECT_KEY +"\" not found.", response);
    }

    @Test
    public void givenCorrectValues_whenTriesToUpdate_returnOk() {
        setupDefaultMocks();

        ProjectTeamByIssueTypeDto expectedToBeRegistered = createDto(null, ID_NOT_REGISTERED, ID_NOT_REGISTERED);

        ProjectDefaultTeamsUpdateDto updateDto = defaultUpdateDto();
        updateDto.defaultTeamsByIssueType.removeIf(item -> item.id == PROJECT_TEAM_BY_ISSUE_TYPE_1.getId());
        updateDto.defaultTeamsByIssueType.add(expectedToBeRegistered);

        ResponseEntity<?> response = subject.updateDefaultTeams(PROJECT_KEY, updateDto);

        assertResponse(OK, response);
        assertAdded(1, 0, expectedToBeRegistered);
        assertUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_2, ID2, ID2);
        assertUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_3, ID3, ID3);
        assertRemoved(1, PROJECT_TEAM_BY_ISSUE_TYPE_1);
    }

    @Test
    public void givenInvalidProjectOrUserWithoutPermission_whenTriesToUpdate_returnErrorProjectNotFound() {
        setupDefaultMocks();

        ResponseEntity<?> response = subject.updateDefaultTeams(INEXISTENT_PROJECT_KEY, defaultUpdateDto());
        assertResponse(NOT_FOUND, "Project \""+ INEXISTENT_PROJECT_KEY +"\" not found.", response);
    }

    @Test
    public void givenUpdateDtoWithoutDefaultTeamId_whenTriesToUpdate_returnErrorDefaultTeamIdIsRequired() {
        setupDefaultMocks();

        ProjectDefaultTeamsUpdateDto updateDto = defaultUpdateDto();
        updateDto.defaultTeamId = null;

        ResponseEntity<?> response = subject.updateDefaultTeams(PROJECT_KEY, updateDto);
        assertResponse(BAD_REQUEST, "\"defaultTeamId\" is required.", response);
    }

    @Test
    public void givenNewValuesToAnEmptyProject_whenTriesToUpdate_dontRemove_addAll() {
        setupMetaDataService(ID_NOT_REGISTERED, ID2, ID3, ID1);
        setupTeamRepo(ID_NOT_REGISTERED, ID2, ID3, ID1);
        setupProjectMock(PROJECT_KEY, emptyList());

        ProjectDefaultTeamsUpdateDto updateDto = defaultUpdateDto();
        updateDto.defaultTeamsByIssueType = asList(
                createDto(null, ID1, ID1),
                createDto(null, ID2, ID2),
                createDto(null, ID3, ID3)
                );

        ResponseEntity<?> response = subject.updateDefaultTeams(PROJECT_KEY, updateDto);

        assertResponse(OK, response);
        verify(PROJECT_MOCK, times(0)).removeDefaultTeamForIssueType(any());
        assertNotUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_1);
        assertNotUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_2);
        assertNotUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_3);
        assertAdded(3, 0, updateDto.defaultTeamsByIssueType.get(0));
        assertAdded(3, 1, updateDto.defaultTeamsByIssueType.get(1));
        assertAdded(3, 2, updateDto.defaultTeamsByIssueType.get(2));
    }

    @Test
    public void givenCurrentValues_whenTriesToUpdate_dontAddOrRemove_updateAll() {
        setupDefaultMocks();

        ResponseEntity<?> response = subject.updateDefaultTeams(PROJECT_KEY, defaultUpdateDto());

        assertResponse(OK, response);
        verify(PROJECT_MOCK, times(0)).addProjectTeamForIssueType(any(), any());
        verify(PROJECT_MOCK, times(0)).removeDefaultTeamForIssueType(any());
        assertUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_1, ID1, ID1);
        assertUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_2, ID2, ID2);
        assertUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_3, ID3, ID3);
    }

    @Test
    public void givenEmptyList_whenTriesToUpdate_dontAddOrUpdate_removeAll() {
        setupDefaultMocks();

        ProjectDefaultTeamsUpdateDto updateDto = defaultUpdateDto();
        updateDto.defaultTeamsByIssueType = emptyList();

        ResponseEntity<?> response = subject.updateDefaultTeams(PROJECT_KEY, updateDto);
        assertResponse(OK, response);

        verify(PROJECT_MOCK, times(0)).addProjectTeamForIssueType(any(), any());
        assertNotUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_1);
        assertNotUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_2);
        assertNotUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_3);
        assertRemoved(3, PROJECT_TEAM_BY_ISSUE_TYPE_1);
        assertRemoved(3, PROJECT_TEAM_BY_ISSUE_TYPE_2);
        assertRemoved(3, PROJECT_TEAM_BY_ISSUE_TYPE_3);
    }

    @Test
    public void givenInexistentValues_whenTriesToUpdate_returnDistinctErrorList() {
        setupDefaultMocks();

        ProjectDefaultTeamsUpdateDto updateDto = defaultUpdateDto();
        updateDto.defaultTeamId = INEXISTENT_ID1;
        updateDto.defaultTeamsByIssueType.add(createDto(null, INEXISTENT_ID1, INEXISTENT_ID1));
        updateDto.defaultTeamsByIssueType.add(createDto(null, INEXISTENT_ID2, INEXISTENT_ID2));
        updateDto.defaultTeamsByIssueType.add(createDto(INEXISTENT_ID1, INEXISTENT_ID1, INEXISTENT_ID1));
        updateDto.defaultTeamsByIssueType.add(createDto(INEXISTENT_ID2, INEXISTENT_ID2, INEXISTENT_ID2));

        ResponseEntity<?> response = subject.updateDefaultTeams(PROJECT_KEY, updateDto);

        assertResponse(BAD_REQUEST,
               "[Issue Type with id \""+ INEXISTENT_ID1 +"\" doesn't exists., "
              + "Issue Type with id \""+ INEXISTENT_ID2 +"\" doesn't exists., "
              + "Default Team with id \""+ INEXISTENT_ID1 +"\" doesn't exists., "
              + "Team with id \""+ INEXISTENT_ID1 +"\" doesn't exists., "
              + "Team with id \""+ INEXISTENT_ID2 +"\" doesn't exists.]",
              response);
    }

    @Test
    public void givenRepeatedValues_whenTriesToUpdate_returnDistinctErrorList() {
        setupDefaultMocks();

        ProjectDefaultTeamsUpdateDto updateDto = defaultUpdateDto();
        updateDto.defaultTeamsByIssueType.add(createDto(null, ID1, ID1));
        updateDto.defaultTeamsByIssueType.add(createDto(null, ID2, ID2));
        updateDto.defaultTeamsByIssueType.add(createDto(ID1, ID1, ID1));
        updateDto.defaultTeamsByIssueType.add(createDto(ID2, ID2, ID2));

        ResponseEntity<?> response = subject.updateDefaultTeams(PROJECT_KEY, updateDto);

        assertResponse(BAD_REQUEST,
                 "[Issue Type \"IT Name 1\" repeated., "
                + "Issue Type \"IT Name 2\" repeated.]",
                response);
    }

    @Test
    public void givenTeamThatUserDoesntHavePermissionToEdit_whenTriesToUpdate_dontAddUpdateOrRemoveThatTeam() {
        // ID1/PROJECT_TEAM_BY_ISSUE_TYPE_1 removed from teams visible to user and updateDto.items

        setupMetaDataService(ID_NOT_REGISTERED, ID2, ID3, ID1);
        setupTeamRepo(ID_NOT_REGISTERED, ID2, ID3);
        setupProjectMock(PROJECT_KEY, defaultItemsListNonOrdered());

        ProjectTeamByIssueTypeDto expectedToBeRegistered = createDto(null, ID_NOT_REGISTERED, ID_NOT_REGISTERED);

        ProjectDefaultTeamsUpdateDto updateDto = defaultUpdateDto();
        updateDto.defaultTeamId = ID2;
        updateDto.defaultTeamsByIssueType.removeIf(item -> item.id == PROJECT_TEAM_BY_ISSUE_TYPE_1.getId());
        updateDto.defaultTeamsByIssueType.add(expectedToBeRegistered);

        ResponseEntity<?> response = subject.updateDefaultTeams(PROJECT_KEY, updateDto);

        assertResponse(OK, response);
        assertAdded(1, 0, expectedToBeRegistered);
        assertUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_2, ID2, ID2);
        assertUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_3, ID3, ID3);
        assertNotUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_1);
    }

    @Test
    public void givenIssueTypeThatUserDoesntHavePermissionToEdit_whenTriesToUpdate_dontAddUpdateOrRemoveThatIssueType() {
        // ID1/PROJECT_TEAM_BY_ISSUE_TYPE_1 removed from issue types visible to user and updateDto.items

        setupMetaDataService(ID_NOT_REGISTERED, ID2, ID3);
        setupTeamRepo(ID_NOT_REGISTERED, ID2, ID3, ID1);
        setupProjectMock(PROJECT_KEY, defaultItemsListNonOrdered());

        ProjectTeamByIssueTypeDto expectedToBeRegistered = createDto(null, ID_NOT_REGISTERED, ID_NOT_REGISTERED);

        ProjectDefaultTeamsUpdateDto updateDto = defaultUpdateDto();
        updateDto.defaultTeamsByIssueType.removeIf(item -> item.id == PROJECT_TEAM_BY_ISSUE_TYPE_1.getId());
        updateDto.defaultTeamsByIssueType.add(expectedToBeRegistered);

        ResponseEntity<?> response = subject.updateDefaultTeams(PROJECT_KEY, updateDto);

        assertResponse(OK, response);
        assertAdded(1, 0, expectedToBeRegistered);
        assertUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_2, ID2, ID2);
        assertUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_3, ID3, ID3);
        assertNotUpdated(PROJECT_TEAM_BY_ISSUE_TYPE_1);
    }

    private void assertResponse(HttpStatus expectedStatus, ResponseEntity<?> response) {
        assertEquals(expectedStatus.value(), response.getStatusCodeValue());
        assertEquals(null, response.getBody());
    }

    private void assertResponse(HttpStatus expectedStatus, String expectedBodyString, ResponseEntity<?> response) {
        assertEquals(expectedStatus.value(), response.getStatusCodeValue());
        assertEquals(expectedBodyString, response.getBody().toString());
    }

    private void assertAdded(int addedCount, int addedIndex, ProjectTeamByIssueTypeDto dto) {
        ArgumentCaptor<Long> teamArgument = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> issueTypeArgument = ArgumentCaptor.forClass(Long.class);
        verify(PROJECT_MOCK, times(addedCount)).addProjectTeamForIssueType(teamArgument.capture(), issueTypeArgument.capture());
        assertEquals(dto.issueTypeId, teamArgument.getAllValues().get(addedIndex));
        assertEquals(dto.teamId, teamArgument.getAllValues().get(addedIndex));
    }

    private void assertUpdated(ProjectDefaultTeamByIssueType item, Long issueTypeId, Long teamId) {
        verify(item, times(1)).setIssueTypeId(eq(issueTypeId));
        verify(item, times(1)).setTeamId(eq(userTeamService.getTeamVisibleToLoggedInUserByIdOrCry(teamId).getId()));
    }

    private void assertNotUpdated(ProjectDefaultTeamByIssueType item) {
        verify(item, times(0)).setIssueTypeId(any());
        verify(item, times(0)).setTeamId(any());
    }

    private void assertRemoved(int totalRemovedCount, ProjectDefaultTeamByIssueType removedItem) {
        ArgumentCaptor<ProjectDefaultTeamByIssueType> argument = ArgumentCaptor.forClass(ProjectDefaultTeamByIssueType.class);
        verify(PROJECT_MOCK, times(totalRemovedCount)).removeDefaultTeamForIssueType(argument.capture());

        List<ProjectDefaultTeamByIssueType> removedAsList = argument.getAllValues().stream()
            .filter(argumentValue -> argumentValue.getId().equals(removedItem.getId()))
            .collect(toList());

        assertEquals(1, removedAsList.size());
    }

    private void assertDataEquals(ResponseEntity<?> response, String expected) {
        ProjectDefaultTeamsDto dataDto = (ProjectDefaultTeamsDto) response.getBody();
        assertEquals(expected, dataDtoToString(dataDto));
    }

    private String dataDtoToString(ProjectDefaultTeamsDto dataDto) {
        String dataString = "defaultTeamId: "+ dataDto.defaultTeamId +"\n";
        dataString += "items:\n-"+ dataDto.defaultTeamsByIssueType.stream()
                .map(i -> "[id=" + i.id + ", issueTypeId=" + i.issueTypeId + ", teamId=" + i.teamId + "]")
                .collect(joining(",\n-"))+"\n";
        dataString += "teams:\n-"+ dataDto.teams.stream()
                .map(i -> "[id: "+ i.id +", name: "+ i.name +"]")
                .collect(joining(",\n-"))+"\n";
        dataString += "issueTypes:\n-"+ dataDto.issueTypes.stream()
                .map(i -> "[id: "+ i.id +", name: "+ i.name +"]")
                .collect(joining(",\n-"));
        return dataString;
    }

    private List<ProjectDefaultTeamByIssueType> defaultItemsListNonOrdered() {
        when(PROJECT_TEAM_BY_ISSUE_TYPE_1.getId()).thenReturn(ID1);
        when(PROJECT_TEAM_BY_ISSUE_TYPE_1.getIssueTypeId()).thenReturn(ID1);
        when(PROJECT_TEAM_BY_ISSUE_TYPE_1.getTeamId()).thenReturn(ID1);
        when(PROJECT_TEAM_BY_ISSUE_TYPE_2.getId()).thenReturn(ID2);
        when(PROJECT_TEAM_BY_ISSUE_TYPE_2.getIssueTypeId()).thenReturn(ID2);
        when(PROJECT_TEAM_BY_ISSUE_TYPE_2.getTeamId()).thenReturn(ID2);
        when(PROJECT_TEAM_BY_ISSUE_TYPE_3.getId()).thenReturn(ID3);
        when(PROJECT_TEAM_BY_ISSUE_TYPE_3.getIssueTypeId()).thenReturn(ID3);
        when(PROJECT_TEAM_BY_ISSUE_TYPE_3.getTeamId()).thenReturn(ID3);
        return Stream.of(PROJECT_TEAM_BY_ISSUE_TYPE_2, PROJECT_TEAM_BY_ISSUE_TYPE_3, PROJECT_TEAM_BY_ISSUE_TYPE_1).collect(toList());
    }

    private ProjectDefaultTeamsUpdateDto defaultUpdateDto() {
        ProjectDefaultTeamsUpdateDto dto = new ProjectDefaultTeamsUpdateDto();
        dto.defaultTeamId = ID1;
        dto.defaultTeamsByIssueType = defaultItemsListNonOrdered().stream().map(item -> new ProjectTeamByIssueTypeDto(item, false)).collect(toList());
        return dto;
    }

    private static ProjectTeamByIssueTypeDto createDto(Long id, Long issueTypeId, Long teamId) {
        ProjectDefaultTeamByIssueType tempItem = mock(ProjectDefaultTeamByIssueType.class);
        when(tempItem.getId()).thenReturn(id);
        when(tempItem.getIssueTypeId()).thenReturn(issueTypeId);
        when(tempItem.getTeamId()).thenReturn(teamId);

        ProjectTeamByIssueTypeDto dto = new ProjectTeamByIssueTypeDto(tempItem, false);
        dto.id = id;
        dto.issueTypeId = issueTypeId;
        dto.teamId = teamId;
        return dto;
    }

    private static Team createTeam(Long id) {
        Team team = new Team("Team Name"+ id, "Manager "+ id, "Coach "+ id, asList("Member 1", "Member 2"));
        team.setId(id);
        return team;
    }

    private static JiraIssueTypeDto createIssueType(Long id) {
        return new JiraIssueTypeDto(id, "IT Name "+ id, false);
    }

    private void setupDefaultMocks() {
        setupMetaDataService(ID_NOT_REGISTERED, ID2, ID3, ID1);
        setupTeamRepo(ID_NOT_REGISTERED, ID2, ID3, ID1);
        setupProjectMock(PROJECT_KEY, defaultItemsListNonOrdered());
    }

    private void setupProjectMock(String projectKey, List<ProjectDefaultTeamByIssueType> projectTeamByIssueTypeList) {
        setupProject(projectKey, projectTeamByIssueTypeList);
        setupProjectService();
    }

    private void setupProject(String projectKey, List<ProjectDefaultTeamByIssueType> teamsByIssueTypes) {
        when(PROJECT_MOCK.getProjectKey()).thenReturn(projectKey);
        when(PROJECT_MOCK.getDefaultTeam()).thenReturn(ID1);
        when(PROJECT_MOCK.getTeamsByIssueTypes()).thenAnswer((i) -> teamsByIssueTypes);
    }

    private void setupProjectService() {
        when(authorizedProjectsService.getTaskboardProject(any(), eq(PROJECT_ADMINISTRATION))).thenReturn(Optional.ofNullable(INEXISTENT_PROJECT));
        when(authorizedProjectsService.getTaskboardProject(PROJECT_KEY, PROJECT_ADMINISTRATION)).thenReturn(Optional.ofNullable(PROJECT_MOCK));
    }

    private void setupMetaDataService(Long... idsToRegister) {
        final List<JiraIssueTypeDto> issueTypes = Stream.of(idsToRegister).map(id -> createIssueType(id)).collect(toList());

        when(metaDataService.getIssueTypeMetadataAsLoggedInUser()).thenReturn(issueTypes.stream().collect(toMap(JiraIssueTypeDto::getId, t -> t)));

        when(metaDataService.issueTypeExistsByIdAsLoggedInUser(any())).thenAnswer(invocation -> {
            Long id = (Long) invocation.getArguments()[0];
            return issueTypes.stream().anyMatch(it -> it.getId().equals(id));
        });

        when(metaDataService.getIssueTypeByIdAsLoggedInUser(any())).thenAnswer(invocation -> {
            Long id = (Long) invocation.getArguments()[0];
            return issueTypes.stream().filter(it -> it.getId().equals(id)).findFirst().orElse(null);
        });
    }

    private void setupTeamRepo(Long...idsToRegister) {
        final Set<Team> teams = Stream.of(idsToRegister)
                .map(id -> createTeam(id))
                .collect(toSet());

        when(userTeamPermissionService.getTeamsVisibleToLoggedInUser()).thenAnswer(i -> teams);

        when(userTeamService.getTeamVisibleToLoggedInUserById(any())).thenAnswer(invocation -> {
            Long id = (Long) invocation.getArguments()[0];
            return teams.stream().filter(t -> t.getId().equals(id)).findFirst();
        });

        when(userTeamService.getTeamVisibleToLoggedInUserByIdOrCry(any())).thenAnswer(invocation -> {
            Long id = (Long) invocation.getArguments()[0];
            return teams.stream().filter(t -> t.getId().equals(id)).findFirst().orElseThrow(IllegalStateException::new);
        });
    }

}
