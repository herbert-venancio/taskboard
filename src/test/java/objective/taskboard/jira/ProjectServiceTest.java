package objective.taskboard.jira;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationEventPublisher;

import objective.taskboard.domain.Project;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.jira.data.JiraProject;
import objective.taskboard.jira.data.Version;
import objective.taskboard.project.ProjectBaselineProvider;
import objective.taskboard.project.ProjectProfileItemRepository;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;

public class ProjectServiceTest {

    private final static String PROJECT_ARCHIVED = "PROJ1";
    private final static String PROJECT_REGULAR_1 = "PROJ2";
    private final static String PROJECT_REGULAR_2 = "PROJ3";
    private final static String PROJECT_WITHOUT_ACCESS = "PROJ4";
    private final static String PROJECT_WITHOUT_METADATA = "PROJ5";
    private final static String PROJECT_INVALID = "PROJECTINVALID";

    private ProjectFilterConfigurationCachedRepository projectRepository = mock(ProjectFilterConfigurationCachedRepository.class);
    private ProjectProfileItemRepository projectProfileItemRepository = mock(ProjectProfileItemRepository.class);
    private JiraProjectService jiraProjectService = mock(JiraProjectService.class);
    private ProjectBaselineProvider projectBaselineProvider = mock(ProjectBaselineProvider.class);
    private ApplicationEventPublisher eventPublisher = mock(ApplicationEventPublisher.class);;
    private ProjectService subject = new ProjectService(projectRepository, projectProfileItemRepository, jiraProjectService, projectBaselineProvider, eventPublisher);

    @Before
    public void setup() {
        ProjectFilterConfiguration taskb = new ProjectFilterConfiguration("TASKB", 1L);
        ProjectFilterConfiguration proj1 = new ProjectFilterConfiguration(PROJECT_ARCHIVED, 1L);
        proj1.setArchived(true);
        ProjectFilterConfiguration proj2 = new ProjectFilterConfiguration(PROJECT_REGULAR_1, 1L);
        proj2.setArchived(false);
        ProjectFilterConfiguration proj3 = new ProjectFilterConfiguration(PROJECT_REGULAR_2, 1L);
        proj3.setArchived(false);
        ProjectFilterConfiguration proj4 = new ProjectFilterConfiguration(PROJECT_WITHOUT_ACCESS, 1L);
        proj4.setArchived(false);
        ProjectFilterConfiguration proj5 = new ProjectFilterConfiguration(PROJECT_WITHOUT_METADATA, 1L);
        proj5.setArchived(false);

        List<ProjectFilterConfiguration> projectList = asList(taskb, proj1, proj2, proj3, proj4, proj5);
        Map<String, ProjectFilterConfiguration> projectsByKey = projectList.stream().collect(Collectors.toMap(p -> p.getProjectKey(), p -> p));

        when(projectRepository.getProjects()).thenReturn(projectList);
        when(projectRepository.exists(any())).thenAnswer(i -> projectsByKey.containsKey(i.getArgumentAt(0, String.class)));
        when(projectRepository.getProjectByKey(any())).thenAnswer(i -> Optional.ofNullable(projectsByKey.get(i.getArgumentAt(0, String.class))));
        when(projectRepository.getProjectByKeyOrCry(any())).thenAnswer(i -> projectsByKey.get(i.getArgumentAt(0, String.class)));

        when(jiraProjectService.getCreateIssueMetadata(any())).thenAnswer(i -> {
            String projectKey = i.getArgumentAt(0, String.class);

            if(PROJECT_WITHOUT_METADATA.equals(projectKey))
                return Optional.empty();

            JiraCreateIssue.ProjectMetadata projectMetadata = new JiraCreateIssue.ProjectMetadata();
            projectMetadata.key = projectKey;
            return Optional.of(projectMetadata);
        });
        
        List<Version> versions = asList(new Version("12549", "0.3"), new Version("12550", "1.0"),  new Version("12551", "2.0"));

        when(jiraProjectService.getUserProjectKeys()).thenReturn(asList(PROJECT_ARCHIVED, PROJECT_REGULAR_1, PROJECT_REGULAR_2, PROJECT_WITHOUT_METADATA));

        when(jiraProjectService.getAllProjects()).thenReturn(
                projectList.stream().map(p -> new JiraProject("1", p.getProjectKey(), versions, p.getProjectKey())).collect(toList()));

        when(projectBaselineProvider.getAvailableDates(any())).thenReturn(emptyList());
    }

    @Test
    public void getVersion() {
        Version expected = new Version("12550", "1.0");
        Version actual = subject.getVersion("12550");

        assertThat(actual).isEqualToComparingFieldByField(expected);
    }

    @Test
    public void cacheGetVersion() {
        Version version1 = subject.getVersion("12550");
        Version version2 = subject.getVersion("12550");

        assertTrue(version1 == version2);
    }

    @Test
    public void getNonArchivedJiraProjectsForUser_dontShowArchivedProjects_dontShowProjectsThatUserHasNoAccess() {
        List<Project> visibleProjectsOnTaskboard = subject.getNonArchivedJiraProjectsForUser();
        assertTrue(visibleProjectsOnTaskboard.stream().anyMatch(p -> p.getKey().equals(PROJECT_REGULAR_1)));
        assertTrue(visibleProjectsOnTaskboard.stream().anyMatch(p -> p.getKey().equals(PROJECT_REGULAR_2)));
        assertFalse(visibleProjectsOnTaskboard.stream().anyMatch(p -> p.getKey().equals(PROJECT_WITHOUT_ACCESS)));
        assertFalse(visibleProjectsOnTaskboard.stream().anyMatch(p -> p.getKey().equals(PROJECT_ARCHIVED)));
    }

