package objective.taskboard.followup;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import objective.taskboard.followup.FromJiraRowCalculator.FromJiraRowCalculation;

public class FollowUpDataSnapshot {
    private final LocalDate date;
    private final FollowupData followupData;
    private FollowUpDataSnapshotHistory history;

    public FollowUpDataSnapshot(LocalDate date, FollowupData followupData, FollowUpDataSnapshotHistory history) {
        this.date = date;
        this.followupData = followupData;
        this.history = history;
    }

    public FollowUpDataSnapshot(LocalDate date, FollowupData followupData) {
        this(date, followupData, null);
    }

    public LocalDate getDate() {
        return date;
    }

    public FollowupData getData() {
        return followupData;
    }

    public Optional<FollowUpDataSnapshotHistory> getHistory() {
        return Optional.ofNullable(history);
    }
    
    public Optional<EffortHistoryRow> getLatestEffortRow() {
        if (!getHistory().isPresent())
            return Optional.empty();
        
        List<EffortHistoryRow> historyRows = getHistory().get().getHistoryRows();
        return Optional.of(historyRows.get(historyRows.size()-1));
    }

    public void setFollowUpDataEntryHistory(FollowUpDataSnapshotHistory history) {
        this.history = history;
    }
    
    public void forEachRow(Consumer<SnapshotRow> consumer) {
        final FromJiraRowCalculator calculator = getCalculator();
                
        followupData.fromJiraDs.rows.forEach(row -> {
            consumer.accept(new SnapshotRow(row, calculator.calculate(row)));
        });
    }

    private FromJiraRowCalculator getCalculator() {
        if (getHistory().isPresent())
            return getHistory().get().getCalculator();
        
        return new FromJiraRowCalculator(new EmptyFollowupCluster());
    }
    
    public static class SnapshotRow {

        public final FromJiraDataRow rowData;
        public final FromJiraRowCalculation calcutatedData;

        public SnapshotRow(FromJiraDataRow row, FromJiraRowCalculation calculate) {
            this.rowData = row;
            this.calcutatedData = calculate;
        }
        
    }
}