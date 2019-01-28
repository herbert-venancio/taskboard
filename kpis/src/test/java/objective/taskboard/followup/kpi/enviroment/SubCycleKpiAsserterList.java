package objective.taskboard.followup.kpi.enviroment;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.AbstractAssert;

import objective.taskboard.followup.kpi.cycletime.CycleTimeKpi;
import objective.taskboard.followup.kpi.cycletime.CycleTimeKpi.SubCycleKpi;

public class SubCycleKpiAsserterList<T> {

    private List<CycleTimeKpi.SubCycleKpi> subjectList;
    private T fatherContext;

    public SubCycleKpiAsserterList(List<CycleTimeKpi.SubCycleKpi> subjectList, T fatherContext) {
        this.subjectList = subjectList;
        this.fatherContext = fatherContext;
    }

    public SubCycleKpiAsserterList<?>.SubCycleKpiAsserter at(String status) {
        SubCycleKpi subCycle = subjectList.stream()
                                            .filter(s -> status.equals(s.getStatus()))
                                            .findFirst()
                                            .orElseThrow(()->new AssertionError(String.format("SubCycle for status %s not found.",status)));

        return new SubCycleKpiAsserter(subCycle);
    }

    public T eoSKAL() {
        return fatherContext;
    }

    public class SubCycleKpiAsserter extends AbstractAssert<SubCycleKpiAsserter, CycleTimeKpi.SubCycleKpi>{

        public SubCycleKpiAsserter(SubCycleKpi actual) {
            super(actual, SubCycleKpiAsserter.class);
        }

        public SubCycleKpiAsserterList<?> eoSCA(){
            return SubCycleKpiAsserterList.this;
        }

        public SubCycleKpiAsserter hasEnterDate(String date) {
            assertThat(actual.getEnterDate())
                .as("Subycycle %s should have enter date", actual.getStatus())
                .hasValueSatisfying(d -> assertThat(d).isEqualTo(parseDateTime(date, "00:00:00", "America/Sao_Paulo")));
            return this;
        }

        public SubCycleKpiAsserter hasExitDate(String date) {
            assertThat(actual.getExitDate())
                .as("Subycycle %s should have exit date",actual.getStatus())
                .hasValueSatisfying(d -> assertThat(d).isEqualTo(parseDateTime(date, "00:00:00", "America/Sao_Paulo")));
            return this;
        }

        public SubCycleKpiAsserter hasNoDuration() {
            return hasTotalDurationInDays(0l);
        }

        public SubCycleKpiAsserter hasTotalDurationInDays(long value) {
            assertThat(actual.getDuration()).as("Status %s should have a total cycle duration of %d",actual.getStatus(),value).isEqualTo(value);
            return this;
        }

        public SubCycleKpiAsserter hasNoEnterDate() {
            assertThat(actual.getEnterDate()).as("Status %s should not have an enter date",actual.getStatus()).isNotPresent();
            return this;
        }

        public SubCycleKpiAsserter hasNoExitDate() {
            assertThat(actual.getExitDate()).as("Status %s should not have an exit date",actual.getStatus()).isNotPresent();
            return this;
        }

    }



}
