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

    private KpiEnvironment environment;
    private ProjectServiceMocker projects = new ProjectServiceMocker(this);
    private IssueKpiServiceMocker issueKpiServices = new IssueKpiServiceMocker();
    private IssueBufferServiceMocker issueBufferService = new IssueBufferServiceMocker();
    private IssueKpiDataItemAdapterFactoryMocker factory = new IssueKpiDataItemAdapterFactoryMocker();

    public MockedServices(KpiEnvironment fatherEnvironment) {
        this.environment = fatherEnvironment;
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
        return environment;
    }

    public class IssueKpiServiceMocker {
        private IssueKpiService service = Mockito.mock(IssueKpiService.class);

        public IssueKpiService getServiceForProject(String projectKey) {
            mockServiceForProject(projectKey);
            return service;
        }

        private void mockServiceForProject(String projectKey) {
            Map<KpiLevel, List<IssueKpi>> issuesByLevel = environment.buildAllIssues();
            Stream.of(KpiLevel.values())
                .forEach(level -> mockIssuesForProjectAndLevel(projectKey, level, issuesByLevel.get(level)));

        }

        private void mockIssuesForProjectAndLevel(String projectKey, KpiLevel level, List<IssueKpi> issuesByLevel) {
            Mockito.when(service.getIssuesFromCurrentState(projectKey, environment.getTimezone(), level))
                .thenReturn(issuesByLevel);
        }

        public KpiEnvironment eoIks() {
            return environment;
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
        private IssueKpiDataItemAdapterFactory factory = Mockito.mock(IssueKpiDataItemAdapterFactory.class);

        public IssueKpiDataItemAdapterFactory getComponent() {
            List<Issue> issues = issueBufferService.getIssues();
            Mockito.when(factory.getItems(issues, environment.getTimezone())).thenReturn(getMappedItemAdapters());
            return factory;
        }

        private List<IssueKpiDataItemAdapter> getMappedItemAdapters() {
            return environment.getAllIssueMockers().stream()
                    .filter(m -> test(m))
                    .map(m -> buildAdapter(m))
                    .collect(Collectors.toList());
        }

        private boolean test(IssueKpiMocker m) {
            return m.level() != KpiLevel.UNMAPPED;
        }

        private IssueKpiDataItemAdapter buildAdapter(IssueKpiMocker mocked) {
            return new FakeIssueKpiAdapter(mocked.getReversedTransitions(), mocked.getIssueKey(),mocked.getIssueTypeKpi(), mocked.level());
        }

    }

}
