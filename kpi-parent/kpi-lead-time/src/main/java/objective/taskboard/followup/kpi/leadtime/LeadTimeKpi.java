package objective.taskboard.followup.kpi.leadtime;

import java.time.Instant;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"issueKey","issueType","leadTime","enterDate","exitDate","lastStatus"})
public class LeadTimeKpi {
    private String issueKey;
    private String issueType;
    private Instant enterDate;
    private Instant exitDate;
    private long leadTime;
    private String lastStatus;
    public LeadTimeKpi(String issueKey, String issueType, Optional<Instant> enterDate, Optional<Instant> exitDate, long leadTime, String lastStatus) {
        this.issueKey = issueKey;
        this.issueType = issueType;
        if (!enterDate.isPresent()) {
            throw new IllegalArgumentException("Invalid enter date.");
        }
        if (!exitDate.isPresent()) {
            throw new IllegalArgumentException("Invalid exit date.");
        }
        this.enterDate = enterDate.get();
        this.exitDate = exitDate.get();
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
        return enterDate;
    }
    public Instant getExitDate() {
        return exitDate;
    }
    public long getLeadTime() {
        return leadTime;
    }
    public String getLastStatus() {
        return lastStatus;
    }
}
