package objective.taskboard.followup.kpi.enviroment;

import static java.util.Arrays.asList;
import static objective.taskboard.followup.kpi.KpiLevel.DEMAND;
import static objective.taskboard.followup.kpi.KpiLevel.FEATURES;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.Mockito;

import objective.taskboard.data.Issue;
import objective.taskboard.data.Worklog;
import objective.taskboard.followup.IssueTransitionService;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueTypeKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.StatusTransitionBuilder.DefaultStatus;
import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy;
import objective.taskboard.followup.kpi.properties.IssueTypeChildrenStatusHierarchy.Hierarchy;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapter;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.utils.DateTimeUtils;

public class KPIEnvironmentBuilder {

    private Map<String, IssueTypeKpi> featuresType = new LinkedHashMap<>();
    private Map<String, IssueTypeKpi> subtasksType = new LinkedHashMap<>();
    private Map<String, IssueKpiBuilder> issues = new LinkedHashMap<>();
    private Map<IssueKpiBuilder,List<IssueKpiBuilder>> issueHierarcy = new LinkedHashMap<>();
    private Map<String, DefaultStatus> statuses = new LinkedHashMap<>();
    private IssueTypeKpi demandType = new IssueTypeKpi(1l, "Demand");

    private Map<KpiLevel,Map<String,Hierarchy>> hierarchies = new LinkedHashMap<>();
    
    private Optional<IssueKpiBuilder> currentIssue = Optional.empty();
    
    private KPIProperties kpiProperties = Mockito.mock(KPIProperties.class);
    private MetadataService metadataService = Mockito.mock(MetadataService.class);
    private IssueTransitionService transitionService = Mockito.mock(IssueTransitionService.class);
    
    public KPIEnvironmentBuilder() {}
    
    public KPIEnvironmentBuilder(KPIProperties kpiProperties, IssueTransitionService transitionService) {
        this.transitionService = transitionService;
        this.kpiProperties = kpiProperties;
    }
    
    public KPIEnvironmentBuilder(IssueTransitionService transitionService) {
        this.transitionService = transitionService;
    }

    public KPIEnvironmentBuilder(KPIProperties kpiProperties) {
        this.kpiProperties = kpiProperties;
    }

    public KPIEnvironmentBuilder addFeatureType(Long id, String name) {
        featuresType.put(name, new IssueTypeKpi(id, name));
        Mockito.when(metadataService.getIssueTypeById(id)).thenReturn(new JiraIssueTypeDto(id, name, false));
        return this;
    }
    
    public KPIEnvironmentBuilder addChildren(String... pKyes) {
        Stream.of(pKyes).forEach(pkey-> addChild(pkey));
        return this;
    }

    public KPIProperties getMockedKPIProperties() {
        this.mockProperties();
        return kpiProperties;
    }
        
    public Issue mockCurrentIssue() {
        assertCurrentIssueSet();
        return currentIssue.get().mockIssue(transitionService);
    }
    
    public IssueKpiDataItemAdapter buildCurrentIssueKPIAdapter() {
        assertCurrentIssueSet();
        return currentIssue.get().buildAdapter();
    }

    private void assertCurrentIssueSet() {
        if(!currentIssue.isPresent())
            throw new IllegalArgumentException("Current issue must be set");
    }

    public IssueKpi buildCurrentIssueAsKpi() {
        assertCurrentIssueSet();
        IssueKpiBuilder kpiBuilder = currentIssue.get();
        List<IssueKpiBuilder> children = issueHierarcy.getOrDefault(kpiBuilder, Collections.emptyList());
        children.forEach(c -> kpiBuilder.addChild(c.build()));
        return kpiBuilder.build();
    }

    private void mockProperties() {
        IssueTypeChildrenStatusHierarchy subtasksHierarchy = new IssueTypeChildrenStatusHierarchy();
        subtasksHierarchy.setHierarchies(getHierachies(FEATURES));
        Mockito.when(kpiProperties.getFeaturesHierarchy()).thenReturn(subtasksHierarchy);
        
        IssueTypeChildrenStatusHierarchy demandHierarchy = new IssueTypeChildrenStatusHierarchy();
        demandHierarchy.setHierarchies(getHierachies(DEMAND));
        Mockito.when(kpiProperties.getDemandHierarchy()).thenReturn(demandHierarchy);
        
        List<String> progressingStatuses = statuses.values().stream().filter(s -> s.isProgressingStatus).map(s -> s.name).collect(Collectors.toList());
        Mockito.when(kpiProperties.getProgressingStatuses()).thenReturn(progressingStatuses);
        
    }

    private List<Hierarchy> getHierachies(KpiLevel level) {
        if(!hierarchies.containsKey(level))
            return Arrays.asList();
        
        return new LinkedList<>(hierarchies.get(level).values());
    }

    public KPIEnvironmentBuilder addChild(String pkey) {
        assertCurrentIssueSet();
        
       issueHierarcy.putIfAbsent(currentIssue.get(), new LinkedList<>());
       issueHierarcy.get(currentIssue.get()).add(issues.get(pkey));
       
       return this;
    }

