package objective.taskboard.followup.kpi;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;

import com.google.common.collect.Streams;

import objective.taskboard.utils.Clock;
import objective.taskboard.utils.RangeUtils;

public class IssueKpi {

    private String pKey;
    private Optional<StatusTransition> firstStatus;
    private Optional<IssueTypeKpi> issueType;
    private List<IssueKpi> children = new LinkedList<>();
    private KpiLevel level;
    private Clock clock;
    private Optional<String> clientEnvironment = Optional.empty();

    public IssueKpi(String pKey, Optional<IssueTypeKpi> issueType, KpiLevel level, Optional<StatusTransition> firstStatus, Clock clock) {
        this.pKey = pKey;
        this.issueType = issueType;
        this.level = level;
        this.firstStatus = firstStatus;
        this.clock = clock;
    }

    public boolean isOnStatusOnDay(String status, ZonedDateTime date) {
        Optional<StatusTransition> issueStatus = firstStatus.flatMap(s -> s.givenDate(date));
        return issueStatus.map(s -> s.isStatus(status)).orElse(false);
    }

    public boolean hasTransitedToAnyStatusOnDay(ZonedDateTime date, String... statuses) {
        Stream<DatedStatusTransition> all = getStatusesWithTransition(statuses);
        Optional<LocalDate> earliestTransition = earliestOfStatuses(all);

        return earliestTransition.map(ld -> ld.equals(date.toLocalDate())).orElse(false);
    }

    public String getIssueTypeName() {
        return issueType.map(t -> t.getType()).orElse("Unmapped");
    }

    public Optional<IssueTypeKpi> getIssueType() {
        return issueType;
    }

    @Override
    public String toString() {
        return String.format("[%s]", pKey);
    }

    public String getIssueKey() {
        return pKey;
    }

    public KpiLevel getLevel() {
        return level;
    }

    public void addChild(IssueKpi issueKpi) {
        children.add(issueKpi);

    }

    List<IssueKpi> getChildren() {
        return children;
    }

    Optional<StatusTransition> firstStatus(){
        return firstStatus;
    }

    public Long getEffort(String status) {
        Optional<StatusTransition> statusTransition =  firstStatus.flatMap(f -> f.find(status));
        return statusTransition.map(s -> s.getEffort()).orElse(0l);
    }

    public Long getEffortFromStatusUntilDate(String status, ZonedDateTime dateLimit) {
        Optional<StatusTransition> statusTransition =  firstStatus.flatMap(f -> f.find(status));
        return statusTransition.map(s -> s.getEffortUntilDate(dateLimit)).orElse(0l);
    }

    public long getEffortUntilDate(ZonedDateTime dateLimit) {
        return firstStatus.map(s -> s.collectEffortUntilDate(dateLimit)).orElse(0L);
    }

    public Long getEffortSumInSecondsFromStatusesUntilDate(List<String> statuses, ZonedDateTime dateLimit) {
        return statuses.stream()
                .map(s -> this.getEffortFromStatusUntilDate(s, dateLimit))
                .collect(Collectors.summingLong(effortInSeconds -> effortInSeconds));
    }

    public List<ZonedWorklog> getWorklogFromChildrenTypeId(Long subtaskType) {
        return children.stream()
                .filter(c -> c.issueType.map(type -> type.getId().equals(subtaskType)).orElse(false))
                .map(IssueKpi::collectWorklogs)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public long getEffortSumFromChildrenWithSubtaskTypeId(long subtaskTypeId) {
        return getDecendants()
                .filter(c -> c.issueType.map(type -> type.getId().equals(subtaskTypeId)).orElse(false))
                .map(IssueKpi::collectWorklogs)
                .flatMap(List::stream)
                .mapToLong(ZonedWorklog::getTimeSpentSeconds)
                .sum();
    }

    public Optional<StatusTransition> findStatus(String status) {
        return firstStatus.flatMap(s -> s.find(status));
    }

    public List<ZonedWorklog> getWorklogFromChildrenStatus(String childrenStatus) {
        return children.stream()
                .map(c -> c.findStatus(childrenStatus))
                .flatMap(s -> s.map(x -> x.getWorklogs().stream()).orElse(Stream.empty()))
                .collect(Collectors.toList());
    }

    public Optional<Range<LocalDate>> getDateRangeBasedOnProgressingStatuses(ZoneId timezone) {

        Optional<LocalDate> firstDateOp = firstStatus.flatMap(StatusTransition::firstDateOnProgressing);
        if(!firstDateOp.isPresent())
            return Optional.empty();

        LocalDate firstDate = firstDateOp.get();
        LocalDate now = ZonedDateTime.ofInstant(clock.now(),timezone).toLocalDate();
        LocalDate lastDate = dateAfterLeavingLastProgressingStatus().orElse(now);

        if(firstDate.isAfter(lastDate))
            return Optional.of(RangeUtils.between(firstDate, firstDate));

        return Optional.of(RangeUtils.between(firstDate, lastDate));
    }

    public Optional<LocalDate> dateAfterLeavingLastProgressingStatus() {
        return firstStatus.flatMap(StatusTransition::getDateAfterLeavingLastProgressingStatus);
    }

    public boolean hasCompletedCycle(Set<String> cycleStatuses) {
        StatusTransitionChain cycleStatusesChain = getStatusChain().getStatusSubChain(cycleStatuses);
        return cycleStatusesChain.hasAnyEnterDate() && cycleStatusesChain.doAllHaveExitDate();
    }

    public StatusTransitionChain getStatusChain() {
        List<StatusTransition> statusChain = new LinkedList<>();
        Optional<StatusTransition> current = firstStatus;
        do {
            if (current.isPresent()) {
                StatusTransition status = current.get();
                statusChain.add(status);
                current = status.next;
            }
        } while (current.isPresent());
        return new StatusTransitionChain(statusChain);
    }

    private List<ZonedWorklog> collectWorklogs() {
        return firstStatus.map(StatusTransition::collectWorklog).orElseGet(Collections::emptyList);
    }

    private Stream<DatedStatusTransition> getStatusesWithTransition(String... statuses) {
        if(!firstStatus.isPresent())
            return Stream.empty();

        return Stream.of(statuses)
                .map(s -> firstStatus.get().findWithTransition(s))
                .filter(s -> s.isPresent())
                .map(s -> s.get());
    }

    private Optional<LocalDate> earliestOfStatuses(Stream<DatedStatusTransition> all) {
        Optional<ZonedDateTime> minimum = all.map(s -> s.getDate()).min(Comparator.naturalOrder());
        return minimum.flatMap(zd -> Optional.of(zd.toLocalDate()));
    }

    private Stream<IssueKpi> getDecendants() {
        if (children.isEmpty()) {
            return Stream.empty();
        }
        return Streams.concat(
                children.stream(),
                children.stream().flatMap(IssueKpi::getDecendants)
            );
    }

    public Optional<String> getLastTransitedStatus() {
        Optional<DatedStatusTransition> lastTransitedStatus = firstStatus.flatMap(StatusTransition::lastTransitedStatus);
        return lastTransitedStatus.map(StatusTransition::getStatusName);
    }

    public void setClientEnvironment(Optional<String> clientEnvironment) {
        this.clientEnvironment = clientEnvironment;
    }
    
    public Optional<String> getClientEnvironment() {
        return clientEnvironment;
    }
    
    public boolean isFromClientEnvironment(String environment) {
        return this.clientEnvironment.map(e -> e.equalsIgnoreCase(environment)).orElse(false);
    }
}