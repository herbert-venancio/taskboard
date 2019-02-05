package objective.taskboard.extension;

import java.util.Optional;

import org.springframework.stereotype.Component;

@Component
public class SizingImportPolymerExtension implements PolymerComponentExtension {

    @Override
    public String componentPath() {
        return "sizing-importer/sizing-import.html";
    }

    @Override
    public Optional<String> topLevelComponentName() {
        return Optional.of("sizing-import");
    }
}
