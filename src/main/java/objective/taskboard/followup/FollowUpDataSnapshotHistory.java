package objective.taskboard.followup;

import static java.util.Arrays.asList;

import java.util.List;
import java.util.Optional;

public class FollowUpDataSnapshotHistory {

    private List<EffortHistoryRow> historyRows;
    
    private final FollowUpDataHistoryRepository historyRepository;
    private final List<String> includeProjects;
    private final FollowUpDataSnapshot lastSnapshot;
    private final Optional<String> endDate;
    
    public FollowUpDataSnapshotHistory(
            final FollowUpDataHistoryRepository historyRepository, 
            String[] includeProjects, 
            FollowUpDataSnapshot lastSnapshot,
            String endDate) {
        
        this.historyRepository = historyRepository;
        this.includeProjects = asList(includeProjects);
        this.lastSnapshot = lastSnapshot;
        this.endDate = Optional.ofNullable(endDate);
    }
    
    public FollowUpDataSnapshotHistory(
            final FollowUpDataHistoryRepository historyRepository, 
            String[] includeProjects, 
            FollowUpDataSnapshot lastSnapshot) {
        
        this.historyRepository = historyRepository;
        this.includeProjects = asList(includeProjects);
        this.lastSnapshot = lastSnapshot;
        this.endDate = Optional.empty();
    }

    private void calculate() {
        if (historyRows != null) return;

        this.historyRows = historyRepository.getHistoryRows(includeProjects, endDate, lastSnapshot);
    }

    public List<EffortHistoryRow> getHistoryRows() {
        calculate();
        return historyRows;
    }
}