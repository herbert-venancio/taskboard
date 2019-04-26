package objective.taskboard.monitor;

import static java.util.Arrays.asList;

import java.time.ZoneId;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.domain.Project;
import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.jira.ProjectService;

@Component
class MonitorDataProvider {

    private final ProjectService projectService;
    private final ScopeMonitorCalculator scopeMonitorCalculator;
    private final TimelineMonitorCalculator timelineMonitorCalculator;
    private final CostMonitorCalculator costMonitorCalculator;

    @Autowired
    public MonitorDataProvider(
            ProjectService projectService,
            ScopeMonitorCalculator scopeMonitorCalculator,
            TimelineMonitorCalculator timelineMonitorCalculator,
            CostMonitorCalculator costMonitorCalculator) {
        this.projectService = projectService;
        this.scopeMonitorCalculator = scopeMonitorCalculator;
        this.timelineMonitorCalculator = timelineMonitorCalculator;
        this.costMonitorCalculator = costMonitorCalculator;
    }

    public StrategicalProjectDataSet fromProject(ProjectFilterConfiguration taskboardProject, ZoneId timezone) {
        Project project = projectService.getJiraProjectAsUserOrCry(taskboardProject.getProjectKey());

        StrategicalProjectDataSet dataSet = new StrategicalProjectDataSet();

        dataSet.projectKey = project.getKey();
        dataSet.projectDisplayName = project.getName();

        dataSet.monitors = asList(
                scopeMonitorCalculator.calculate(taskboardProject, timezone),
                timelineMonitorCalculator.calculate(taskboardProject, timezone),
                costMonitorCalculator.calculate(taskboardProject, timezone));

        return dataSet;
    }

}
