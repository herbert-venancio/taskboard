package objective.taskboard.followup.kpi.extension;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProgressChartFrontendExtensions {

    @Bean
    public BasicKpiFrontendExtension progressChartFrontendExtension() {
        return new BasicKpiFrontendExtension("/static/elements/taskboard/kpis/widget-progress-chart.html");
    }

    @Bean
    public BasicKpiFrontendExtension scopeProgressFrontendExtension() {
        return new BasicKpiFrontendExtension("/static/elements/taskboard/kpis/widget-scope-progress.html");
    }

}
