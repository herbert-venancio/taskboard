package objective.taskboard.followup.kpi.extension;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WipFrontendExtensions {

    @Bean
    public BasicKpiFrontendExtension wipFrontendExtension() {
        return new BasicKpiFrontendExtension("/static/elements/taskboard/kpis/widget-wip.html");
    }

}
