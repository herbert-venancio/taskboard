package objective.taskboard.followup.kpi.extension;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ScopeByTypeFrontendExtensions {

    @Bean
    public BasicKpiFrontendExtension scopeByTypeFrontendExtension() {
        return new BasicKpiFrontendExtension("/static/elements/taskboard/kpis/widget-scope-by-type.html");
    }

}
