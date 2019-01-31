package objective.taskboard.followup;

import java.time.LocalDate;
import java.util.function.Supplier;

public interface SynthesisSynchronizer {
    void syncSynthesis(Supplier<FollowUpSnapshot> lazySnapshotProvider, String projectKey, LocalDate date, boolean override);
}