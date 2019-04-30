package objective.taskboard.domain.converter;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.data.Changelog;
import objective.taskboard.data.IssuesConfiguration;
import objective.taskboard.filter.LaneService;
import objective.taskboard.utils.Clock;

@Component
public class CardVisibilityEvalService {
    
    private LaneService laneService;
    
    private Clock localDateTimeProvider;

    @Autowired
    public CardVisibilityEvalService(LaneService laneService, Clock localDateTimeProvider) {
        this.laneService = laneService;
        this.localDateTimeProvider = localDateTimeProvider;
    }
    
    public boolean isStillInVisibleRange(Long status, Instant lastUpdatedDate, List<Changelog> all) {
        Optional<Instant> visibleUntil = calculateVisibleUntil(status, lastUpdatedDate, all);
        if (visibleUntil.isPresent())
            return localDateTimeProvider.now().isBefore(visibleUntil.get());
        
        return true;
    }

    public Optional<Instant> calculateVisibleUntil(Long status, Instant lastUpdatedDate, List<Changelog> all) {
        List<IssuesConfiguration> filters = laneService.getFilters();
        if (filters.isEmpty())
            return Optional.empty();

        Optional<IssuesConfiguration> filter = filters.stream()
                .filter(f->f.getStatus() == status)
                .filter(f->f.getLimitInDays() != null)
                .findFirst();

        if (!filter.isPresent())
            return Optional.empty();

        List<Changelog> changelogs = all.stream()
                .filter(c->"status".equals(c.field))
                .filter(c->Integer.parseInt(c.originalTo)==filter.get().getStatus())
                .sorted((o1,o2) -> o2.timestamp.compareTo(o1.timestamp))
                .collect(Collectors.toList());

        int limitInDays = filter.get().getLimitInDays();

        if (changelogs.isEmpty())
            return Optional.of(lastUpdatedDate.plus(-limitInDays, DAYS));

        return Optional.of(changelogs.get(0).timestamp.toInstant().plus(-limitInDays, DAYS));
    }
}
