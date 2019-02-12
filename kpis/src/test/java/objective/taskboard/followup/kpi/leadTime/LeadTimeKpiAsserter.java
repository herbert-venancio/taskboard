package objective.taskboard.followup.kpi.leadTime;

import java.time.Instant;

import org.assertj.core.api.Assertions;

import objective.taskboard.followup.kpi.leadtime.LeadTimeKpi;
import objective.taskboard.utils.DateTimeUtils;

public class LeadTimeKpiAsserter<T> {
    private LeadTimeKpi subject;
    private T parentContext;

    public LeadTimeKpiAsserter(LeadTimeKpi lKpi, T parentContext) {
        this.subject = lKpi;
        this.parentContext = parentContext;
    }

    public LeadTimeKpiAsserter<T> startsAt(String enterDate) {
        Assertions.assertThat(subject.getEnterDate()).isEqualTo(parseInstant(enterDate));
        return this;
    }

    public LeadTimeKpiAsserter<T> endsAt(String exitDate) {
        Assertions.assertThat(subject.getExitDate()).isEqualTo(parseInstant(exitDate));
        return this;
    }

    public LeadTimeKpiAsserter<T> hasTotalLeadTime(long days) {
        Assertions.assertThat(subject.getLeadTime()).isEqualTo(days);
        return this;
    }

    public LeadTimeKpiAsserter<T> hasLastStatus(String statusName) {
        Assertions.assertThat(subject.getLastStatus()).isEqualTo(statusName);
        return this;
    }

    public LeadTimeKpiAsserter<T> hasType(String typeName) {
        Assertions.assertThat(subject.getIssueType()).isEqualTo(typeName);
        return this;
    }

    public T eoLTKA() {
        return parentContext;
    }

    private Instant parseInstant(String date) {
        return DateTimeUtils.parseDateTime(date, "00:00:00", "America/Sao_Paulo").toInstant();
    }

}
