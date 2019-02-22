package objective.taskboard.followup.kpi.touchtime;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.jira.ProjectService;

@Service
public class TouchTimeKpiProvider {
    private TouchTimeByWeekKpiStrategyFactory byWeek;
    private TouchTimeByIssueKpiStrategyFactory byIssue;
    private ProjectService projectService;
    private Map<TouchTimeMethod, TouchTimeKpiStrategyFactory<?>> factories = new EnumMap<>(TouchTimeMethod.class);

    @Autowired
    public TouchTimeKpiProvider(
            TouchTimeByWeekKpiStrategyFactory byWeek,
            TouchTimeByIssueKpiStrategyFactory byIssue,
            ProjectService projectService) {
        this.byWeek = byWeek;
        this.byIssue = byIssue;
        this.projectService = projectService;
        initializeFactories();
    }

    private void initializeFactories() {
        factories.put(TouchTimeMethod.BY_WEEK, byWeek);
        factories.put(TouchTimeMethod.BY_ISSUE, byIssue);
        factories.put(TouchTimeMethod.UNKOWN, (kpiLevel, project, timezone) -> {
            throw new IllegalArgumentException("Method invalid");
        });
    }

    public List<?> getDataSet(String methodName, String projectKey, KpiLevel kpiLevel, ZoneId timezone) {
        TouchTimeMethod method = TouchTimeMethod.fromMethod(methodName);

        ProjectFilterConfiguration project = projectService.getTaskboardProjectOrCry(projectKey);
        return factories.get(method).getStrategy(kpiLevel, project, timezone).getDataSet();
    }

    public enum TouchTimeMethod {
        BY_WEEK("byWeek"), BY_ISSUE("byIssue"), UNKOWN("");

        private String method;

        private TouchTimeMethod(String method) {
            this.method = method;
        }

        static TouchTimeMethod fromMethod(String method) {
            return Arrays.asList(TouchTimeMethod.values()).stream()
                    .filter(n -> n.method.equals(method))
                    .findFirst().orElse(UNKOWN);
        }
    }

}