    @Test
    public void getTaskboardProjects_withoutParams_showAllTaskboardProjects_sortedByProjectKey() {
        List<ProjectFilterConfiguration> projects = subject.getTaskboardProjects();
        assertTrue(projectRepository.getProjects().size() == projects.size());
        assertIsSortedByProjectkey(projects);
    }

    @Test
    public void getProjectMetadata_ifProjectIsNonArchivedAndUserHasAccessAndProjectHasMetadata_returnTheValue() {
        Optional<JiraCreateIssue.ProjectMetadata> metadataForRegular = subject.getProjectMetadata(PROJECT_REGULAR_1);
        assertTrue(metadataForRegular.isPresent());

        Optional<JiraCreateIssue.ProjectMetadata> metadataForWithoutAccess = subject.getProjectMetadata(PROJECT_WITHOUT_ACCESS);
        assertFalse(metadataForWithoutAccess.isPresent());

        Optional<JiraCreateIssue.ProjectMetadata> metadataForArchived = subject.getProjectMetadata(PROJECT_ARCHIVED);
        assertFalse(metadataForArchived.isPresent());

        Optional<JiraCreateIssue.ProjectMetadata> metadataForProjectWithoutMetadata = subject.getProjectMetadata(PROJECT_WITHOUT_METADATA);
        assertFalse(metadataForProjectWithoutMetadata.isPresent());
    }

    @Test
    public void isNonArchivedAndUserHasAccess_ifProjectIsArchivedOrUserHasNoAccess_returnFalse() {
        assertTrue(subject.isNonArchivedAndUserHasAccess(PROJECT_REGULAR_1));
        assertTrue(subject.isNonArchivedAndUserHasAccess(PROJECT_REGULAR_2));
        assertFalse(subject.isNonArchivedAndUserHasAccess(PROJECT_ARCHIVED));
        assertFalse(subject.isNonArchivedAndUserHasAccess(PROJECT_WITHOUT_ACCESS));
    }

    @Test
    public void jiraProjectExistsAndUserHasAccess_ifUserHasNoAccess_returnFalse_ifProjectIsArchived_returnTrue() {
        assertTrue(subject.jiraProjectExistsAndUserHasAccess(PROJECT_REGULAR_1));
        assertTrue(subject.jiraProjectExistsAndUserHasAccess(PROJECT_REGULAR_2));
        assertTrue(subject.jiraProjectExistsAndUserHasAccess(PROJECT_ARCHIVED));
        assertFalse(subject.jiraProjectExistsAndUserHasAccess(PROJECT_WITHOUT_ACCESS));
        assertFalse(subject.jiraProjectExistsAndUserHasAccess(PROJECT_INVALID));
    }

    @Test
    public void taskboardProjectExists_ifProjectExists_returnTrue() {
        assertTrue(subject.taskboardProjectExists(PROJECT_REGULAR_1));
        assertTrue(subject.taskboardProjectExists(PROJECT_REGULAR_2));
        assertTrue(subject.taskboardProjectExists(PROJECT_ARCHIVED));
        assertTrue(subject.taskboardProjectExists(PROJECT_WITHOUT_ACCESS));
        assertFalse(subject.taskboardProjectExists(PROJECT_INVALID));
    }

    @Test
    public void getTaskboardProject_withoutPermissionsParam_ifProjectExists_returnValue() {
        Optional<ProjectFilterConfiguration> projectRegular1 = subject.getTaskboardProject(PROJECT_REGULAR_1);
        assertTrue(projectRegular1.isPresent());

        Optional<ProjectFilterConfiguration> projectRegular2 = subject.getTaskboardProject(PROJECT_REGULAR_2);
        assertTrue(projectRegular2.isPresent());

        Optional<ProjectFilterConfiguration> projectArchived = subject.getTaskboardProject(PROJECT_ARCHIVED);
        assertTrue(projectArchived.isPresent());

        Optional<ProjectFilterConfiguration> projectWithoutAccess = subject.getTaskboardProject(PROJECT_WITHOUT_ACCESS);
        assertTrue(projectWithoutAccess.isPresent());

        Optional<ProjectFilterConfiguration> projectInvalid = subject.getTaskboardProject(PROJECT_INVALID);
        assertFalse(projectInvalid.isPresent());
    }

    @Test
    public void saveTaskboardProject() {
        ProjectFilterConfiguration newProject = new ProjectFilterConfiguration(PROJECT_INVALID, 1L);
        subject.saveTaskboardProject(newProject);

        verify(projectRepository, atLeast(1)).save(newProject);
    }

    private void assertIsSortedByProjectkey(List<ProjectFilterConfiguration> projects) {
        List<ProjectFilterConfiguration> sortedProjects = projects.stream()
                .sorted((p1, p2) -> p1.getProjectKey().compareTo(p2.getProjectKey()))
                .collect(toList());

        for (int i = 0; i < projects.size(); i++)
            assertThat(projects.get(i)).isEqualToComparingFieldByField(sortedProjects.get(i));
    }
}