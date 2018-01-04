package objective.taskboard.followup;

import static java.util.Arrays.asList;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import objective.taskboard.followup.FromJiraRowCalculator.FromJiraRowCalculation;

public class FollowUpDataSnapshotHistory {

    private List<EffortHistoryRow> historyRows;
    
    private final FollowUpDataHistoryRepository historyRepository;
    private final List<String> includeProjects;
    private final ZoneId timezone;
    private final FollowUpDataSnapshot followUpDataEntry;
    private final FromJiraRowCalculator rowCalculator;
    
    public FollowUpDataSnapshotHistory(
            final FollowUpDataHistoryRepository historyRepository, 
            String[] includeProjects, 
            ZoneId timezone, 
            FollowUpDataSnapshot followUpDataEntry, 
            FromJiraRowCalculator rowCalculator) {
                this.historyRepository = historyRepository;
                this.includeProjects = asList(includeProjects);
                this.timezone = timezone;
                this.followUpDataEntry = followUpDataEntry;
                this.rowCalculator = rowCalculator;
    }
    

    private void calculate() {
        if (historyRows != null) return;
        
        this.historyRows = new ArrayList<>();
        
        historyRepository.forEachHistoryEntry(includeProjects, timezone, historyEntry -> {
            historyRows.add(generateEffortHistoryRow(rowCalculator, historyEntry));
        });
        
        historyRows.add(generateEffortHistoryRow(rowCalculator, followUpDataEntry));
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