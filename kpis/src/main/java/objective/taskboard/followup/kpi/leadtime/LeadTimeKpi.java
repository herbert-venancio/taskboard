package objective.taskboard.followup.kpi.leadtime;

import java.time.Instant;
import java.util.Optional;

public class LeadTimeKpi {
    private String issueKey;
    private String issueType;
    private Instant startDate;
    private Instant endDate;
    private long leadTime;
    private String lastStatus;
    public LeadTimeKpi(String issueKey, String issueType, Optional<Instant> startDate, Optional<Instant> endDate, long leadTime, String lastStatus) {
        this.issueKey = issueKey;
        this.issueType = issueType;
        if (!startDate.isPresent()) {
            throw new IllegalArgumentException("Invalid start date.");
        }
        if (!endDate.isPresent()) {
            throw new IllegalArgumentException("Invalid end date.");
        }
        this.startDate = startDate.get();
        this.endDate = endDate.get();
        this.leadTime = leadTime;
        this.lastStatus = lastStatus;
    }
    public String getIssueKey() {
        return issueKey;
    }
    public String getIssueType() {
        return issueType;
    }
    public Instant getEnterDate() {
        return startDate;
    }
    public Instant getExitDate() {
        return endDate;
    }
    public long getLeadTime() {
        return leadTime;
    }
    public String getLastStatus() {
        return lastStatus;
    }
}
