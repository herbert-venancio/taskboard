package objective.taskboard.jira;

import static java.lang.String.join;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.auth.authorizer.Permissions.PROJECT_ADMINISTRATION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.auth.authorizer.Authorizer;
import objective.taskboard.domain.ProjectFilterConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class AuthorizedProjectsServiceTest {

    private final static String PROJECT_REGULAR_1 = "PROJ2";
    private final static String PROJECT_REGULAR_2 = "PROJ3";
    private final static String PROJECT_WITHOUT_ACCESS = "PROJ4";
    private final static String PROJECT_INVALID = "PROJECTINVALID";
    private final static List<String> ALL_PROJECT_KEYS = asList(PROJECT_REGULAR_1, PROJECT_REGULAR_2, PROJECT_WITHOUT_ACCESS);

    private ProjectService projectService = mock(ProjectService.class);
    private Authorizer authorizer = mock(Authorizer.class);

    private AuthorizedProjectsService subject = new AuthorizedProjectsService(projectService, authorizer);
    private List<ProjectFilterConfiguration> allProjects;

    @Before
    public void setup() {
        allProjects = ALL_PROJECT_KEYS.stream()
                .map(name -> new ProjectFilterConfiguration(name, 1L))
                .peek(p -> when(projectService.getTaskboardProject(p.getProjectKey())).thenReturn(Optional.ofNullable(p)))
                .collect(toList());

        when(projectService.getTaskboardProjects()).thenReturn(allProjects);

        when(authorizer.getAllowedProjectsForPermissions(any())).thenReturn(asList(PROJECT_REGULAR_2, PROJECT_REGULAR_1));
        when(authorizer.getAllowedProjectsForPermissions()).thenReturn(asList(PROJECT_REGULAR_2, PROJECT_REGULAR_1));
    }

    @Test
    public void getTaskboardProjects_withPermissionsParam_showOnlyProjectsThatUserHasPermission_sortedByProjectKey() {
        List<ProjectFilterConfiguration> projects = subject.getTaskboardProjects(PROJECT_ADMINISTRATION);
        assertTrue(projects.stream().anyMatch(p -> p.getProjectKey().equals(PROJECT_REGULAR_1)));
        assertTrue(projects.stream().anyMatch(p -> p.getProjectKey().equals(PROJECT_REGULAR_2)));
        assertFalse(projects.stream().anyMatch(p -> p.getProjectKey().equals(PROJECT_WITHOUT_ACCESS)));
    }

    @Test
    public void getTaskboardProjects_withPermissionsAndFilterParams_showOnlyProjectsThatUserHasPermission_respectingTheFilter() {
        List<ProjectFilterConfiguration> projects = subject.getTaskboardProjects(pKey -> true, PROJECT_ADMINISTRATION);
        assertProjects(projects, PROJECT_REGULAR_1, PROJECT_REGULAR_2);

        projects = subject.getTaskboardProjects(pKey -> pKey.equals(PROJECT_REGULAR_2));
        assertProjects(projects, PROJECT_REGULAR_2);

        projects = subject.getTaskboardProjects(pKey -> pKey.equals(PROJECT_REGULAR_1), PROJECT_ADMINISTRATION);
        assertProjects(projects, PROJECT_REGULAR_1);
    }

    @Test
    public void getTaskboardProject_withPermissionsParam_ifProjectExistsAndUserHasPermission_returnValue() {
        Optional<ProjectFilterConfiguration> projectRegular1 = subject.getTaskboardProject(PROJECT_REGULAR_1, PROJECT_ADMINISTRATION);
        assertTrue(projectRegular1.isPresent());

        Optional<ProjectFilterConfiguration> projectRegular2 = subject.getTaskboardProject(PROJECT_REGULAR_2, PROJECT_ADMINISTRATION);
        assertTrue(projectRegular2.isPresent());

        Optional<ProjectFilterConfiguration> projectWithoutAccess = subject.getTaskboardProject(PROJECT_WITHOUT_ACCESS, PROJECT_ADMINISTRATION);
        assertFalse(projectWithoutAccess.isPresent());

        Optional<ProjectFilterConfiguration> projectInvalid = subject.getTaskboardProject(PROJECT_INVALID, PROJECT_ADMINISTRATION);
        assertFalse(projectInvalid.isPresent());
    }

    private void assertProjects(List<ProjectFilterConfiguration> projects, String... expectedProjectKeys) {
        String actual = projects.stream()
                .map(p -> p.getProjectKey())
                .collect(joining("\n"));
        assertEquals(join("\n", expectedProjectKeys), actual);
    }

}