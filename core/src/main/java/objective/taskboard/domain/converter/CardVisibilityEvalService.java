package objective.taskboard.domain.converter;

import static java.lang.Integer.parseInt;
import static java.time.temporal.ChronoUnit.DAYS;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.data.Changelog;
import objective.taskboard.domain.Filter;
import objective.taskboard.repository.FilterCachedRepository;
import objective.taskboard.utils.Clock;

@Component
public class CardVisibilityEvalService {
    
    @Autowired
    private FilterCachedRepository filterRepository;
    
    @Autowired
    private Clock localDateTimeProvider;
    
    public boolean isStillInVisibleRange(Long status, Instant lastUpdatedDate, List<Changelog> all) {
        Optional<Instant> visibleUntil = calculateVisibleUntil(status, lastUpdatedDate, all);
        if (visibleUntil.isPresent())
            return localDateTimeProvider.now().isBefore(visibleUntil.get());
        
        return true;
    }

    public Optional<Instant> calculateVisibleUntil(Long status, Instant lastUpdatedDate, List<Changelog> all) {
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
                .filter(c->"status".equals(c.field))
                .filter(c->Integer.parseInt(c.originalTo)==filter.get().getStatusId())
                .sorted((o1,o2) -> o2.timestamp.compareTo(o1.timestamp))
                .collect(Collectors.toList());
        
        if (changelogs.isEmpty()) 
            return Optional.of(lastUpdatedDate.plus(-limitInDays(filter.get()), DAYS));
        
        return Optional.of(changelogs.get(0).timestamp.toInstant().plus(-limitInDays(filter.get()), DAYS));
    }
    
    private int limitInDays(Filter f) {
        return parseInt(f.getLimitInDays().replaceAll("[^0-9-]", ""));
    }
}
