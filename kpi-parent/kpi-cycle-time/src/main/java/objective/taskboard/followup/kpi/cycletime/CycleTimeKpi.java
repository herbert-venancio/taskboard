package objective.taskboard.followup.kpi.cycletime;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"issueKey","issueType","cycleTime","enterDate","exitDate","subCycles"})
public class CycleTimeKpi {
    private String issueKey;
    private String issueType;
    private List<SubCycleKpi> subCycles;
    private Instant enterDate;
    private Instant exitDate;
    private long duration;
    public CycleTimeKpi(String issueKey, String issueType, Instant enterDate, Instant exitDate, long duration, List<SubCycleKpi> subCycles) {
        this.issueKey = issueKey;
        this.issueType = issueType;
        this.subCycles = subCycles;
        this.enterDate = enterDate;
        this.exitDate = exitDate;
        this.duration = duration;
    }
    @JsonGetter
    public String getIssueKey() {
        return issueKey;
    }
    @JsonGetter
    public String getIssueType() {
        return issueType;
    }
    @JsonGetter
    public List<SubCycleKpi> getSubCycles() {
        return subCycles;
    }
    @JsonGetter
    public long getCycleTime() {
        return duration;
    }
    @JsonGetter
    public Instant getEnterDate() {
        return enterDate;
    }
    @JsonGetter
    public Instant getExitDate() {
        return exitDate;
    }
    @Override
    public String toString() {
        return "CycleTimeKpi [issueKey=" + issueKey + ", issueType=" + issueType + ", subCycles=" + subCycles + "]";
    }
    public static class SubCycleKpi {
        private String status;
        private Optional<ZonedDateTime> enterDate;
        private Optional<ZonedDateTime> exitDate;
        private String color;
        public SubCycleKpi(String status, Optional<ZonedDateTime> enterDate, Optional<ZonedDateTime> exitDate, String color) {
            this.status = status;
            this.enterDate = enterDate;
            this.exitDate = exitDate;
            this.color = color;
        }
        @JsonGetter
        public String getStatus() {
            return status;
        }
        Optional<ZonedDateTime> getEnterDate() {
            return enterDate;
        }
        Optional<ZonedDateTime> getExitDate() {
            return exitDate;
        }
        @JsonGetter
        public long getDuration() {
            if (!enterDate.isPresent() || !exitDate.isPresent()) {
                return 0L;
            }
            return DAYS.between(enterDate.get(), exitDate.get());
        }
        @JsonGetter
        public String getColor() {
            return color;
        }
        @Override
        public String toString() {
            return "SubCycleKpi [status=" + status + ", enterDate=" + enterDate + ", exitDate=" + exitDate + "]";
        }
    }
}
