package objective.taskboard.extension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PolymerExtensionService {
    private final List<PolymerComponentExtension> items;

    @Autowired
    public PolymerExtensionService(Optional<List<PolymerComponentExtension>> items) {
        this.items = Collections.unmodifiableList(items.orElse(Collections.emptyList()));
    }

    public List<PolymerComponentExtension> getItems() {
        return items;
    }
}
