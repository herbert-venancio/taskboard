package objective.taskboard.followup.kpi.enviroment;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.Mockito;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapter;
import objective.taskboard.followup.kpi.transformer.IssueKpiDataItemAdapterFactory;
import objective.taskboard.issueBuffer.IssueBufferService;

public class MockedServices {

    private KpiEnvironment fatherEnvironment;
    private ProjectServiceMocker projects = new ProjectServiceMocker(this);
    private IssueKpiServiceMocker issueKpiServices = new IssueKpiServiceMocker();
    private IssueBufferServiceMocker issueBufferService = new IssueBufferServiceMocker();
    private IssueKpiDataItemAdapterFactoryMocker factory = new IssueKpiDataItemAdapterFactoryMocker();

    public MockedServices(KpiEnvironment fatherEnvironment) {
        this.fatherEnvironment = fatherEnvironment;
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

    public IssueKpiDataItemAdapterFactoryMocker itemAdapterFactory() {
        return factory;
    }

    public KpiEnvironment eoS() {
        return fatherEnvironment;
    }

    public class IssueKpiServiceMocker {
        private IssueKpiService service = Mockito.mock(IssueKpiService.class);

        public IssueKpiService getService() {
            mockService();
            return service;
        }

        private void mockService() {
            Map<KpiLevel, List<IssueKpi>> issuesByLevel = fatherEnvironment.buildAllIssues();
            Stream.of(KpiLevel.values()).forEach(level -> {
                mockIssuesForLevel(level, issuesByLevel.get(level));
            });

        }

        private void mockIssuesForLevel(KpiLevel level, List<IssueKpi> issuesByLevel) {
            Mockito.when(service.getIssuesFromCurrentState(fatherEnvironment.getProjectKey(),fatherEnvironment.getTimezone(), level))
                .thenReturn(issuesByLevel);
        }

        public KpiEnvironment eoIks() {
            return fatherEnvironment;
        }

    }

    public class IssueBufferServiceMocker {

        private IssueBufferService service = Mockito.mock(IssueBufferService.class);
        private List<Issue> allIssues;

        public IssueBufferService getService() {
            prepareService();
            return service;
        }

        private void prepareService() {
            List<Issue> allIssues = getIssues();
            Mockito.when(service.getAllIssues()).thenReturn(allIssues);
        }

        List<Issue> getIssues() {
            if (allIssues == null)
                allIssues = fatherEnvironment.mockAllIssues().collectIssuesMocked();

            return allIssues;

        }
    }

    public class IssueKpiDataItemAdapterFactoryMocker {
        private IssueKpiDataItemAdapterFactory factory = Mockito.mock(IssueKpiDataItemAdapterFactory.class);

        public IssueKpiDataItemAdapterFactory getComponent() {
            List<Issue> issues = issueBufferService.getIssues();
            Mockito.when(factory.getItems(issues, fatherEnvironment.getTimezone())).thenReturn(getMappedItemAdapters());
            return factory;
        }

        private List<IssueKpiDataItemAdapter> getMappedItemAdapters() {
            return fatherEnvironment.getAllIssueMockers().stream()
                    .filter(m -> test(m))
                    .map(m -> buildAdapter(m))
                    .collect(Collectors.toList());
        }

        private boolean test(IssueKpiMocker m) {
            return m.level() != KpiLevel.UNMAPPED;
        }

        private IssueKpiDataItemAdapter buildAdapter(IssueKpiMocker mocked) {
            return new FakeIssueKpiAdapter(mocked.getReveredTransitions(), mocked.getIssueKey(),mocked.getIssueTypeKpi(), mocked.level());
        }

    }

}
