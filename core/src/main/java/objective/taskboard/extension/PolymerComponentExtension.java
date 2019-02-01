package objective.taskboard.extension;

import java.util.Optional;

public interface PolymerComponentExtension {
    public String componentPath();
    public Optional<String> topLevelComponentName();
}
