package objective.taskboard.followup;

import java.time.LocalDate;

class EffortHistoryRow {
    final LocalDate date;
    double sumEffortDone = 0;
    double sumEffortBacklog = 0;

    public EffortHistoryRow(LocalDate date) {
        this.date = date;
    }
}