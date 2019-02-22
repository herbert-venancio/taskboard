package objective.taskboard.followup.kpi.touchtime.helpers;

import java.time.ZoneId;
import java.util.List;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.followup.kpi.properties.KpiTouchTimeProperties;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByWeekKpiDataPoint;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByWeekKpiStrategy;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByWeekKpiStrategyFactory;
import objective.taskboard.jira.properties.JiraProperties;

public class GenerateTTByWeekDataSetStrategyBehavior extends GenerateTTDataSetStrategyBehavior<TTByWeekKpiDataSetAsserter> {

    public GenerateTTByWeekDataSetStrategyBehavior(String projectKey, KpiLevel issueLevel, ZoneId timezone) {
        super(projectKey, issueLevel, timezone);
    }

    @Override
    public TTByWeekKpiDataSetAsserter doBehave(KpiEnvironment environment) {
        KpiTouchTimeProperties kpiProperties = environment.getKPIProperties(KpiTouchTimeProperties.class);
        JiraProperties jiraProperties = environment.getJiraProperties();
        IssueKpiService issueKpiService = environment.services().issueKpi().getService();
        ProjectFilterConfiguration projectConfiguration = environment.services().projects().getProject(getProjectKey());

        TouchTimeByWeekKpiStrategyFactory factory = new TouchTimeByWeekKpiStrategyFactory(kpiProperties, issueKpiService, jiraProperties);
        TouchTimeByWeekKpiStrategy subject = factory.getStrategy(getIssueLevel(), projectConfiguration, getTimezone());
        List<TouchTimeByWeekKpiDataPoint> dataset = subject.getDataSet();
        return new TTByWeekKpiDataSetAsserter(dataset);
    }
}