package objective.taskboard.followup.kpi.leadTime;

import static objective.taskboard.utils.DateTimeUtils.determineTimeZoneId;
import static objective.taskboard.utils.DateTimeUtils.parseDateTime;

import java.time.Instant;

import org.assertj.core.api.Assertions;

import objective.taskboard.followup.kpi.leadtime.LeadTimeKpi;

public class LeadTimeKpiAsserter {
    private LeadTimeKpi subject;

    public LeadTimeKpiAsserter(LeadTimeKpi lKpi) {
        this.subject = lKpi;
    }

    public LeadTimeKpiAsserter startsAt(String enterDate) {
        Assertions.assertThat(subject.getEnterDate()).isEqualTo(parseInstant(enterDate));
        return this;
    }

    public LeadTimeKpiAsserter endsAt(String exitDate) {
        Assertions.assertThat(subject.getExitDate()).isEqualTo(parseInstant(exitDate));
        return this;
    }

    public LeadTimeKpiAsserter hasTotalLeadTime(long days) {
        Assertions.assertThat(subject.getLeadTime()).isEqualTo(days);
        return this;
    }

    public LeadTimeKpiAsserter hasLastStatus(String statusName) {
        Assertions.assertThat(subject.getLastStatus()).isEqualTo(statusName);
        return this;
    }

    public LeadTimeKpiAsserter hasType(String typeName) {
        Assertions.assertThat(subject.getIssueType()).isEqualTo(typeName);
        return this;
    }

    private Instant parseInstant(String date) {
        return parseDateTime(date, "00:00:00", determineTimeZoneId("America/Sao_Paulo")).toInstant();
    }

}
