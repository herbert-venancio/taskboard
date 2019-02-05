package objective.taskboard.extension;

import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class CardboardPolymerExtension implements PolymerComponentExtension {

    @Override
    public String componentPath() {
        return "taskboard/cardboard.html";
    }

    @Override
    public Optional<String> topLevelComponentName() {
        return Optional.of("taskboard-home");
    }
}
