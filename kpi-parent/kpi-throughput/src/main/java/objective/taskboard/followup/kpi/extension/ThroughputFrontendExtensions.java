package objective.taskboard.followup.kpi.extension;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThroughputFrontendExtensions {

    @Bean
    public BasicKpiFrontendExtension throughputFrontendExtension() {
        return new BasicKpiFrontendExtension("/static/elements/taskboard/kpis/widget-throughput.html");
    }

}
