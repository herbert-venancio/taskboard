package objective.taskboard.followup.kpi.extension;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LeadTimeFrontendExtensions {

    @Bean
    public BasicKpiFrontendExtension leadTimeFrontendExtension() {
        return new BasicKpiFrontendExtension("/static/elements/taskboard/kpis/widget-lead-time.html");
    }

}
