package objective.taskboard.followup.kpi.enviroment;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.mockito.Mockito;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;

public class IssueKpiServiceMocker {
    private IssueKpiService service = Mockito.mock(IssueKpiService.class);
    private KpiEnvironment fatherEnvironment;

    public IssueKpiServiceMocker(KpiEnvironment environment) {
        this.fatherEnvironment = environment;
    }

    public IssueKpiService getIssueKpiService() {
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
        Mockito.when(service.getIssuesFromCurrentState(fatherEnvironment.getProjectKey(), fatherEnvironment.getTimezone(), level))
                .thenReturn(issuesByLevel);
    }
    public KpiEnvironment eoIks() {
        return fatherEnvironment;
    }

}
