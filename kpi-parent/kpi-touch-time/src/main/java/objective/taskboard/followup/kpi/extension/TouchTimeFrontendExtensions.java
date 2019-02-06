package objective.taskboard.followup.kpi.extension;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TouchTimeFrontendExtensions {

    @Bean
    public BasicKpiFrontendExtension touchTimeIssuesFrontendExtension() {
        return new BasicKpiFrontendExtension("/static/elements/taskboard/kpis/widget-touch-time-issues.html");
    }

    @Bean
    public BasicKpiFrontendExtension touchTimeWeeklyFrontendExtension() {
        return new BasicKpiFrontendExtension("/static/elements/taskboard/kpis/widget-touch-time-weekly.html");
    }

}
