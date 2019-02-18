package objective.taskboard.followup.kpi.extension;

import org.springframework.stereotype.Component;

@Component
public class ProgressChartFrontendExtension implements KpiFrontendExtension {

    @Override
    public String getComponentPath() {
        return "/static/elements/taskboard/kpis/widget-progress-chart.html";
    }

}
