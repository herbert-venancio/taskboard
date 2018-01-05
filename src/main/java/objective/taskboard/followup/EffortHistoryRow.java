package objective.taskboard.followup;

import java.time.LocalDate;

public class EffortHistoryRow {
    public final LocalDate date;
    public double sumEffortDone = 0;
    public double sumEffortBacklog = 0;

    public EffortHistoryRow(LocalDate date) {
        this.date = date;
    }
}