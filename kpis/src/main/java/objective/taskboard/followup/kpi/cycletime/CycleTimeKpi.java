package objective.taskboard.followup.kpi.cycletime;

import static java.time.temporal.ChronoUnit.DAYS;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Map<String, Long> getSubCycles() {
        return subCycles.stream()
                .collect(Collectors.toMap(
                        SubCycleKpi::getStatus,
                        SubCycleKpi::getDuration,
                        (u,v) -> { throw new IllegalStateException(String.format("Duplicate key %s", u)); },
                        LinkedHashMap::new));
    }
    @JsonGetter
    public long getCycleTime() {
        Optional<ZonedDateTime> opEnterDate = getEnterDateAsZonedDateTime();
        Optional<ZonedDateTime> opExitDate = getExitDateAsZonedDateTime();
        if (!opEnterDate.isPresent()) {
            return 0L;
        }
        if (!opExitDate.isPresent()) {
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
        public SubCycleKpi(String status, Optional<ZonedDateTime> enterDate, Optional<ZonedDateTime> exitDate) {
            this.status = status;
            this.enterDate = enterDate;
            this.exitDate = exitDate;
        }
        public String getStatus() {
            return status;
        }
        public Optional<ZonedDateTime> getEnterDate() {
            return enterDate;
        }
        public Optional<ZonedDateTime> getExitDate() {
            return exitDate;
        }
        public long getDuration() {
            if (!enterDate.isPresent() || !exitDate.isPresent()) {
                return 0L;
            }
            return DAYS.between(enterDate.get(), exitDate.get());
        }
        @Override
        public String toString() {
            return "SubCycleKpi [status=" + status + ", enterDate=" + enterDate + ", exitDate=" + exitDate + "]";
        }
    }
}
