package objective.taskboard.followup;

import java.time.LocalDate;

public interface SynthesisSynchronizer {
    void syncSynthesis(FollowUpSnapshot snapshot, String projectKey, LocalDate date, boolean override);
}