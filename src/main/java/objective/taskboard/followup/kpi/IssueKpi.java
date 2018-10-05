package objective.taskboard.followup.kpi;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class IssueKpi {

    private String pKey;
    private Optional<StatusTransition> firstStatus;
    private String issueType;
    private List<IssueKpi> children = new LinkedList<>();
    private KpiLevel level;

    public IssueKpi(String pKey, String issueType, KpiLevel level, Optional<StatusTransition> firstStatus) {
        this.pKey = pKey;
        this.issueType = issueType;
        this.level = level;
        this.firstStatus = firstStatus;
    }

    public boolean isOnStatusOnDay(String status, ZonedDateTime date) {
        Optional<StatusTransition> issueStatus = firstStatus.map(s -> s.givenDate(date)).orElse(Optional.empty());
        return issueStatus.map(s -> s.isStatus(status)).orElse(false);
    }

    public boolean hasTransitedToAnyStatusOnDay(ZonedDateTime date, String... statuses) {
        Stream<DatedStatusTransition> all = getStatusesWithTransition(statuses);
        Optional<LocalDate> earliestTransition = earliestOfStatuses(all);
        
        return earliestTransition.map(ld -> ld.equals(date.toLocalDate())).orElse(false);
    }

    private Optional<LocalDate> earliestOfStatuses(Stream<DatedStatusTransition> all) {
        Optional<ZonedDateTime> minimum = all.map(s -> s.getDate()).min(Comparator.naturalOrder());
        return minimum.map(zd -> Optional.of(zd.toLocalDate())).orElse(Optional.empty());
    }

    private Stream<DatedStatusTransition> getStatusesWithTransition(String... statuses) {
        if(!firstStatus.isPresent())
            return Stream.empty();
        
        return Stream.of(statuses)
                .map(s -> firstStatus.get().findWithTransition(s))
                .filter(s -> s.isPresent())
                .map(s -> s.get());
    }

    public String getIssueType() {
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

}
