package objective.taskboard.followup.kpi.extension;

import org.springframework.stereotype.Component;

@Component
public class BugByEnvironmentExtension  implements KpiFrontendExtension {

    @Override
    public String getComponentPath() {
        return "/static/elements/taskboard/kpis/widget-bug-by-environment.html";
    }

}