    public KPIEnvironmentBuilder withIssue(String pkey) {
        currentIssue = Optional.ofNullable(issues.get(pkey));
        return this;
    }

    public KPIEnvironmentBuilder addTransition(String status, String dateTime) {
        if(!statuses.containsKey(status))
            throw new IllegalArgumentException("Missing status configuration");
        if(dateTime == null)
            return addTransition(status);
        
        currentIssue.ifPresent(kpiBuilder -> kpiBuilder.addTransition(statuses.get(status),dateTime));
        return this;
    }
    
    public KPIEnvironmentBuilder addTransition(String status) {
        if(!statuses.containsKey(status))
            throw new IllegalArgumentException("Missing status configuration");
        currentIssue.ifPresent(kpiBuilder -> kpiBuilder.addTransition(statuses.get(status)));
        return this;
    }

    public KPIEnvironmentBuilder addStatus(Long id, String status, boolean isProgressing) {
        statuses.put(status, new DefaultStatus(id,status, isProgressing));
        return this;
    }
    
    public KPIEnvironmentBuilder addSubtaskHierarchy(KpiLevel level, String fatherStatus, String... childrenTypesNames) {

        List<Long> typesId = Stream.of(childrenTypesNames).filter(s -> subtasksType.containsKey(s))
                .map(s -> subtasksType.get(s).getId()).collect(Collectors.toList());

        
        Hierarchy hierarchy = getHierarchy(level, fatherStatus);
        hierarchy.setChldrenTypeId(typesId);
        return this;
    }

    private Hierarchy getHierarchy(KpiLevel level,String fatherStatus) {
        if(!hierarchies.containsKey(level))
            hierarchies.put(level, new LinkedHashMap<>());
        
        Map<String, Hierarchy> hierarchiesByLevel = hierarchies.get(level);
        
        if(!hierarchiesByLevel.containsKey(fatherStatus)) {
            Hierarchy h = new Hierarchy();
            h.setFatherStatus(fatherStatus);
            hierarchiesByLevel.put(fatherStatus,h);
        }
        
        return hierarchiesByLevel.get(fatherStatus);
    }
    
    public KPIEnvironmentBuilder addStatusHierarchy(KpiLevel level, String fatherStatus, String... childrenStatus) {
        
        Hierarchy hierarchy = getHierarchy(level,fatherStatus);
        hierarchy.setChildrenStatus(asList(childrenStatus));
        return this;
    }

    public KPIEnvironmentBuilder addSubtaskType(Long id, String name) {
        subtasksType.put(name, new IssueTypeKpi(id, name));
        Mockito.when(metadataService.getIssueTypeById(id)).thenReturn(new JiraIssueTypeDto(id, name, true));
        return this;
    }

    public KPIEnvironmentBuilder withMockingIssue(String pkey, String type, KpiLevel level) {
        IssueTypeKpi typeKpi = getType(type, level);
        IssueKpiBuilder builder = new IssueKpiBuilder(pkey, typeKpi, level);
        issues.put(pkey, builder);
        withIssue(pkey);
        return this;
    }

    private IssueTypeKpi getType(String type, KpiLevel level) {
        if (KpiLevel.UNMAPPED.equals(level))
            return null;
        if (KpiLevel.DEMAND.equals(level))
            return demandType;
        return KpiLevel.FEATURES.equals(level) ? featuresType.get(type) : subtasksType.get(type);
    }

    public KPIEnvironmentBuilder setCurrentStatusToCurrentIssue(String statusName) {
        assertCurrentIssueSet();
        DefaultStatus status = statuses.get(statusName);
        currentIssue.ifPresent(c -> c.withStatus(status));
        return this;
    }

    public KPIEnvironmentBuilder setProjectKeyToCurrentIssue(String projectKey) {
        assertCurrentIssueSet();
        currentIssue.ifPresent(c -> c.withProjectKey(projectKey));
        return this;
    }

    public KPIEnvironmentBuilder setFatherToCurrentIssue(String fatherKey) {
        assertCurrentIssueSet();
        currentIssue.ifPresent(c -> c.withFather(fatherKey));
        return this;        
    }

    public KPIEnvironmentBuilder addWorklog(Worklog worklog) {
        assertCurrentIssueSet();
        currentIssue.ifPresent(c -> c.addWorklog(worklog));
        return this;
    }
    
    public KPIEnvironmentBuilder addWorklog(String date, int time) {
        Worklog worklog = new Worklog("a.developer",DateTimeUtils.parseStringToDate(date),time);
        return addWorklog(worklog);
    }

    public List<Issue> mockAllIssues() {
        return this.issues.values().stream().map(b -> b.mockIssue(transitionService)).collect(Collectors.toList());
    }

    public List<IssueKpiDataItemAdapter> buildAllIssuesAsAdapter() {
        return this.issues.values().stream().map(b -> b.buildAdapter()).collect(Collectors.toList());
    }

}
