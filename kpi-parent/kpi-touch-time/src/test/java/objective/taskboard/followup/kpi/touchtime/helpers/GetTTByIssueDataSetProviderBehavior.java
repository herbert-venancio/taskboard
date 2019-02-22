package objective.taskboard.followup.kpi.touchtime.helpers;

import java.time.ZoneId;
import java.util.List;

import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.DSLSimpleBehaviorWithAsserter;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.followup.kpi.properties.KpiTouchTimeProperties;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiDataPoint;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiStrategyFactory;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByWeekKpiStrategyFactory;
import objective.taskboard.followup.kpi.touchtime.TouchTimeKpiProvider;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.properties.JiraProperties;

class GetTTByIssueDataSetProviderBehavior implements DSLSimpleBehaviorWithAsserter<TTByIssueKpiDataSetAsserter> {
    private String methodName;
    private String projectKey;
    private KpiLevel kpiLevel;
    private ZoneId timezone;
    private TTByIssueKpiDataSetAsserter asserter;

    GetTTByIssueDataSetProviderBehavior(String methodName, String projectKey, KpiLevel kpiLevel, ZoneId timezone) {
        this.methodName = methodName;
        this.projectKey = projectKey;
        this.kpiLevel = kpiLevel;
        this.timezone = timezone;
    }

    @Override
    public void behave(KpiEnvironment environment) {
        JiraProperties jiraProperties = environment.getJiraProperties();
        KpiTouchTimeProperties touchTimeProperties = environment.getKPIProperties(KpiTouchTimeProperties.class);
        IssueKpiService issueKpiService = environment.services().issueKpi().getService();
        ProjectService projectService = environment.services().projects().getService();
        TouchTimeByWeekKpiStrategyFactory byWeek = new TouchTimeByWeekKpiStrategyFactory(touchTimeProperties, issueKpiService, jiraProperties);
        TouchTimeByIssueKpiStrategyFactory byIssue = new TouchTimeByIssueKpiStrategyFactory(touchTimeProperties, issueKpiService, jiraProperties);
        TouchTimeKpiProvider subject = new TouchTimeKpiProvider(byWeek, byIssue, projectService);
        List<?> dataset = subject.getDataSet(methodName, projectKey, kpiLevel, timezone);
        createAsserter(dataset);
    }

    @SuppressWarnings("unchecked")
    private void createAsserter(List<?> dataset) {
        this.asserter = new TTByIssueKpiDataSetAsserter((List<TouchTimeByIssueKpiDataPoint>) dataset);
    }

    @Override
    public TTByIssueKpiDataSetAsserter then() {
        return asserter;
    }
}