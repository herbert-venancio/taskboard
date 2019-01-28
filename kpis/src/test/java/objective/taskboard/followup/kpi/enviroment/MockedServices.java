package objective.taskboard.followup.kpi.enviroment;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.Mockito;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.IssueTransitionService;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapter;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapterFactory;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.Clock;

public class MockedServices {

    private KpiEnvironment environment;
    private ProjectServiceMocker projects = new ProjectServiceMocker(this);
    private IssueKpiServiceMocker issueKpiServices = new IssueKpiServiceMocker();
    private IssueBufferServiceMocker issueBufferService = new IssueBufferServiceMocker();
    private IssueKpiDataItemAdapterFactoryMocker factory = new IssueKpiDataItemAdapterFactoryMocker();
    private MetadataServiceMocker metadataService = new MetadataServiceMocker();
    private TransitionServiceMocker transitionsService = new TransitionServiceMocker();

    public MockedServices(KpiEnvironment fatherEnvironment) {
        this.environment = fatherEnvironment;
    }

    public void mockAll() {
        projects.mockService();
        issueKpiServices.mockService();
        issueBufferService.mockService();
        factory.mockComponent();
        metadataService.mockService();
        transitionsService.mockService();
    }

    public ProjectServiceMocker projects() {
        return projects;
    }

    public IssueKpiServiceMocker issueKpi() {
        return issueKpiServices;
    }

    public IssueBufferServiceMocker issuesBuffer() {
        return issueBufferService;
    }

    public TransitionServiceMocker issuesTransition() {
        return transitionsService;
    }

    public IssueKpiDataItemAdapterFactoryMocker itemAdapterFactory() {
        return factory;
    }

    public MetadataServiceMocker metadata() {
        return metadataService;
    }

    public KpiEnvironment eoS() {
        return environment;
    }

    public class IssueKpiServiceMocker {
        private IssueKpiService service = Mockito.mock(IssueKpiService.class);
        private Map<String,IssueKpi> issues;

        public IssueKpiService getService() {
            if(service == null)
                mockService();
            return service;
        }

        public Map<String,IssueKpi> getIssues(){
            if(issues == null)
                mockService();
            return issues;
        }

        public void prepareFromDataSet(GenerateAnalyticsDataSets factory) {
            Stream.of(KpiLevel.values()).forEach(level ->
                when(getService().getIssues(factory.getOptionalDataSetForLevel(level)))
                    .thenReturn(getIssuesByLevel(level))
            );
        }

        public Map<KpiLevel, List<IssueKpi>> getIssuesByLevel() {
            return getIssues().values().stream().collect(Collectors.groupingBy(IssueKpi::getLevel));
        }

        private void mockService() {
            List<IssueKpiMocker> allIssues = environment.getAllIssueMockers();
            issues = new LinkedHashMap<>();

            Stream.of(KpiLevel.values())
                .forEach(level -> {
                    List<IssueKpiMocker> leveledIssues = allIssues.stream().filter(i -> level.equals(i.level())).collect(Collectors.toList());
                    mockIssues(level, leveledIssues);
                });

        }

        private void mockIssues(KpiLevel level, List<IssueKpiMocker> issuesByLevel) {

            checkProjectConfigured(issuesByLevel);

            Map<String,List<IssueKpiMocker>> byProject = issuesByLevel.stream().collect(Collectors.groupingBy(IssueKpiMocker::project));

            byProject.entrySet().stream().forEach(entry -> {
                String projectKey = entry.getKey();
                List<IssueKpi> issuesByProject = entry.getValue().stream()
                                                        .map(IssueKpiMocker::buildIssueKpi)
                                                        .collect(Collectors.toList());
                saveIssues(issuesByProject);
                Mockito.when(service.getIssuesFromCurrentState(projectKey, environment.getTimezone(), level))
                    .thenReturn(issuesByProject);
            });

        }

        private void saveIssues(List<IssueKpi> issuesByProject) {
            issuesByProject.stream().forEach(issue -> issues.put(issue.getIssueKey(),issue));
        }

        private List<IssueKpi> getIssuesByLevel(KpiLevel level){
            return Optional.ofNullable(getIssuesByLevel().get(level)).orElse(emptyList());
        }

        private void checkProjectConfigured(List<IssueKpiMocker> issuesToCheck) {
            List<String> issuesWithoutProject = issuesToCheck.stream()
                                                    .filter(i -> i.noProjectConfigured())
                                                    .map(i -> i.getIssueKey())
                                                    .collect(Collectors.toList());
            if(issuesWithoutProject.size() > 0)
                throw new AssertionError(String.format("Issues without project configured: %s", issuesToCheck));
        }

