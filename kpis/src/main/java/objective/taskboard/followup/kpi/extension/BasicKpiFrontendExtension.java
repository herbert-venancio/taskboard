package objective.taskboard.followup.kpi.extension;

public class BasicKpiFrontendExtension implements KpiFrontendExtension {

    private final String componentPath;

    public BasicKpiFrontendExtension(String componentPath) {
        this.componentPath = componentPath;
    }

    @Override
    public String getComponentPath() {
        return componentPath;
    }
}
