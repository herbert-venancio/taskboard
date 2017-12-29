package objective.taskboard.followup;

import java.time.LocalDate;

public class FollowUpDataEntry {
    private final LocalDate date;
    private final FollowupData followupData;

    public FollowUpDataEntry(LocalDate date, FollowupData followupData) {
        this.date = date;
        this.followupData = followupData;
    }

    public LocalDate getDate() {
        return date;
    }

    public FollowupData getData() {
        return followupData;
    }
}