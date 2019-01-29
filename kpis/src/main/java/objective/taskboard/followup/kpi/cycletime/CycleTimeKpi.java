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
    public CycleTimeKpi(String issueKey, String issueType, List<SubCycleKpi> subCycles) {
        this.issueKey = issueKey;
        this.issueType = issueType;
        this.subCycles = subCycles;
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
        Optional<ZonedDateTime> opEnterDate = getEnterDateAsZonedDateTime();
        Optional<ZonedDateTime> opExitDate = getExitDateAsZonedDateTime();
        if (!opEnterDate.isPresent() || !opExitDate.isPresent()) {
            return 0L;
        }
        return DAYS.between(opEnterDate.get(), opExitDate.get()) + 1;
    }
    @JsonGetter
    public Instant getEnterDate() {
        return getEnterDateAsZonedDateTime().map(ZonedDateTime::toInstant).orElse(null);
    }
    @JsonGetter
    public Instant getExitDate() {
        return getExitDateAsZonedDateTime().map(ZonedDateTime::toInstant).orElse(null);
    }
    private Optional<ZonedDateTime> getEnterDateAsZonedDateTime() {
        return subCycles.stream()
                .map(SubCycleKpi::getEnterDate)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(ZonedDateTime::compareTo);
    }
    private Optional<ZonedDateTime> getExitDateAsZonedDateTime() {
        return subCycles.stream()
                .map(SubCycleKpi::getExitDate)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(ZonedDateTime::compareTo);
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
