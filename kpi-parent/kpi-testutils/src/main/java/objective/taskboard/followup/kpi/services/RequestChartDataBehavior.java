package objective.taskboard.followup.kpi.services;

import org.mockito.Mockito;

import objective.taskboard.auth.authorizer.permission.ProjectDashboardOperationalPermission;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.testUtils.ControllerTestUtils.AssertResponse;

public abstract class RequestChartDataBehavior<T> implements DSLSimpleBehaviorWithAsserter<AssertResponse> {
    protected final String projectKey;
    protected String level;
    protected final String zoneId;
    protected final boolean hasPermission;
    protected boolean preventProviderMock;
    protected AssertResponse asserter;
    
    public RequestChartDataBehavior(String projectKey, String level, String zoneId, boolean hasPermission, boolean preventProviderMock) {
        this(projectKey, zoneId, hasPermission, preventProviderMock);
        this.level = level;
    }
    
    public RequestChartDataBehavior(String projectKey, String zoneId, boolean hasPermission, boolean preventProviderMock) {
        this.projectKey = projectKey;
        this.zoneId = zoneId;
        this.hasPermission = hasPermission;
        this.preventProviderMock = preventProviderMock;
    }

    @Override
    public AssertResponse then() {
        return asserter;
    }

    @Override
    public void behave(KpiEnvironment environment) {
        ProjectDashboardOperationalPermission permission = getMockedPermission();
        ProjectService projectService = environment.services().projects().getService();
        doBehave(environment, permission, projectService);
    }

    public abstract void doBehave(KpiEnvironment environment, ProjectDashboardOperationalPermission permission, ProjectService projectService);

    protected ProjectDashboardOperationalPermission getMockedPermission() {
        ProjectDashboardOperationalPermission projectDashboardOperationalPermission = Mockito.mock(ProjectDashboardOperationalPermission.class);
        Mockito.when(projectDashboardOperationalPermission.isAuthorizedFor(projectKey)).thenReturn(hasPermission);
        return projectDashboardOperationalPermission;
    }

    protected abstract T mockProvider(KpiEnvironment environment);
}
