package objective.taskboard.followup.kpi;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;

import objective.taskboard.data.Worklog;
import objective.taskboard.utils.Clock;
import objective.taskboard.utils.RangeUtils;

public class IssueKpi {

    private String pKey;
    private Optional<StatusTransition> firstStatus;
    private Optional<IssueTypeKpi> issueType;
    private List<IssueKpi> children = new LinkedList<>();
    private KpiLevel level;

    public IssueKpi(String pKey, Optional<IssueTypeKpi> issueType, KpiLevel level, Optional<StatusTransition> firstStatus) {
        this.pKey = pKey;
        this.issueType = issueType;
        this.level = level;
        this.firstStatus = firstStatus;
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

    public List<Worklog> getWorklogFromChildren(Long subtaskType) {
        return children.stream()
                .filter(c -> c.issueType.isPresent())
                .filter(c -> c.issueType.get().getId().equals(subtaskType))
                .map(c -> c.collectWorklogs())
                .flatMap(List::stream)
                .collect(Collectors.toList());
        
    }
    
    private List<Worklog> collectWorklogs() {
        return firstStatus.map(s -> s.collectWorklog()).orElseGet(Collections::emptyList);
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

    public Optional<StatusTransition> findStatus(String status) {
        return firstStatus.flatMap(s -> s.find(status));
    }

    public List<Worklog> getWorklogFromChildrenStatus(String childrenStatus) {
        return children.stream()
                .map(c -> c.findStatus(childrenStatus))
                .filter(s -> s.isPresent())
                .map(s -> s.get())
                .map(s -> s.getWorklogs())
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public Optional<Range<ZonedDateTime>> getDateRangeBasedOnProgressinsStatuses(Clock clock,ZoneId timezone) {
        Optional<ZonedDateTime> firstDateOp = firstStatus.flatMap(s -> s.firstDateOnProgressing(timezone));
        if(!firstDateOp.isPresent())
            return Optional.empty();
        
        ZonedDateTime firstDate = firstDateOp.get();
        ZonedDateTime now = ZonedDateTime.ofInstant(clock.now(),timezone);
        ZonedDateTime lastDate = firstStatus.flatMap(s -> s.getDateAfterLeavingLastProgressingStatus()).orElse(now);

        if(firstDate.isAfter(lastDate))
            return Optional.of(RangeUtils.between(firstDate, firstDate));
        
        return Optional.of(RangeUtils.between(firstDate, lastDate));
    }

}