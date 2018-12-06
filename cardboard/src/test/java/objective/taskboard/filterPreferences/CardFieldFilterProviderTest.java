package objective.taskboard.filterPreferences;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import objective.taskboard.data.CardFieldFilter;
import objective.taskboard.data.CardFieldFilter.FieldSelector;
import objective.taskboard.data.Team;
import objective.taskboard.domain.Project;
import objective.taskboard.issueTypeVisibility.IssueTypeVisibilityService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.team.UserTeamPermissionService;

@RunWith(MockitoJUnitRunner.class)
public class CardFieldFilterProviderTest {

    private CardFieldFilterProvider subject;

    @Before
    public void setup() {
        subject = new CardFieldFilterProvider(getIssueTypeVisibilityServiceMock(), getUserTeamPermissionServiceMock(), getProjectServiceMock());
    }

    @Test
    public void getDefaultList_returnAllFieldSelectorsOnTheList() {
        List<CardFieldFilter> cardFieldFilters = subject.getDefaultList();

        assertEquals(FieldSelector.values().length, cardFieldFilters.size());
        Arrays.stream(FieldSelector.values())
            .allMatch(fs -> cardFieldFilters.stream().anyMatch(cff -> cff.getFieldSelector() == fs));
    }

    @Test
    public void getDefaultList_returnAllValuesSortedByName() {
        assertOrder(subject.getDefaultList(),
               "Issue Type",
                "- ISSUE_TYPE_1",
                "- ISSUE_TYPE_2",
                "- ISSUE_TYPE_3",
               "Project",
                "- Project 1",
                "- Project 2",
                "- Project 3",
               "Team",
                "- TEAM_1",
                "- TEAM_2",
                "- TEAM_3");
    }

    private IssueTypeVisibilityService getIssueTypeVisibilityServiceMock() {
        IssueTypeVisibilityService issueTypeVisibilityService = mock(IssueTypeVisibilityService.class);
        when(issueTypeVisibilityService.getVisibleIssueTypes()).thenReturn(asList(
                new JiraIssueTypeDto(1L, "ISSUE_TYPE_1", true),
                new JiraIssueTypeDto(2L, "ISSUE_TYPE_2", true),
                new JiraIssueTypeDto(3L, "ISSUE_TYPE_3", true)
                ));
        return issueTypeVisibilityService;
    }

    private UserTeamPermissionService getUserTeamPermissionServiceMock() {
        UserTeamPermissionService userTeamPermissionService = mock(UserTeamPermissionService.class);
        when(userTeamPermissionService.getTeamsVisibleToLoggedInUser()).thenReturn(Stream.of(
                new Team("TEAM_1", null, null, asList()),
                new Team("TEAM_2", null, null, asList()),
                new Team("TEAM_3", null, null, asList())
                ).collect(Collectors.toSet()));
        return userTeamPermissionService;
    }

    private ProjectService getProjectServiceMock() {
        ProjectService projectService = mock(ProjectService.class);
        when(projectService.getNonArchivedJiraProjectsForUser()).thenReturn(asList(
                createProject("PROJ_1", "Project 1", 1L),
                createProject("PROJ_2", "Project 2", 2L),
                createProject("PROJ_3", "Project 3", 3L)
                ));
        return projectService;
    }

    private static Project createProject(String key, String name, Long teamId) {
        Project project = new Project();
        project.setKey(key);
        project.setName(name);
        project.setTeamId(teamId);
        return project;
    }

    private void assertOrder(List<CardFieldFilter> cardFieldFilters, String... expected) {
        String actual = cardFieldFilters.stream()
                .map(f -> {
                    return f.getFieldSelector().getName() + "\n- " + f.getFilterFieldsValues().stream()
                            .map(v -> v.getName())
                            .collect(joining("\n- "));
                })
                .collect(joining("\n"));

        assertEquals(StringUtils.join(expected, "\n"), actual);
    }

}
