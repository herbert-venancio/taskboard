package objective.taskboard.followup;

import java.time.LocalDate;
import java.util.Optional;

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

    public void setFollowUpDataEntryHistory(FollowUpDataSnapshotHistory history) {
        this.history = history;
    }
}