package objective.taskboard.followup.kpi.extension;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PlannedVsBallparkFrontendExtensions {

    @Bean
    public BasicKpiFrontendExtension plannedVsBallparkFrontendExtension() {
        return new BasicKpiFrontendExtension("/static/elements/taskboard/kpis/widget-planned-x-ballpark.html");
    }

}
