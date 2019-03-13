package objective.taskboard.followup.kpi.touchtime.helpers;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;

import java.time.Instant;
import java.time.ZoneId;

import objective.taskboard.followup.kpi.enviroment.KpiExpectedDataPointBuilder;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiDataPoint;

public class TTByIssueKpiDataPointBuilder implements KpiExpectedDataPointBuilder<TouchTimeByIssueKpiDataPoint> {
    private String issueKey;
    private String issueType;
    private String issueStatus;
    private double effortInHours;
    private String startProgressingDate;
    private String endProgressingDate;

    public TTByIssueKpiDataPointBuilder withIssueKey(String issueKey) {
        this.issueKey = issueKey;
        return this;
    }

    public TTByIssueKpiDataPointBuilder withIssueType(String issueType) {
        this.issueType = issueType;
        return this;
    }

    public TTByIssueKpiDataPointBuilder withIssueStatus(String issueStatus) {
        this.issueStatus = issueStatus;
        return this;
    }

    public TTByIssueKpiDataPointBuilder withEffortInHours(double effortInHours) {
        this.effortInHours = effortInHours;
        return this;
    }

    public TTByIssueKpiDataPointBuilder withStartProgressingDate(String startProgressingDate) {
        this.startProgressingDate = startProgressingDate;
        return this;
    }

    public TTByIssueKpiDataPointBuilder withEndProgressingDate(String endProgressingDate) {
        this.endProgressingDate = endProgressingDate;
        return this;
    }

    @Override
    public TouchTimeByIssueKpiDataPoint build() {
        Instant startInstant = parseInstant(startProgressingDate);
        Instant endInstant = parseInstant(endProgressingDate);
        return new TouchTimeByIssueKpiDataPoint(issueKey, issueType, issueStatus, effortInHours, startInstant, endInstant);
    }

    private Instant parseInstant(String date) {
        return parseDateTime(date, "00:00:00", ZoneId.systemDefault()).toInstant();
    }
}