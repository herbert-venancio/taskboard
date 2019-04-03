package objective.taskboard.followup.kpi.transformer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import objective.taskboard.data.Issue;
import objective.taskboard.data.Worklog;
import objective.taskboard.followup.kpi.ChildrenWorklogDistributor;
import objective.taskboard.followup.kpi.DatedStatusTransition;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.StatusTransition;
import objective.taskboard.followup.kpi.SubtaskWorklogDistributor;
import objective.taskboard.followup.kpi.ZonedWorklog;
import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.utils.Clock;

public class IssueKpiTransformer {

    private Map<String,IssueKpi> issuesKpi = new LinkedHashMap<>();
    private Map<String,IssueKpiDataItemAdapter> items = new LinkedHashMap<>();
    private Map<String,Issue> originalIssues = new LinkedHashMap<>();
    private KPIProperties kpiProperties;
    private Clock clock;
    private boolean mappingHierarchically = false;
    private boolean settingWorklog = false;
    private List<Predicate<IssueKpi>> filters = new LinkedList<>();
    private Optional<ZoneId> timezone = Optional.empty();

    public IssueKpiTransformer(KPIProperties kpiProperties, Clock clock) {
        this.kpiProperties = kpiProperties;
        this.clock = clock;
    }

    public IssueKpiTransformer withItems(List<IssueKpiDataItemAdapter> items) {
        this.items = items.stream().collect(Collectors.toMap(IssueKpiDataItemAdapter::getIssueKey, Function.identity()));
        return this;
    }

    public IssueKpiTransformer withOriginalIssues(List<Issue> originalIssues) {
        this.originalIssues = originalIssues.stream().collect(Collectors.toMap(Issue::getIssueKey, Function.identity()));
        return this;
    }

    public IssueKpiTransformer mappingHierarchically() {
        this.mappingHierarchically  = true;
        return this;
    }

    public IssueKpiTransformer settingWorklogWithTimezone(ZoneId timezone) {
        this.settingWorklog  = true;
        this.timezone = Optional.of(timezone);
        return this;
    }

    public List<IssueKpi> transform(){
        validateParameters();

        map();

        if(mappingHierarchically)
            mapHierarchy();

        if(settingWorklog)
            putWorklogs();

        Predicate<IssueKpi> finalFilter = filters.stream().reduce(w -> true, Predicate::and);
        return issuesKpi.values().stream().filter(finalFilter).collect(Collectors.toList());
    }

    private void putWorklogs() {
        Map<KpiLevel,List<IssueKpi>> issuesByLevel = new EnumMap<>(KpiLevel.class);
        for (KpiLevel level : KpiLevel.values()) {
            issuesByLevel.put(level, issuesKpi.values().stream().filter(i -> i.getLevel() == level).collect(Collectors.toList()));
        }
        mapSubtaskWorklog(issuesByLevel.get(KpiLevel.SUBTASKS));
        mapChildrenWorklog(kpiProperties.getFeaturesHierarchy(),issuesByLevel.get(KpiLevel.FEATURES));
        mapChildrenWorklog(kpiProperties.getDemandHierarchy(),issuesByLevel.get(KpiLevel.DEMAND));
    }

    private void mapSubtaskWorklog(List<IssueKpi> issues) {
        SubtaskWorklogDistributor distributor = new SubtaskWorklogDistributor();
        for (IssueKpi issueKpi : issues) {
            Issue originalIssue = originalIssues.get(issueKpi.getIssueKey());
            List<ZonedWorklog> zonedWorklogs = originalIssue.getWorklogs().stream()
                    .filter(w -> w.started != null)
                    .map(this::transformIntoZonedWorklog)
                    .collect(Collectors.toList());
            distributor.distributeWorklogs(issueKpi, zonedWorklogs);
        }
    }

    private ZonedWorklog transformIntoZonedWorklog(Worklog worklog) {
        return new ZonedWorklog(worklog, timezone.orElseThrow(() -> new IllegalArgumentException("Timezone can't be empty")));
    }

    private void mapChildrenWorklog(IssueTypeChildrenStatusHierarchy hierarchyProperty, List<IssueKpi> issues) {
        for (IssueKpi issueKpi : issues) {
            ChildrenWorklogDistributor.distributeWorklogs(hierarchyProperty, issueKpi);
        }
    }

    private void mapHierarchy() {
        for (IssueKpi issueKpi : issuesKpi.values()) {
            Issue parentIssue = originalIssues.get(issueKpi.getIssueKey());
            String parentKey = parentIssue.getParent();
            Optional<IssueKpi> parentIssueKpi = Optional.ofNullable(issuesKpi.get(parentKey));
            parentIssueKpi.ifPresent(p -> p.addChild(issueKpi));
        }
    }

    private void map(){
        for (IssueKpiDataItemAdapter itemProvider : items.values()) {
            
            Optional<StatusTransition> next = Optional.empty();
            
            for (Entry<String,ZonedDateTime> transition : itemProvider.getTransitions().entrySet()) {
                String status = transition.getKey();
                ZonedDateTime date = transition.getValue();

                StatusTransition statusTrasition = create(status, date,next);
                next = Optional.of(statusTrasition);
            }
            KpiLevel level = itemProvider.getLevel();
            IssueKpi issue = new IssueKpi(itemProvider.getIssueKey(),itemProvider.getIssueType(), level,next, clock);
            mapCustomFields(issue,itemProvider);
            issuesKpi.put(itemProvider.getIssueKey(), issue);
        }
    }

    

    private void mapCustomFields(IssueKpi issue, IssueKpiDataItemAdapter itemProvider) {
        issue.setClientEnvironment(getClientEnvironment(itemProvider));
    }

    private Optional<String> getClientEnvironment(IssueKpiDataItemAdapter itemProvider) {
        return itemProvider.getCustomFieldValue(kpiProperties.getEnvironmentField());
    }

    private boolean isProgressingStatus(String status) {
        return kpiProperties.getProgressingStatuses().contains(status);
    }

    private StatusTransition create(String status, ZonedDateTime date, Optional<StatusTransition> next) {
        boolean isProgressingStatus = isProgressingStatus(status);
        return (date == null) ? new StatusTransition(status,isProgressingStatus, next) :  new DatedStatusTransition(status, date,isProgressingStatus,next);
    }

    private void validateParameters() {

        if(this.mappingHierarchically && originalIssues.isEmpty())
            throw new IllegalArgumentException("To map issues hierarchically, the original issues must be provided");

        if(this.settingWorklog && originalIssues.isEmpty())
            throw new IllegalArgumentException("To map the issues worklogs, the original issues must be provided");

        if(this.settingWorklog && !timezone.isPresent()) {
            throw new IllegalArgumentException("To map the issues worklogs, the timezone must be provided");
        }
    }

    public IssueKpiTransformer filter(Predicate<IssueKpi> filter) {
        this.filters.add(filter);
        return this;
    }

}
