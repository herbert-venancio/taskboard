package objective.taskboard.extension;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ApplicationToolbarService {

    private final List<ApplicationToolbarItem> items;

    @Autowired
    public ApplicationToolbarService(Optional<List<ApplicationToolbarItem>> items) {
        this.items = unmodifiableList(items.orElse(emptyList()));
    }

    public List<ApplicationToolbarItem> getItems() {
        return items.stream()
                .filter(e -> e.isVisible())
                .sorted(comparing(ApplicationToolbarItem::getButtonId))
                .collect(toList());
    }
}
