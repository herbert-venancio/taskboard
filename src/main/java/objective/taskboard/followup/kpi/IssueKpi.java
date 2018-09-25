package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class IssueKpi {

    private String pKey;
    private StatusTransitionChain firstStatus;
    private String issueType;
    private List<IssueKpi> children = new LinkedList<>();
    private KpiLevel level;

    public IssueKpi(String pKey, String issueType, KpiLevel level, StatusTransitionChain firstStatus) {
        this.pKey = pKey;
        this.issueType = issueType;
        this.level = level;
        this.firstStatus = firstStatus;
    }

    public boolean isOnStatusOnDay(String status, ZonedDateTime date) {
        Optional<StatusTransitionChain> issueStatus = firstStatus.givenDate(date);
        if (!issueStatus.isPresent())
            return false;
        return issueStatus.get().isStatus(status);
    }

    public boolean hasTransitedToAnyStatusOnDay(ZonedDateTime date, String... statuses) {
        Stream<StatusTransitionChain> all = getStatusesWithTransition(statuses);
        Optional<ZonedDateTime> earliestTransition = earliestOfStatuses(all);
        
        return earliestTransition.isPresent() && earliestTransition.get().toLocalDate().equals(date.toLocalDate());
    }

    private Optional<ZonedDateTime> earliestOfStatuses(Stream<StatusTransitionChain> all) {
        return all.map(s -> s.getDate().get()).min(Comparator.naturalOrder());
    }

    private Stream<StatusTransitionChain> getStatusesWithTransition(String... statuses) {
        return Stream.of(statuses)
                .map(s -> firstStatus.find(s))
                .filter(s -> s.getDate().isPresent());
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
