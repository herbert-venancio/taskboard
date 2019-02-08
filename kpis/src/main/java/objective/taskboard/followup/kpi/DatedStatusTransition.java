package objective.taskboard.followup.kpi;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    @Override
    public Optional<ZonedDateTime> getEnterDate() {
        if (enterDate.isPresent()) {
            return enterDate;
        }
        Optional<ZonedDateTime> minWorklogsDate = minimumDateFromWorklogs();
        Optional<ZonedDateTime> minNextWorklogsDate = minimumDateFromNextWorklogs();
        Optional<ZonedDateTime> minWorklogDate = min(minWorklogsDate, minNextWorklogsDate);
        enterDate = min(Optional.of(this.date.truncatedTo(ChronoUnit.DAYS)), minWorklogDate);
        return enterDate;
    }

    public ZonedDateTime getDate() {
        return date;
    }

    protected boolean isOnDate(ZonedWorklog worklog) {
        return isSameDate(worklog) || dateIsBefore(worklog);
    }

    private boolean isSameDate(ZonedWorklog worklog) {
        LocalDate worklogDate = worklog.getStarted().toLocalDate();
        return worklogDate.equals(date.toLocalDate());
    }

    boolean isSameDate(DatedStatusTransition other) {
        return this.date.toLocalDate().equals(other.date.toLocalDate());
    }

    @Override
    public Optional<DatedStatusTransition> withDate() {
        return Optional.of(this);
    }

    @Override
    boolean hasAnyNextThatReceivesWorklog(ZonedWorklog worklog) {
        if (!isSameDate(worklog))
            return super.hasAnyNextThatReceivesWorklog(worklog);

        boolean nextWithDateHasNextReceiver = hasNextDatedStatusThatCanReceiveWorklog(worklog);
        if (nextWithDateHasNextReceiver)
            return true;

        boolean nextWithSameDateDontReceive = hasNextDatedStatusThatIsNotProgressingAndSameDay();
        return !nextWithSameDateDontReceive && super.hasAnyNextThatReceivesWorklog(worklog);
    }

    private boolean hasNextDatedStatusThatCanReceiveWorklog(ZonedWorklog worklog) {
        Optional<DatedStatusTransition> nextWithDate = next.flatMap(n -> n.withDate());
        return nextWithDate.map(n -> n.hasNextOnDateThatCouldReceiveWorklog(worklog, true)).orElse(false);
    }

    private boolean hasNextDatedStatusThatIsNotProgressingAndSameDay() {
        Optional<DatedStatusTransition> nextWithDate = next.flatMap(n -> n.withDate());
        return nextWithDate.map(n -> !n.isProgressingStatus && isSameDate(n)).orElse(false);
    }

    @Override
    public Optional<LocalDate> firstDateOnProgressing() {
        if (!this.isProgressingStatus)
            return super.firstDateOnProgressing();

        ZonedDateTime currentDate = this.date;
        List<LocalDate> allDates = collectAllDates();
        allDates.add(currentDate.toLocalDate());
        return allDates.stream().min(Comparator.naturalOrder());
    }

    private List<LocalDate> collectAllDates() {
        return this.getWorklogs().stream()
                .map(ZonedWorklog::getStarted)
                .map(ZonedDateTime::toLocalDate)
                .collect(Collectors.toList());
    }

    public boolean dateIsBefore(ZonedWorklog worklog) {
        LocalDate worklogDate = worklog.getStarted().toLocalDate();
        return worklogDate.isAfter(date.toLocalDate());
    }

    @Override
    public String toString() {
        return "DatedStatusTransition [status=" + super.status + ", date=" + date + ", enterDate=" + enterDate + ", exitDate=" + exitDate + "]";
    }

}
