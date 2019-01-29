package objective.taskboard.followup.kpi.enviroment;

import static java.util.stream.Collectors.toMap;
import static objective.taskboard.utils.DateTimeUtils.determineTimeZoneId;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;

import java.time.Instant;
import java.util.Map;
import java.util.Map.Entry;

import org.assertj.core.api.Assertions;

import objective.taskboard.followup.kpi.cycletime.CycleTimeKpi;

public class CycleTimeKpiAsserter<T> {

    private CycleTimeKpi subject;
    private T fatherContext;

    public CycleTimeKpiAsserter(CycleTimeKpi subject, T fatherContext) {
        this.subject = subject;
        this.fatherContext = fatherContext;
    }

    public T eoCK() {
        return fatherContext;
    }

    public CycleTimeKpiAsserter<T> hasTotalCycleTime(long days) {
        Assertions.assertThat(subject.getCycleTime()).isEqualTo(days);
        return this;
    }

    public CycleTimeKpiAsserter<T> startsAt(String enterDate) {
        Assertions.assertThat(subject.getEnterDate()).isEqualTo(parseInstant(enterDate));
        return this;
    }

    public CycleTimeKpiAsserter<T> endsAt(String exitDate) {
        Assertions.assertThat(subject.getExitDate()).isEqualTo(parseInstant(exitDate));
        return this;
    }

    public SubCycleGroupAsserter hasSubCycles() {
        Map<String, Long> subCycles = subject.getSubCycles();
        return new SubCycleGroupAsserter(this, subCycles);
    }

    private Instant parseInstant(String date) {
        return parseDateTime(date, "00:00:00", determineTimeZoneId("America/Sao_Paulo")).toInstant();
    }

    public class SubCycleGroupAsserter {

        private CycleTimeKpiAsserter<T> father;
        private Map<String,SubCycleAsserter> subCyclesAsserters;

        public SubCycleGroupAsserter(CycleTimeKpiAsserter<T> father, Map<String, Long> subCycles) {
            this.father = father;
            this.subCyclesAsserters = map(subCycles);
        }

        public SubCycleAsserter subCycle(String status) {
            Assertions.assertThat(subCyclesAsserters).containsKey(status);
            return subCyclesAsserters.get(status);
        }

        private Map<String, SubCycleAsserter> map(Map<String, Long> subCycles) {
            return subCycles.entrySet().stream()
                        .collect(toMap(Entry::getKey, entry -> mapAsserter(entry)));
        }

        private SubCycleAsserter mapAsserter(Entry<String, Long> entry) {
            Long cycle = entry.getValue();
            return new SubCycleAsserter(cycle);
        }

        public CycleTimeKpiAsserter<T> eoSC() {
            return father;
        }

        public class SubCycleAsserter {
            private Long cycleTime;

            public SubCycleAsserter(Long cycleTime) {
                this.cycleTime = cycleTime;
            }

            public SubCycleGroupAsserter hasCycleTimeInDays(long days) {
                Assertions.assertThat(cycleTime).isEqualTo(days);
                return SubCycleGroupAsserter.this;
            }

            public SubCycleGroupAsserter hasNoCycle() {
                return hasCycleTimeInDays(0L);
            }
        }
    }

}
