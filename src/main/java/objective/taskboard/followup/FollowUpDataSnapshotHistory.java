package objective.taskboard.followup;

import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import objective.taskboard.followup.FromJiraRowCalculator.FromJiraRowCalculation;

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
            historyRepository.forEachHistoryEntry(includeProjects, endDate.get(), timezone, historyEntry -> {
                historyRows.add(generateEffortHistoryRow(rowCalculator, historyEntry));
            });
        else
            historyRepository.forEachHistoryEntry(includeProjects, timezone, historyEntry -> {
                historyRows.add(generateEffortHistoryRow(rowCalculator, historyEntry));
            });
        
        historyRows.add(generateEffortHistoryRow(rowCalculator, lastSnapshot));
    }
    
    private EffortHistoryRow generateEffortHistoryRow(FromJiraRowCalculator rowCalculator, FollowUpDataSnapshot followupDataEntry) {
        EffortHistoryRow historyRow = new EffortHistoryRow(followupDataEntry.getDate());

        followupDataEntry.getData().fromJiraDs.rows.stream().forEach(fromJiraRow -> {
            FromJiraRowCalculation fromJiraRowCalculation = rowCalculator.calculate(fromJiraRow);
            
            historyRow.sumEffortDone += fromJiraRowCalculation.getEffortDone();
            historyRow.sumEffortBacklog += fromJiraRowCalculation.getEffortOnBacklog();
        });

        return historyRow;
    }

    public List<EffortHistoryRow> getHistoryRows() {
        calculate();
        return historyRows;
    }

    static class EffortHistoryRow {
        final LocalDate date;
        double sumEffortDone = 0;
        double sumEffortBacklog = 0;
    
        public EffortHistoryRow(LocalDate date) {
            this.date = date;
        }
    }
}