package objective.taskboard.followup.kpi.bugbyenvironment;

import java.time.Instant;

public class BugByEnvironmentDataPoint {
    
    public final Instant date;
    public final String bugCategory;
    public final long totalOfBugs;

    public BugByEnvironmentDataPoint(Instant date, String bugCategory, long totalOfBugs) {
        this.date = date;
        this.bugCategory = bugCategory;
        this.totalOfBugs = totalOfBugs;
    }
    
}
