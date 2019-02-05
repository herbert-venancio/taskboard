package objective.taskboard.extension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationToolbarService {
    
    private final List<ApplicationToolbarItem> items;
    
    @Autowired
    public ApplicationToolbarService(Optional<List<ApplicationToolbarItem>> items) {
        this.items = Collections.unmodifiableList(items.orElse(Collections.emptyList()));
    }
    
    public List<ApplicationToolbarItem> getItems() {
        return items.stream().filter(e -> e.isVisible()).collect(Collectors.toList());
    }
}
