package objective.taskboard.extension;

import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class DashboardPolymerExtension implements PolymerComponentExtension {

    @Override
    public String componentPath() {
        return "dashboard/dashboard-extension.html";
    }

    @Override
    public Optional<String> topLevelComponentName() {
        return Optional.empty();
    }

}
