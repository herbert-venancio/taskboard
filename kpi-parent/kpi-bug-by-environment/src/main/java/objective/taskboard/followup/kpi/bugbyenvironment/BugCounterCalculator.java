package objective.taskboard.followup.kpi.bugbyenvironment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.bugbyenvironment.filters.FilterNotClosedIssuesOnWeek;
import objective.taskboard.followup.kpi.bugbyenvironment.filters.FilterOnlyBugs;
import objective.taskboard.followup.kpi.filters.KpiWeekRange;
import objective.taskboard.followup.kpi.properties.KpiBugByEnvironmentProperties;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraIssueTypeDto;

public class BugCounterCalculator {

    private final List<IssueKpi> issues;
    private final KpiBugByEnvironmentProperties properties;
    private final Set<String> populatedClientEnvironment;
    private final MetadataService metadataService;
    private final Set<String> bugTypes;
    
    public BugCounterCalculator(MetadataService metadataService, KpiBugByEnvironmentProperties properties, List<IssueKpi> issues) {
        this.metadataService = metadataService;
        this.properties = properties;
        this.issues = filterIssues(issues);
        this.populatedClientEnvironment = getPopulatedClientEnvironment();
        this.bugTypes = getBugTypes();
    }

    public Map<String,Long> getBugsCategorizedOnWeek(KpiWeekRange weekRange){
        return buildMap(getIssuesOpenOn(weekRange));
    }

    private Set<String> getPopulatedClientEnvironment() {
        return this.issues.stream()
                    .map(IssueKpi::getClientEnvironment)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
    }

    private Set<String> getBugTypes() {
        return properties.getBugTypes().stream()
                    .map(metadataService::getIssueTypeById)
                    .map(JiraIssueTypeDto::getName)
                    .collect(Collectors.toSet());
    }

    private List<IssueKpi> filterIssues(List<IssueKpi> allIssues) {
        return allIssues.stream().filter(filterBugs()).collect(Collectors.toList());
    }

    private FilterOnlyBugs filterBugs() {
        return new FilterOnlyBugs(properties.getBugTypes());
    }
    
    private Map<String, Long> buildMap(List<IssueKpi> issuesOpenOnWeek) {
        Map<String,Long> finalMap = new LinkedHashMap<>();
        populatedClientEnvironment.forEach(clientEnvironment-> finalMap.put(clientEnvironment, countCategory(clientEnvironment,issuesOpenOnWeek)));
        bugTypes.forEach(bugType -> finalMap.put(bugType, countBugsWithoutCategory(bugType,issuesOpenOnWeek)));
        return finalMap;
    }

    private Long countBugsWithoutCategory(String bugType, List<IssueKpi> issuesOpenOnWeek) {
        return issuesOpenOnWeek.stream()
                                .filter(issue -> !issue.getClientEnvironment().isPresent())
                                .filter(issue -> issue.getIssueTypeName().equalsIgnoreCase(bugType)).count();
    }

    private Long countCategory(String clientEnvironment, List<IssueKpi> issuesOpenOnWeek) {
        return issuesOpenOnWeek.stream().filter(issue -> issue.isFromClientEnvironment(clientEnvironment)).count();
    }

    private List<IssueKpi> getIssuesOpenOn(KpiWeekRange weekRange){
        return issues.stream().filter(filterNotClosedBugs(weekRange)).collect(Collectors.toList());
    }

    private FilterNotClosedIssuesOnWeek filterNotClosedBugs(KpiWeekRange weekRange){
        return new FilterNotClosedIssuesOnWeek(weekRange);
    }

}
