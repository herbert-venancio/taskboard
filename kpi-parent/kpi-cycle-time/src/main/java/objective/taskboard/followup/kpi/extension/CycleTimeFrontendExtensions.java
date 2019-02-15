package objective.taskboard.followup.kpi.extension;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CycleTimeFrontendExtensions {

    @Bean
    public BasicKpiFrontendExtension cycleTimeFrontendExtension() {
        return new BasicKpiFrontendExtension("/static/elements/taskboard/kpis/widget-cycle-time.html");
    }

}
