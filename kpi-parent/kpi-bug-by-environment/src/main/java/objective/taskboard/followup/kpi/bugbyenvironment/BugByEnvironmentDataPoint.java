package objective.taskboard.followup.kpi.bugbyenvironment;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"date","bugCategory","totalOfBugs"})
public class BugByEnvironmentDataPoint implements Comparable<BugByEnvironmentDataPoint>{
    
    public final Instant date;
    public final String bugCategory;
    public final long totalOfBugs;

    public BugByEnvironmentDataPoint(Instant date, String bugCategory, long totalOfBugs) {
        this.date = date;
        this.bugCategory = bugCategory;
        this.totalOfBugs = totalOfBugs;
    }

    @Override
    public int compareTo(BugByEnvironmentDataPoint o) {
        int dateComparison = date.compareTo(o.date);
        if(dateComparison != 0)
            return dateComparison;
        return bugCategory.compareTo(o.bugCategory);
    }
    
}
