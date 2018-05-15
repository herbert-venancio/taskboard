package objective.taskboard.filterPreferences;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.CardFieldFilter;
import objective.taskboard.data.CardFieldFilter.FieldSelector;
import objective.taskboard.data.FilterFieldValue;
import objective.taskboard.data.Team;
import objective.taskboard.domain.Project;
import objective.taskboard.issueTypeVisibility.IssueTypeVisibilityService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.team.UserTeamService;

@Service
class CardFieldFilterProvider {

    private IssueTypeVisibilityService issueTypeVisibilityService;
    private UserTeamService userTeamService;
    private ProjectService projectService;

    @Autowired
    public CardFieldFilterProvider(
            IssueTypeVisibilityService issueTypeVisibilityService,
            UserTeamService userTeamService,
            ProjectService projectService
            ) {
        this.issueTypeVisibilityService = issueTypeVisibilityService;
        this.userTeamService = userTeamService;
        this.projectService = projectService;
    }

    public List<CardFieldFilter> getDefaultList() {
        List<JiraIssueTypeDto> visibleIssueTypes = issueTypeVisibilityService.getVisibleIssueTypes();
        Set<Team> teamsVisibleToUser = userTeamService.getTeamsVisibleToLoggedInUser();
        List<Project> nonArchivedJiraProjectsForUser = projectService.getNonArchivedJiraProjectsForUser();

        return Stream.of(
                new CardFieldFilter(FieldSelector.ISSUE_TYPE, getIssueTypeItems(visibleIssueTypes)),
                new CardFieldFilter(FieldSelector.PROJECT, getProjectItems(teamsVisibleToUser, nonArchivedJiraProjectsForUser)),
                new CardFieldFilter(FieldSelector.TEAM, getTeamItems(teamsVisibleToUser))
            ).sorted(this::compareFilter).collect(toList());
    }

    private List<FilterFieldValue> getIssueTypeItems(List<JiraIssueTypeDto> visibleIssueTypes) {
        return visibleIssueTypes.stream()
                .map(t -> new FilterFieldValue(t.getName(), t.getId().toString(), t.getIconUri(), true))
                .sorted(this::compareFilter)
                .collect(toList());
    }

    private List<FilterFieldValue> getProjectItems(Set<Team> teamsVisibleToUser, List<Project> nonArchivedJiraProjectsForUser) {
        return nonArchivedJiraProjectsForUser.stream()
                .map(p -> {
                    List<String> filteredTeams = teamsVisibleToUser.stream()
                        .filter(t -> p.getTeamId().equals(t.getId()))
                        .map(t -> t.getName())
                        .collect(toList());
                    return new FilterFieldValue(p.getName(), p.getKey(), null, true, filteredTeams, p.getVersions());
                })
                .sorted(this::compareFilter)
                .collect(toList());
    }

    private List<FilterFieldValue> getTeamItems(Set<Team> teamsVisibleToUser) {
        return teamsVisibleToUser.stream()
                .map(t -> new FilterFieldValue(t.getName(), t.getName(), null, true))
                .sorted(this::compareFilter)
                .collect(toList());
    }

    private int compareFilter(CardFieldFilter f1, CardFieldFilter f2) {
        if (f1 == null && f2 == null) return 0;
        if (f1 == null) return 1;
        if (f2 == null) return -1;
        return f1.getFieldSelector().getName().compareTo(f2.getFieldSelector().getName());
    }

    private int compareFilter(FilterFieldValue f1, FilterFieldValue f2) {
        if (f1 == null && f2 == null) return 0;
        if (f1 == null) return 1;
        if (f2 == null) return -1;
        return f1.getName().compareTo(f2.getName());
    }
}
