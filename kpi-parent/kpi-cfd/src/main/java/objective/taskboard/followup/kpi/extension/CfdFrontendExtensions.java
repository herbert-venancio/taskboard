package objective.taskboard.followup.kpi.extension;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CfdFrontendExtensions {

    @Bean
    public BasicKpiFrontendExtension cfdFrontendExtension() {
        return new BasicKpiFrontendExtension("/static/elements/taskboard/kpis/widget-cfd.html");
    }

}
