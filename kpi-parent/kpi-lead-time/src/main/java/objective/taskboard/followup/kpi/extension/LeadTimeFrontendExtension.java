package objective.taskboard.followup.kpi.extension;

import org.springframework.stereotype.Component;

@Component
public class LeadTimeFrontendExtension implements KpiFrontendExtension {

    @Override
    public String getComponentPath() {
        return "/static/elements/taskboard/kpis/widget-lead-time.html";
    }

}
