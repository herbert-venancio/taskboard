package objective.taskboard.followup.kpi.services;

import static java.util.Collections.emptyList;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.Mockito;

import objective.taskboard.data.Issue;
import objective.taskboard.domain.IssueColorService;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.FollowUpSnapshot;
import objective.taskboard.followup.FollowUpSnapshotService;
import objective.taskboard.followup.IssueTransitionService;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.followup.kpi.services.IssueKpiService;
import objective.taskboard.followup.kpi.services.KpiDataService;
import objective.taskboard.followup.kpi.services.KpiEnvironment.StatusDto;
import objective.taskboard.followup.kpi.services.snapshot.AnalyticsDataSetsGenerator;
import objective.taskboard.followup.kpi.services.snapshot.SnapshotGenerator;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapter;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapterFactory;
import objective.taskboard.issueBuffer.IssueBufferService;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.Clock;

public class MockedServices {

    private KpiEnvironment environment;
    private IssueKpiTestRepository issuesRepository;
    private ProjectServiceMocker projects = new ProjectServiceMocker(this);
    private IssueKpiServiceMocker issueKpiServices = new IssueKpiServiceMocker();
    private IssueBufferServiceMocker issueBufferService = new IssueBufferServiceMocker();
    private IssueKpiDataItemAdapterFactoryMocker factory = new IssueKpiDataItemAdapterFactoryMocker();
    private MetadataServiceMocker metadataService = new MetadataServiceMocker();
    private TransitionServiceMocker transitionsService = new TransitionServiceMocker();
    private IssueColorServiceMocker colorService = new IssueColorServiceMocker();
    private FollowupSnapshotServiceMocker snapshotService = new FollowupSnapshotServiceMocker();
    private KpiDataServiceMocker kpiDataServiceMocker = new KpiDataServiceMocker();

    public MockedServices(KpiEnvironment environment) {
        this.environment = environment;
        this.issuesRepository = new IssueKpiTestRepository(environment);
    }

    public void prepareAllMocks() {
        projects.mockService();
        issueKpiServices.prepareMock();
        issueBufferService.prepareMock();
        factory.prepareMock();
        metadataService.prepareMock();
        transitionsService.prepareMock();
        colorService.prepareMock();
        snapshotService.prepareMock();
        kpiDataServiceMocker.preapreMock();
    }

    public ProjectServiceMocker projects() {
        return projects;
    }

    public IssueKpiServiceMocker issueKpi() {
        return issueKpiServices;
    }
    
