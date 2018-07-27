package objective.taskboard.followup.kpi;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

public class IssueStatusFlow {

    private String pKey;
    private StatusTransitionChain firstStatus;
    private String issueType;

    public IssueStatusFlow(String pKey, String issueType, StatusTransitionChain firstStatus) {
        this.pKey = pKey;
        this.issueType = issueType;
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

}
