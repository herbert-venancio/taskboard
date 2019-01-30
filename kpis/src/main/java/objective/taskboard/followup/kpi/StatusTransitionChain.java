package objective.taskboard.followup.kpi;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class StatusTransitionChain {

    private List<StatusTransition> statuses;
    private Optional<ZonedDateTime> minDate = Optional.empty();
    private Optional<ZonedDateTime> maxDate = Optional.empty();
    private ZoneId timezone;

    public StatusTransitionChain(List<StatusTransition> statuses, ZoneId timezone) {
        this.statuses = statuses;
        this.timezone = timezone;
    }

    public List<StatusTransition> getStatusesAsList() {
        return statuses;
    }

    public StatusTransitionChain getStatusSubChain(Set<String> includedStatuses) {
        List<StatusTransition> chain = statuses.stream()
                .filter(s -> includedStatuses.contains(s.status))
                .collect(Collectors.toList());
        return new StatusTransitionChain(chain, timezone);
    }

    public Optional<ZonedDateTime> getMinimumDate() {
        if (!minDate.isPresent()) {
            minDate = statuses.stream()
                    .map(s -> s.getEnterDate(timezone))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .min(ZonedDateTime::compareTo);
        }
        return minDate;
    }

    public Optional<ZonedDateTime> getMaximumDate() {
        if (!maxDate.isPresent()) {
            maxDate = statuses.stream()
                    .map(s -> s.getExitDate(timezone))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .max(ZonedDateTime::compareTo);
        }
        return maxDate;
    }

    public long getDurationInDays() {
        if (!hasBothDate()) {
            return 0L;
        }
        return DAYS.between(getMinimumDate().get(), getMaximumDate().get()); //NOSONAR
    }

    public long getDurationInDaysEndDateIncluded() {
        if (!hasBothDate()) {
            return 0L;
        }
        return getDurationInDays() + 1;
    }

    public boolean doAllHaveExitDate() {
        return statuses.stream()
            .filter(s -> !s.getExitDate(timezone).isPresent())
            .count() == 0;
    }

    public boolean hasAnyEnterDate() {
        return statuses.stream().anyMatch(s -> s.getEnterDate(timezone).isPresent());
    }

    private boolean hasBothDate() {
        return getMinimumDate().isPresent() && getMaximumDate().isPresent();
    }
}
