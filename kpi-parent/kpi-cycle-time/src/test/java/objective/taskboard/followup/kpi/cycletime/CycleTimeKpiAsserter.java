package objective.taskboard.followup.kpi.cycletime;

import static java.util.stream.Collectors.toMap;
import static objective.taskboard.utils.DateTimeUtils.determineTimeZoneId;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.assertj.core.api.Assertions;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.cycletime.CycleTimeKpi.SubCycleKpi;
import objective.taskboard.followup.kpi.properties.KpiCycleTimeProperties;
import objective.taskboard.followup.kpi.services.DSLKpi;
import objective.taskboard.followup.kpi.services.KpiEnvironment;

public class CycleTimeKpiAsserter<T> {

    private CycleTimeKpi subject;
    private T parentContext;

    public CycleTimeKpiAsserter(CycleTimeKpi subject, T parentContext) {
        this.subject = subject;
        this.parentContext = parentContext;
    }

    public static BiFunction<KpiEnvironment, DSLKpi.AsserterFactory, CycleTimeKpiAsserter<DSLKpi.AsserterFactory>> cycleTimeKpi(String pkey) {
        return (environment, factory) -> {
            IssueKpi kpi = environment.eoE().getIssueKpi(pkey);
            CycleTimeKpiFactory cycleTimeKpiFactory = new CycleTimeKpiFactory(
                    environment.getKPIProperties(KpiCycleTimeProperties.class).getCycleTime().toMap(),
                    environment.services().issueColor().getService());
            CycleTimeKpi cKpi = cycleTimeKpiFactory.create(kpi);
            return new CycleTimeKpiAsserter<>(cKpi, factory);
        };
    }

    public T eoCK() {
        return parentContext;
    }

    public CycleTimeKpiAsserter<T> hasTotalCycleTime(long days) {
        assertThat(subject.getCycleTime()).isEqualTo(days);
        return this;
    }

    public CycleTimeKpiAsserter<T> startsAt(String enterDate) {
        assertThat(subject.getEnterDate()).isEqualTo(parseInstant(enterDate));
        return this;
    }

    public CycleTimeKpiAsserter<T> endsAt(String exitDate) {
        assertThat(subject.getExitDate()).isEqualTo(parseInstant(exitDate));
        return this;
    }

    public SubCycleGroupAsserter hasSubCycles() {
        List<SubCycleKpi> subCycles = subject.getSubCycles();
        return new SubCycleGroupAsserter(subCycles);
    }

    private Instant parseInstant(String date) {
        return parseDateTime(date, "00:00:00", determineTimeZoneId("America/Sao_Paulo")).toInstant();
    }

    public class SubCycleGroupAsserter {

        private Map<String,SubCycleAsserter> subCyclesAsserters;

        public SubCycleGroupAsserter(List<SubCycleKpi> subCycles) {
            this.subCyclesAsserters = map(subCycles);
        }

        public SubCycleAsserter subCycle(String status) {
            Assertions.assertThat(subCyclesAsserters).containsKey(status);
            return subCyclesAsserters.get(status);
        }

        private Map<String, SubCycleAsserter> map(List<SubCycleKpi> subCycles) {
            return subCycles.stream()
                        .collect(toMap(SubCycleKpi::getStatus, SubCycleAsserter::new));
        }

        public CycleTimeKpiAsserter<T> eoSC() {
            return CycleTimeKpiAsserter.this;
        }

        public class SubCycleAsserter {
            private SubCycleKpi subject;

            public SubCycleAsserter(SubCycleKpi subCycleKpi) {
                this.subject = subCycleKpi;
            }

            public SubCycleAsserter hasEnterDate(String date) {
                assertThat(subject.getEnterDate())
                    .as("Subycycle %s should have enter date", subject.getStatus())
                    .hasValueSatisfying(d -> assertThat(d).isEqualTo(parseDateTime(date, "00:00:00", "America/Sao_Paulo")));
                return this;
            }

            public SubCycleAsserter hasNoEnterDate() {
                assertThat(subject.getEnterDate())
                    .as("Status %s should not have an enter date", subject.getStatus())
                    .isNotPresent();
                return this;
            }

            public SubCycleAsserter hasExitDate(String date) {
                assertThat(subject.getExitDate())
                    .as("Subycycle %s should have exit date", subject.getStatus())
                    .hasValueSatisfying(d -> assertThat(d).isEqualTo(parseDateTime(date, "00:00:00", "America/Sao_Paulo")));
                return this;
            }

            public SubCycleAsserter hasNoExitDate() {
                assertThat(subject.getExitDate())
                    .as("Status %s should not have an exit date", subject.getStatus())
                    .isNotPresent();
                return this;
            }

            public SubCycleAsserter hasCycleTimeInDays(long days) {
                assertThat(subject.getDuration())
                    .as("Status %s should have a total cycle duration of %d", subject.getStatus(), days)
                    .isEqualTo(days);
                return this;
            }

            public SubCycleAsserter hasNoCycle() {
                return hasCycleTimeInDays(0L);
            }

            public SubCycleAsserter hasColorHex(String colorHex) {
                assertThat(subject.getColor()).isEqualTo(colorHex);
                return this;
            }

            public SubCycleGroupAsserter eoS() {
                return SubCycleGroupAsserter.this;
            }
        }
    }

}
