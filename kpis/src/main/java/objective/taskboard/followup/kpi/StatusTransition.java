package objective.taskboard.followup.kpi;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eclipse.collections.impl.block.factory.Comparators;

import com.google.common.base.Function;

import objective.taskboard.data.Worklog;
import objective.taskboard.utils.DateTimeUtils;

public class StatusTransition {

    protected final String status;
    protected final Optional<StatusTransition> next;
    protected final boolean isProgressingStatus;
    private List<Worklog> worklogs = new LinkedList<>();
    protected Optional<ZonedDateTime> enterDate = Optional.empty();
    protected Optional<ZonedDateTime> exitDate = Optional.empty();

    public StatusTransition(String status, boolean isProgressingStatus, Optional<StatusTransition> next) {
        this.status = status;
        this.next = next;
        this.isProgressingStatus = isProgressingStatus;
    }

    public String getStatusName() {
        return status;
    }

    public Optional<StatusTransition> givenDate(ZonedDateTime date) {
        return next.map(n -> n.givenDate(date)).orElse(Optional.empty());
    }

    public boolean isWithinDate(ZonedDateTime date) {
        return next.map(n -> n.isWithinDate(date)).orElse(false);
    }

    public boolean isStatus(String status) {
        return this.status.equals(status);
    }

    public Optional<DatedStatusTransition> findWithTransition(String status) {
        return next.map(n -> n.findWithTransition(status)).orElse(Optional.empty());
    }

    public void putWorklog(Worklog worklog) {
        this.worklogs.add(worklog);
    }

    boolean isProgressingStatus() {
        return isProgressingStatus;
    }

    boolean hasAnyNextThatReceivesWorklog(Worklog worklog) {
        if (hasNextProgressingStatusThatReceivesWorklog(worklog))
            return true;

        Optional<DatedStatusTransition> nextWithDate = next.flatMap(n -> n.withDate());

        boolean nextIsOnDate = nextWithDate.map(n -> n.isOnDate(worklog)).orElse(false);
        boolean nextReceiveWorklog = nextWithDate.map(n -> n.isProgressingStatus || n.hasNextOnDateThatCouldReceiveWorklog(worklog, true)).orElse(false);

        return nextIsOnDate && nextReceiveWorklog;
    }

    boolean hasNextProgressingStatusThatReceivesWorklog(Worklog worklog) {
        if(hasFutureProgressingStatusBeforeNonProgressingReceiver(worklog))
            return true;

        Optional<DatedStatusTransition> nextWithDate = withDate();
        boolean nextIsProgressing = nextWithDate.map(n -> n.isProgressingStatus).orElse(false);
        boolean nextHasNextProgressing = nextWithDate.map(n -> n.hasNextProgressing()).orElse(false);
        boolean worklogIsAfterNext = nextWithDate.map( n -> n.dateIsBefore(worklog)).orElse(false);

        return !nextIsProgressing && nextHasNextProgressing && worklogIsAfterNext;
    }

    boolean hasFutureProgressingStatusBeforeNonProgressingReceiver(Worklog worklog) {
        Optional<StatusTransition> nextProgressing = next.flatMap(n-> n.whichIsProgressing());
        return nextProgressing.map(n -> n.hasNextOnDateThatCouldReceiveWorklog(worklog, false)).orElse(false);
    }

    public Long getEffort() {
        return worklogs.stream().mapToLong(w -> Long.valueOf(w.timeSpentSeconds)).reduce(Long::sum).orElse(0);
    }

    public Long getEffortUntilDate(ZonedDateTime dateLimit) {
        ZoneId zone = dateLimit.getZone();
        LocalDate localDateLimit = dateLimit.toLocalDate();
        return worklogs.stream()
                .filter(w -> !DateTimeUtils.toLocalDate(w.started, zone).isAfter(localDateLimit))
                .mapToLong(w -> Long.valueOf(w.timeSpentSeconds)).reduce(Long::sum).orElse(0);
    }

    public long collectEffortUntilDate(ZonedDateTime dateLimit) {
        return next.map(s -> s.collectEffortUntilDate(dateLimit)).orElse(0L) + getEffortUntilDate(dateLimit);
    }

    public Optional<DatedStatusTransition> withDate() {
        return next.flatMap(n -> n.withDate());
    }

    protected Optional<ZonedDateTime> minimumDateFromWorklogs(ZoneId timezone) {
        Optional<ZonedDateTime> minWorklogDate = this.worklogs.stream()
                .map(w -> DateTimeUtils.get(w.started, timezone))
                .map(d -> d.truncatedTo(ChronoUnit.DAYS))
                .min(Comparators.naturalOrder());
        return minWorklogDate;
    }

    protected Optional<ZonedDateTime> maximumDateFromWorklogs(ZoneId timezone) {
        Optional<ZonedDateTime> maxWorklogDate = this.worklogs.stream()
                .map(w -> DateTimeUtils.get(w.started, timezone))
                .map(d -> d.truncatedTo(ChronoUnit.DAYS))
                .max(Comparators.naturalOrder());
        return maxWorklogDate;
    }

