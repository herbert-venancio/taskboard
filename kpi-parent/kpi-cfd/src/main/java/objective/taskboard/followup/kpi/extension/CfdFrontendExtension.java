package objective.taskboard.followup.kpi.extension;

import org.springframework.stereotype.Component;

@Component
public class CfdFrontendExtension implements KpiFrontendExtension {

    @Override
    public String getComponentPath() {
        return "/static/elements/taskboard/kpis/widget-cfd.html";
    }

}