        public KpiEnvironment eoIks() {
            return environment;
        }

        public IssueKpiService buildServiceInstance() {

            JiraProperties jiraProperties = environment.getJiraProperties();
            KPIProperties kpiProperties = environment.getKPIProperties();
            Clock clock = environment.getClock();

            IssueBufferService issueBufferService = issuesBuffer().getService();
            ProjectService projectService = projects().getService();
            IssueKpiDataItemAdapterFactory factory = itemAdapterFactory().getComponent();

            return new IssueKpiService(issueBufferService, projectService, jiraProperties, kpiProperties, clock, factory);
        }

    }

    public class IssueBufferServiceMocker {

        private IssueBufferService service;
        private List<Issue> allIssues;

        public IssueBufferService getService() {
            if(service == null)
                mockService();
            return service;
        }

        private void mockService() {
            service = Mockito.mock(IssueBufferService.class);
            prepareService();
        }

        private void prepareService() {
            List<Issue> allIssues = getIssues();
            Mockito.when(service.getAllIssues()).thenReturn(allIssues);
        }

        public List<Issue> getIssues() {
            if (allIssues == null)
                allIssues = mockIssues();

            return allIssues;

        }

        private List<Issue> mockIssues() {
            return environment.getAllIssueMockers().stream()
                    .map(i -> i.mock())
                    .collect(Collectors.toList());
        }
    }

    public class IssueKpiDataItemAdapterFactoryMocker {
        private IssueKpiDataItemAdapterFactory factory;

        public IssueKpiDataItemAdapterFactory getComponent() {
            if(factory == null)
                mockComponent();
            return factory;
        }

        private void mockComponent() {
            factory = Mockito.mock(IssueKpiDataItemAdapterFactory.class);
            List<Issue> issues = issueBufferService.getIssues();
            Mockito.when(factory.getItems(issues, environment.getTimezone())).thenReturn(getMappedItemAdapters());
        }

        private List<IssueKpiDataItemAdapter> getMappedItemAdapters() {
            return environment.getAllIssueMockers().stream()
                    .filter(m -> filterUnmapped(m))
                    .map(IssueKpiMocker::buildAsAdapter)
                    .collect(Collectors.toList());
        }

        private boolean filterUnmapped(IssueKpiMocker m) {
            return m.level() != KpiLevel.UNMAPPED;
        }

        public IssueKpiDataItemAdapterFactoryMocker configureForDataSet(Optional<AnalyticsTransitionsDataSet> analyticsDs) {
            Mockito.when(getComponent().getItems(analyticsDs)).thenReturn(geItems(analyticsDs));
            return this;
        }

        private List<IssueKpiDataItemAdapter> geItems(Optional<AnalyticsTransitionsDataSet> analyticsDs) {
            if(!analyticsDs.isPresent())
                return emptyList();
            List<String> keys = analyticsDs.get().rows.stream().map(r -> r.issueKey).collect(Collectors.toList());
            return environment.getAllIssueMockers().stream().filter( i -> keys.contains(i.getIssueKey())).map(IssueKpiMocker::buildAsAdapter).collect(Collectors.toList());
        }

    }

    public class TransitionServiceMocker {
        private IssueTransitionService transitionService;

        public IssueTransitionService getService() {
            if(transitionService == null)
                mockService();
            return transitionService;
        }

        private void mockService() {
            transitionService = Mockito.mock(IssueTransitionService.class);

            ZoneId timezone = environment.getTimezone();
            JiraProperties jiraProperties = environment.getJiraProperties();
            ensureIssuesAreMocked();
            environment.getAllIssueMockers().forEach(i -> {
                Mockito.when(transitionService.getTransitions(i.mockedIssue(), timezone, i.level().getStatusPriorityOrder(jiraProperties))).thenReturn(i.getReversedTransitions());
            });
        }

        private void ensureIssuesAreMocked() {
            issueBufferService.getIssues();
        }
    }

    public class MetadataServiceMocker {
        private MetadataService metadataService;

        public MetadataService getService() {
            if(metadataService == null)
                mockService();
            return metadataService;
        }

        private void mockService() {
            metadataService = Mockito.mock(MetadataService.class);

            environment.types().foreach(typeDto ->{
                JiraIssueTypeDto jiraDto = new JiraIssueTypeDto(typeDto.id(), typeDto.name(), typeDto.isSubtask());
                Mockito.when(metadataService.getIssueTypeByName(typeDto.name())).thenReturn(Optional.of(jiraDto));
            });
        }

    }
}