    public KpiDataServiceMocker kpiDataService() {
        return kpiDataServiceMocker;
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

    public IssueColorServiceMocker issueColor() {
        return colorService;
    }
    
    public FollowupSnapshotServiceMocker followupSnapshot() {
        return snapshotService;
    }

    public KpiEnvironment eoS() {
        return environment;
    }

    public class IssueKpiServiceMocker {
        
        private IssueKpiService service = Mockito.mock(IssueKpiService.class);

        public IssueKpiService getService() {
            if(service == null)
                prepareMock();
            return service;
        }
        
        public Map<String, IssueKpi> getIssuesKpiByKey() {
            return issuesRepository.getIssuesKpi();
        }

        public Map<KpiLevel, List<IssueKpi>> getIssuesByLevel() {
            return getIssuesKpiByKey().values().stream().collect(Collectors.groupingBy(IssueKpi::getLevel));
        }

        private void prepareMock() {
            Map<String, Map<KpiLevel, List<IssueKpi>>> byProject = issuesRepository.getIssuesByProject();
            byProject.entrySet().stream().forEach(entry -> {
                String projectKey = entry.getKey();
                Map<KpiLevel, List<IssueKpi>> issuesByProject = entry.getValue();
                issuesByProject.entrySet().forEach(levelIssues -> mockIssuesByLevel(projectKey,levelIssues.getKey(),levelIssues.getValue()));
            });

        }

        void mockIssuesByLevel(String projectKey, KpiLevel level, List<IssueKpi> issuesByProjectAndLevel) {
            Mockito.when(service.getIssuesFromCurrentState(projectKey, environment.getTimezone(), level))
                    .thenReturn(issuesByProjectAndLevel);
        }

        public KpiEnvironment eoIks() {
            return environment;
        }

        public IssueKpiService buildServiceInstance() {

            JiraProperties jiraProperties = environment.getJiraProperties();
            KPIProperties kpiProperties = environment.getKPIProperties();
            Clock clock = environment.getClock();

            IssueBufferService issuesBufferService = issuesBuffer().getService();
            IssueKpiDataItemAdapterFactory itemFactory = itemAdapterFactory().getComponent();

            return new IssueKpiService(issuesBufferService, jiraProperties, kpiProperties, clock, itemFactory);
        }

    }

    public class IssueBufferServiceMocker {

        private IssueBufferService service;

        public IssueBufferService getService() {
            if(service == null)
                prepareMock();
            return service;
        }

        private void prepareMock() {
            service = Mockito.mock(IssueBufferService.class);
            Mockito.when(service.getAllIssues()).thenReturn(issuesRepository.getIssues());
        }
        
        public List<Issue> getIssues(){
            return issuesRepository.getIssues();
        }

    }

    public class IssueKpiDataItemAdapterFactoryMocker {
        private IssueKpiDataItemAdapterFactory factory;

        public IssueKpiDataItemAdapterFactory getComponent() {
            if(factory == null)
                prepareMock();
            return factory;
        }

        private void prepareMock() {
            factory = Mockito.mock(IssueKpiDataItemAdapterFactory.class);
            List<Issue> issues = issuesRepository.getIssues();
            Mockito.when(factory.getItems(issues, environment.getTimezone())).thenReturn(getMappedItemAdapters());
        }

        private List<IssueKpiDataItemAdapter> getMappedItemAdapters() {
            return environment.getAllIssueMockers().stream()
                    .filter(m -> m.level() != KpiLevel.UNMAPPED)
                    .map(IssueKpiMocker::buildAsAdapter)
                    .collect(Collectors.toList());
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
                prepareMock();
            return transitionService;
        }

        private void prepareMock() {
            transitionService = Mockito.mock(IssueTransitionService.class);

            ZoneId timezone = environment.getTimezone();
            JiraProperties jiraProperties = environment.getJiraProperties();
            environment.getAllIssueMockers().forEach(i -> 
                Mockito.when(transitionService.getTransitions(i.mockedIssue(), timezone, i.level().getStatusPriorityOrder(jiraProperties))).thenReturn(i.getReversedTransitions())
            );
        }

    }

    public class MetadataServiceMocker {
        private MetadataService metadataService;

        public MetadataService getService() {
            if(metadataService == null)
                prepareMock();
            return metadataService;
        }

        private void prepareMock() {
            metadataService = Mockito.mock(MetadataService.class);

            environment.types().foreach(typeDto ->{
                JiraIssueTypeDto jiraDto = new JiraIssueTypeDto(typeDto.id(), typeDto.name(), typeDto.isSubtask());
                Mockito.when(metadataService.getIssueTypeByName(typeDto.name())).thenReturn(Optional.of(jiraDto));
                Mockito.when(metadataService.getIssueTypeById(typeDto.id())).thenReturn(jiraDto);
            });
        }
    }
    
    public class KpiDataServiceMocker {

        private KpiDataService service;
        
        public KpiDataService getService() {
            if(service == null)
                preapreMock();
            return service;
        }
        
        private void preapreMock() {
            service = Mockito.mock(KpiDataService.class);
            Map<String, Map<KpiLevel, List<IssueKpi>>> byProject = issuesRepository.getIssuesByProject();
            byProject.entrySet().stream().forEach(entry -> {
                String projectKey = entry.getKey();
                Map<KpiLevel, List<IssueKpi>> issuesByProject = entry.getValue();
                issuesByProject.entrySet().forEach(levelIssues -> mockIssuesByLevel(projectKey,levelIssues.getKey(),levelIssues.getValue()));
            });
            
        }
        
        public void prepareFromDataSet(AnalyticsDataSetsGenerator factory) {
            Stream.of(KpiLevel.values()).forEach(level ->
                when(getService().getIssuesFromAnalyticDataSet(factory.getOptionalDataSetForLevel(level)))
                    .thenReturn(issuesRepository.getIssuesByLevel(level))
            );
        }

        void mockIssuesByLevel(String projectKey, KpiLevel level, List<IssueKpi> issuesByProjectAndLevel) {
            Mockito.when(service.getIssuesFromCurrentState(projectKey, environment.getTimezone(), level))
                    .thenReturn(issuesByProjectAndLevel);
            Mockito.when(service.getIssuesFromCurrentProjectRange(projectKey, environment.getTimezone(), level))
                    .thenReturn(issuesByProjectAndLevel);
        }
        
    }

    public class IssueColorServiceMocker {
        private String progressingColor = "#123456";
        private String nonProgressingColor = "#FEDCBA";
        private IssueColorService service;

        public IssueColorService getService() {
            if (service == null) {
                prepareMock();
            }
            return service;
        }

        public IssueColorServiceMocker withProgressingStatusesColor(String color) {
            this.progressingColor = color;
            return this;
        }

        public IssueColorServiceMocker withNonProgressingStatusesColor(String color) {
            this.nonProgressingColor = color;
            return this;
        }

        public MockedServices eoIC() {
            return MockedServices.this;
        }

        private void prepareMock() {
            service =  Mockito.mock(IssueColorService.class);
            Collection<StatusDto> statuses = environment.statuses().getStatuses();
            environment.types().foreach(type -> {
                statuses.forEach(statusDto -> {
                    Mockito.when(service.getStatusColor(type.id(), statusDto.name()))
                        .thenReturn(statusDto.isProgressingStatus() ? progressingColor : nonProgressingColor);
                });
            });
        }
    }
    
    public class FollowupSnapshotServiceMocker {
        
        private FollowUpSnapshotService service;
        
        public FollowUpSnapshotService getService() {
            if (service == null)
                prepareMock();
            
            return service;
            
        }

        private void prepareMock() {
            service = Mockito.mock(FollowUpSnapshotService.class);
        }

        public void prepareForGenerator(SnapshotGenerator snapshotGenerator, String projectKey, ZoneId timezone) {
            FollowUpSnapshotService mockedService = getService();
            FollowUpSnapshot snapshot = snapshotGenerator.buildSnapshot();
            Mockito.when(mockedService.getFromCurrentState(timezone, projectKey)).thenReturn(snapshot);
            Mockito.when(mockedService.get(Mockito.any(),Mockito.eq(timezone), Mockito.eq(projectKey))).thenReturn(snapshot);
        }
        
    }

    
}
