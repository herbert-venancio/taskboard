package objective.taskboard.followup.kpi;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class StatusTransitionChain {

    private List<StatusTransition> statuses;
    private Optional<ZonedDateTime> minDate = Optional.empty();
    private Optional<ZonedDateTime> maxDate = Optional.empty();
    private List<StatusTransition> tail;

    public StatusTransitionChain(List<StatusTransition> statuses) {
        this(statuses, Collections.emptyList());
    }

    public StatusTransitionChain(List<StatusTransition> statuses, List<StatusTransition> tail) {
        this.statuses = statuses;
        this.tail = tail;
    }

    public List<StatusTransition> getStatusesAsList() {
        return statuses;
    }

    public StatusTransitionChain getStatusSubChain(Set<String> includedStatuses) {
        int lastSelectedStatusIndex = -1;
        List<StatusTransition> chain = new LinkedList<>();
        for (int i = 0; i < statuses.size(); i++) {
            StatusTransition status = statuses.get(i);
            if (includedStatuses.contains(status.getStatusName())) {
                chain.add(status);
                lastSelectedStatusIndex = i;
            }
        }
        List<StatusTransition> tail2 = Collections.emptyList();
        if (lastSelectedStatusIndex > -1) {
            tail2 = statuses.subList(lastSelectedStatusIndex + 1, statuses.size());
        }
        return new StatusTransitionChain(chain, tail2);
    }

    public Optional<ZonedDateTime> getMinimumDate() {
        if (!minDate.isPresent()) {
            minDate = statuses.stream()
                    .map(StatusTransition::getEnterDate)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .min(ZonedDateTime::compareTo);
        }
        return minDate;
    }

    public Optional<ZonedDateTime> getMaximumDate() {
        if (!maxDate.isPresent()) {
            maxDate = statuses.stream()
                    .map(StatusTransition::getExitDate)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .max(ZonedDateTime::compareTo);
        }
        return maxDate;
    }

    public Optional<ZonedDateTime> getExitDate() {
        long count = tail.stream()
            .map(StatusTransition::getEnterDate)
            .filter(Optional::isPresent)
            .count();
        return count > 0 ? getMaximumDate() : Optional.empty();
    }

    public long getDurationInDays() {
        if (!hasBothDates()) {
            return 0L;
        }
        return DAYS.between(getMinimumDate().get(), getExitDate().get()); //NOSONAR
    }

    public long getDurationInDaysEndDateIncluded() {
        if (!hasBothDates()) {
            return 0L;
        }
        return getDurationInDays() + 1;
    }

    public boolean doAllHaveExitDate() {
        return statuses.stream()
            .filter(s -> !s.getExitDate().isPresent())
            .count() == 0;
    }

    public boolean hasAnyEnterDate() {
        return statuses.stream().anyMatch(s -> s.getEnterDate().isPresent());
    }

    public String getCurrentStatusName() {
        return statuses.stream()
                .filter(s -> s.getEnterDate().isPresent())
                .reduce((first, second) -> second)
                .map(StatusTransition::getStatusName).orElse("NOSTATUS");
    }

    private boolean hasBothDates() {
        return getMinimumDate().isPresent() && getExitDate().isPresent();
    }
}