    protected Optional<ZonedDateTime> minimumDateFromNextWorklogs(ZoneId timezone) {
        if (!next.isPresent()) {
            return Optional.empty();
        }
        StatusTransition nextStatus = next.get();
        Optional<ZonedDateTime> myMinDate = minimumDateFromWorklogs(timezone);
        Optional<ZonedDateTime> minDateFollowingStatuses = nextStatus.minimumDateFromNextWorklogs(timezone);
        return min(myMinDate, minDateFollowingStatuses);
    }

    private boolean isProgressingWithWorklogs() {
        return isProgressingStatus && !this.worklogs.isEmpty();
    }

    public Optional<StatusTransition> whichIsProgressing() {
        return this.isProgressingStatus() ? Optional.of(this) : flatNext(n -> n.whichIsProgressing());
    }

    protected boolean hasNextOnDateThatCouldReceiveWorklog(Worklog worklog, boolean couldReceive) {
        Optional<DatedStatusTransition> nextWithDate = next.flatMap(n -> n.withDate());
        return nextWithDate.map(n -> (n.isProgressingStatus == couldReceive) && n.isOnDate(worklog)).orElse(false);
    }

    public Optional<StatusTransition> find(String status) {
        return this.status.equalsIgnoreCase(status)? Optional.of(this) : flatNext(n->n.find(status));
    }

    Optional<StatusTransition> flatNext(Function<? super StatusTransition, Optional<StatusTransition>> mapper) {
        return next.flatMap(mapper);
    }

    List<Worklog> getWorklogs(){
        return this.worklogs;
    }

    public List<Worklog> collectWorklog() {
        List<Worklog> allWorklogs = next.map(n -> n.collectWorklog()).orElseGet(LinkedList::new);
        allWorklogs.addAll(this.worklogs);
        return allWorklogs;
    }

    public Optional<LocalDate> firstDateOnProgressing(ZoneId timezone){
        if(isProgressingWithWorklogs())
            return minimumDateFromWorklogs(timezone).map(ZonedDateTime::toLocalDate);
        return next.flatMap(n -> n.firstDateOnProgressing(timezone));
    }

    public Optional<LocalDate> getDateAfterLeavingLastProgressingStatus() {
        Optional<StatusTransition> lastProgressingStatusOp = flatNext(s -> s.getLastProgressingStatus());
        if(!lastProgressingStatusOp.isPresent())
            return Optional.empty();
        StatusTransition lastProgressingStatus = lastProgressingStatusOp.get();

        Optional<DatedStatusTransition> nextWithDate = lastProgressingStatus.next.flatMap(n -> n.withDate());
        return nextWithDate.flatMap(s -> Optional.of(s.getDate().toLocalDate()));
    }

    private Optional<StatusTransition> getLastProgressingStatus() {
        Optional<StatusTransition> hasNext = flatNext(s -> s.getLastProgressingStatus());
        if(hasNext.isPresent())
            return hasNext;

        return isProgressingStatus ? Optional.of(this) : Optional.empty();
    }

    protected boolean hasNextProgressing() {
        boolean nextIsProgressing = next.map(n -> n.isProgressingStatus).orElse(false);
        if(nextIsProgressing)
            return true;
        return next.map(n -> n.hasNextProgressing()).orElse(false);
    }

    public Optional<ZonedDateTime> getEnterDate(ZoneId timezone) {
        if (enterDate.isPresent()) {
            return enterDate;
        }
        enterDate = minimumDateFromWorklogs(timezone);
        return enterDate;
    }

    public Optional<ZonedDateTime> getExitDate(ZoneId timezone) {
        if (exitDate.isPresent()) {
            return exitDate;
        }
        if (!next.isPresent()) {
            return Optional.empty();
        }
        StatusTransition nextStatus = next.get();
        Optional<ZonedDateTime> nextStatusEnterDate = nextStatus.getEnterDate(timezone);
        if (!nextStatusEnterDate.isPresent()) {
            return nextStatus.getExitDate(timezone);
        }
        exitDate = max(maximumDateFromWorklogs(timezone), nextStatusEnterDate);
        return exitDate;
    }

    protected Optional<ZonedDateTime> min(Optional<ZonedDateTime> opDate1, Optional<ZonedDateTime> opDate2) {
        if (!opDate2.isPresent()) {
            return opDate1;
        }
        if (!opDate1.isPresent()) {
            return opDate2;
        }
        return opDate1.get().isBefore(opDate2.get())? opDate1 : opDate2;
    }

    protected Optional<ZonedDateTime> max(Optional<ZonedDateTime> opDate1, Optional<ZonedDateTime> opDate2) {
        if (!opDate2.isPresent()) {
            return opDate1;
        }
        if (!opDate1.isPresent()) {
            return opDate2;
        }
        return opDate1.get().isAfter(opDate2.get())? opDate1 : opDate2;
    }

    @Override
    public String toString() {
        return "StatusTransition [status=" + status + ", isProgressingStatus=" + isProgressingStatus + ", worklogs="
                + worklogs + ", enterDate=" + enterDate + ", exitDate=" + exitDate + "]";
    }
}
