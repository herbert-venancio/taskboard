package objective.taskboard.followup.kpi;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import objective.taskboard.data.Worklog;
import objective.taskboard.utils.DateTimeUtils;

public class DatedStatusTransition extends StatusTransition {

    public final ZonedDateTime date;

    public DatedStatusTransition(String status, ZonedDateTime date, boolean isProgressingStatus,Optional<StatusTransition> next) {
        super(status, isProgressingStatus, next);
        this.date = date;
    }

    @Override
    public Optional<DatedStatusTransition> findWithTransition(String status) {
        return this.status.equalsIgnoreCase(status) ? Optional.of(this) : super.findWithTransition(status);
    }

    @Override
    public Optional<StatusTransition> givenDate(ZonedDateTime date) {

        if (next.isPresent() && next.get().isWithinDate(date))
            return next.get().givenDate(date);

        if (this.isWithinDate(date))
            return Optional.of(this);

        return Optional.empty();
    }

    @Override
    public boolean isWithinDate(ZonedDateTime date) {
        return !this.date.toLocalDate().isAfter(date.toLocalDate());
    }

    public ZonedDateTime getDate() {
        return date;
    }

    protected boolean isOnDate(Worklog worklog) {
        return isSameDate(worklog) || dateIsBefore(worklog);
    }

    private boolean isSameDate(Worklog worklog) {
        ZonedDateTime worklogZonedDateTime = DateTimeUtils.get(worklog.started, date.getZone());
        if (worklogZonedDateTime == null)
            return false;

        LocalDate worklogDate = worklogZonedDateTime.toLocalDate();
        return worklogDate.equals(date.toLocalDate());
    }

    boolean isSameDate(DatedStatusTransition other) {
        return this.date.toLocalDate().equals(other.date.toLocalDate());
    }

    public Optional<DatedStatusTransition> withDate() {
        return Optional.of(this);
    }

    boolean hasAnyNextThatReceivesWorklog(Worklog worklog) {
        if (!isSameDate(worklog))
            return super.hasAnyNextThatReceivesWorklog(worklog);

        boolean nextWithDateHasNextReceiver = hasNextDatedStatusThatCanReceiveWorklog(worklog);
        if (nextWithDateHasNextReceiver)
            return true;

        boolean nextWithSameDateDontReceive = hasNextDatedStatusThatIsNotProgressingAndSameDay();
        return !nextWithSameDateDontReceive && super.hasAnyNextThatReceivesWorklog(worklog);
    }

    private boolean hasNextDatedStatusThatCanReceiveWorklog(Worklog worklog) {
        Optional<DatedStatusTransition> nextWithDate = next.flatMap(n -> n.withDate());
        return nextWithDate.map(n -> n.hasNextOnDateThatCouldReceiveWorklog(worklog, true)).orElse(false);
    }

    private boolean hasNextDatedStatusThatIsNotProgressingAndSameDay() {
        Optional<DatedStatusTransition> nextWithDate = next.flatMap(n -> n.withDate());
        return nextWithDate.map(n -> !n.isProgressingStatus && isSameDate(n)).orElse(false);
    }

    @Override
    public Optional<ZonedDateTime> firstDateOnProgressing(ZoneId timezone) {
        if (!this.isProgressingStatus)
            return super.firstDateOnProgressing(timezone);

        ZonedDateTime currentDate = this.date;
        List<ZonedDateTime> allDates = collectAllDates(currentDate.getZone());
        allDates.add(currentDate);
        return allDates.stream().min(Comparator.naturalOrder());
    }

    private List<ZonedDateTime> collectAllDates(ZoneId timezone) {
        return this.getWorklogs().stream()
                .map(w -> DateTimeUtils.get(w.started, timezone))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public boolean dateIsBefore(Worklog worklog) {
        ZonedDateTime worklogZonedDateTime = DateTimeUtils.get(worklog.started, date.getZone());
        if (worklogZonedDateTime == null)
            return false;

        LocalDate worklogDate = worklogZonedDateTime.toLocalDate();

        return worklogDate.isAfter(date.toLocalDate());
    }

}
