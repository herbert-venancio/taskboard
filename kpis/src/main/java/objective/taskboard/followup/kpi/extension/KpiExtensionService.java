package objective.taskboard.followup.kpi.extension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class KpiExtensionService {

    private final List<KpiFrontendExtension> items;

    @Autowired
    public KpiExtensionService(Optional<List<KpiFrontendExtension>> items) {
        this.items = Collections.unmodifiableList(items.orElse(Collections.emptyList()));
    }

    public List<KpiFrontendExtension> getItems() {
        return items;
    }
}
