package objective.taskboard.followup;

import static java.util.Arrays.asList;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FollowUpDataSnapshotHistory {

    private List<EffortHistoryRow> historyRows;
    
    private final FollowUpDataHistoryRepository historyRepository;
    private final List<String> includeProjects;
    private final ZoneId timezone;
    private final FollowUpDataSnapshot lastSnapshot;
    private final FromJiraRowCalculator rowCalculator;
    private final Optional<String> endDate;
    
    public FollowUpDataSnapshotHistory(
            final FollowUpDataHistoryRepository historyRepository, 
            String[] includeProjects, 
            ZoneId timezone, 
            FollowUpDataSnapshot lastSnapshot, 
            FromJiraRowCalculator rowCalculator,
            String endDate) {
        
        this.historyRepository = historyRepository;
        this.includeProjects = asList(includeProjects);
        this.timezone = timezone;
        this.lastSnapshot = lastSnapshot;
        this.rowCalculator = rowCalculator;
        this.endDate = Optional.ofNullable(endDate);
    }
    
    public FollowUpDataSnapshotHistory(
            final FollowUpDataHistoryRepository historyRepository, 
            String[] includeProjects, 
            ZoneId timezone, 
            FollowUpDataSnapshot lastSnapshot, 
            FromJiraRowCalculator rowCalculator) {
        
        this.historyRepository = historyRepository;
        this.includeProjects = asList(includeProjects);
        this.timezone = timezone;
        this.lastSnapshot = lastSnapshot;
        this.rowCalculator = rowCalculator;
        this.endDate = Optional.empty();
    }

    private void calculate() {
        if (historyRows != null) return;
        
        this.historyRows = new ArrayList<>();
        
        if (endDate.isPresent())
            historyRepository.forEachSnapshot(includeProjects, endDate.get(), timezone, snapshot -> {
                historyRows.add(snapshot.getEffortHistoryRow(rowCalculator));
            });
        else
            historyRepository.forEachSnapshot(includeProjects, timezone, snapshot -> {
                historyRows.add(snapshot.getEffortHistoryRow(rowCalculator));
            });
        
        historyRows.add(lastSnapshot.getEffortHistoryRow(rowCalculator));
    }

    public List<EffortHistoryRow> getHistoryRows() {
        calculate();
        return historyRows;
    }

    public FromJiraRowCalculator getCalculator() {
        return rowCalculator;
    }
}