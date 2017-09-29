package objective.taskboard.domain.converter;

import static java.lang.Integer.parseInt;
import static java.time.temporal.ChronoUnit.DAYS;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.data.Changelog;
import objective.taskboard.domain.Filter;
import objective.taskboard.repository.FilterCachedRepository;

@Component
public class MaxVisibilityDateCalculatorService {
    
    @Autowired
    private FilterCachedRepository filterRepository;

    public Optional<LocalDateTime> calculateVisibleUntil(long status, List<Changelog> all) {
        List<Filter> filters = filterRepository.getCache();
        if (filters.isEmpty())
            return Optional.empty();
        
        Optional<Filter> filter = filters.stream()
                .filter(f->f.getStatusId() == status)
                .filter(f->f.getLimitInDays() != null)
                .findFirst();
        
        if (!filter.isPresent())
            return Optional.empty();
        
        List<Changelog> changelogs = all.stream()
                .filter(c->c.field.equals("status"))
                .filter(c->Integer.parseInt(c.originalTo)==filter.get().getStatusId())
                .sorted((o1,o2) -> o2.timestamp.compareTo(o1.timestamp))
                .collect(Collectors.toList());
        
        if (changelogs.isEmpty())
            return Optional.empty();
        
        return Optional.of(changelogs.get(0).timestamp.plus(-limitInDays(filter.get()), DAYS).toLocalDateTime());
    }
    
    private int limitInDays(Filter f) {
        return parseInt(f.getLimitInDays().replaceAll("[^0-9-]", ""));
    }
}
