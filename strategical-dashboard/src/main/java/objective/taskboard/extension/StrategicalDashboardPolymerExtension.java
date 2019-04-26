package objective.taskboard.extension;

import java.util.Optional;

public class StrategicalDashboardPolymerExtension implements PolymerComponentExtension {

    @Override
    public String componentPath() {
        return "strategical-dashboard/";
    }

    @Override
    public Optional<String> topLevelComponentName() {
        return Optional.of("strategical-dashboard");
    }

}
