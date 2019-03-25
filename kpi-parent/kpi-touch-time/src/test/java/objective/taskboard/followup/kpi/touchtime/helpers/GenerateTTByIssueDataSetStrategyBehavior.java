package objective.taskboard.followup.kpi.touchtime.helpers;

import java.time.ZoneId;
import java.util.List;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.kpi.KpiDataService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.enviroment.KpiEnvironment;
import objective.taskboard.followup.kpi.properties.KpiTouchTimeProperties;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiDataPoint;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiStrategy;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiStrategyFactory;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.properties.JiraProperties;

public class GenerateTTByIssueDataSetStrategyBehavior extends GenerateTTDataSetStrategyBehavior<TTByIssueKpiDataSetAsserter> {

    public GenerateTTByIssueDataSetStrategyBehavior(String projectKey, KpiLevel issueLevel, ZoneId timezone) {
        super(projectKey, issueLevel, timezone);
    }

    @Override
    public TTByIssueKpiDataSetAsserter doBehave(KpiEnvironment environment) {
        KpiTouchTimeProperties kpiProperties = environment.getKPIProperties(KpiTouchTimeProperties.class);
        JiraProperties jiraProperties = environment.getJiraProperties();
        KpiDataService kpiDataService= environment.services().kpiDataService().getService();
        ProjectFilterConfiguration projectConfiguration = environment.services().projects().getProject(getProjectKey());
        MetadataService metadataService = environment.services().metadata().getService();
        TouchTimeByIssueKpiStrategyFactory factory = new TouchTimeByIssueKpiStrategyFactory(kpiProperties, kpiDataService, jiraProperties, metadataService);
        TouchTimeByIssueKpiStrategy subject = factory.getStrategy(getIssueLevel(), projectConfiguration, getTimezone());
        List<TouchTimeByIssueKpiDataPoint> dataSet = subject.getDataSet();
        return new TTByIssueKpiDataSetAsserter(dataSet);
    }
}