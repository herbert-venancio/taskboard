package objective.taskboard.filterPreferences;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.data.CardFieldFilter;
import objective.taskboard.data.Issue;
import objective.taskboard.data.IssuesConfiguration;
import objective.taskboard.data.LaneConfiguration;
import objective.taskboard.database.TaskboardDatabaseService;

@Service
public class CardFieldFilterService {

    private CardFieldFilterProvider cardFieldFilterProvider;
    private UserPreferencesService userPreferencesService;
    private TaskboardDatabaseService taskboardDatabaseService;

    @Autowired
    public CardFieldFilterService(
            CardFieldFilterProvider cardFieldFilterProvider,
            UserPreferencesService userPreferencesService,
            TaskboardDatabaseService taskboardDatabaseService
            ) {
        this.cardFieldFilterProvider = cardFieldFilterProvider;
        this.userPreferencesService = userPreferencesService;
        this.taskboardDatabaseService = taskboardDatabaseService;
    }

    public List<CardFieldFilter> getFilterForLoggerUser() {
        List<CardFieldFilter> cardFieldFilters = cardFieldFilterProvider.getDefaultList();
        userPreferencesService.applyLoggedUserPreferencesOnCardFieldFilter(cardFieldFilters);
        return cardFieldFilters;
    }

    public List<Issue> getIssuesSelectedByLoggedUser(List<Issue> issues) {
        Stream<Issue> selectedIssues = getSelectedIssuesByLaneConfiguration(issues.stream());
        selectedIssues = getSelectedIssuesByCardFieldFilters(selectedIssues);
        return selectedIssues.collect(toList());
    }

    private Stream<Issue> getSelectedIssuesByLaneConfiguration(Stream<Issue> issues) {
        List<LaneConfiguration> lanesConfiguration = taskboardDatabaseService.laneConfiguration();
        userPreferencesService.applyLoggedUserPreferencesOnLaneConfiguration(lanesConfiguration);
        List<IssuesConfiguration> issuesConfToShow = lanesConfiguration.stream()
                .filter(laneConfiguration -> laneConfiguration.isShowLevel())
                .flatMap(laneConfiguration -> laneConfiguration.getStages().stream())
                .flatMap(stage -> stage.getSteps().stream())
                .flatMap(step -> step.getIssuesConfiguration().stream())
                .collect(toList());
        return issues.filter(issue -> isIssueSelectedByIssuesConfigurations(issue, issuesConfToShow));
    }

    private boolean isIssueSelectedByIssuesConfigurations(Issue issue, List<IssuesConfiguration> issuesConfigurations) {
        return issuesConfigurations.stream()
                .anyMatch(issueConf -> issueConf.matches(issue));
    }

    private Stream<Issue> getSelectedIssuesByCardFieldFilters(Stream<Issue> issues) {
        List<CardFieldFilter> cardFieldFilters = getFilterForLoggerUser();
        if (cardFieldFilters.isEmpty())
            return Stream.empty();

        return issues.filter(issue -> isIssueSelectedByCardFieldFilters(issue, cardFieldFilters));
    }

    private boolean isIssueSelectedByCardFieldFilters(Issue issue, List<CardFieldFilter> cardFieldFilters) {
        return cardFieldFilters.stream()
            .allMatch(item -> item.isIssueSelected(issue));
    }

}
