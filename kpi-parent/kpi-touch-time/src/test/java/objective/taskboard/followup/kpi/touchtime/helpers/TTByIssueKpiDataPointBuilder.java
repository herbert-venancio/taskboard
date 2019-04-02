package objective.taskboard.followup.kpi.touchtime.helpers;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;

import java.time.Instant;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import objective.taskboard.followup.kpi.services.KpiExpectedDataPointBuilder;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiDataPoint;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByIssueKpiDataPoint.Stack;

public class TTByIssueKpiDataPointBuilder implements KpiExpectedDataPointBuilder<TouchTimeByIssueKpiDataPoint> {
    private String issueKey;
    private String issueType;
    private String startProgressingDate;
    private String endProgressingDate;
    private List<StackBuilder> stacksBuilders = new LinkedList<>();

    public TTByIssueKpiDataPointBuilder withIssueKey(String issueKey) {
        this.issueKey = issueKey;
        return this;
    }

    public TTByIssueKpiDataPointBuilder withIssueType(String issueType) {
        this.issueType = issueType;
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

    public StackBuilder withStack() {
        return new StackBuilder();
    }

    @Override
    public TouchTimeByIssueKpiDataPoint build() {
        Instant startInstant = parseInstant(startProgressingDate);
        Instant endInstant = parseInstant(endProgressingDate);
        List<Stack> stacks = this.stacksBuilders.stream().map(StackBuilder::build).collect(Collectors.toList());
        return new TouchTimeByIssueKpiDataPoint(issueKey, issueType, startInstant, endInstant, stacks);
    }

    private Instant parseInstant(String date) {
        return parseDateTime(date, "00:00:00", ZoneId.systemDefault()).toInstant();
    }

    public class StackBuilder {

        private String stackName;
        private double effortInHours;

        public StackBuilder withStackName(String stackName) {
            this.stackName = stackName;
            return this;
        }

        public StackBuilder withEffortInHours(double effortInHours) {
            this.effortInHours = effortInHours;
            return this;
        }

        public TTByIssueKpiDataPointBuilder eoS() {
            stacksBuilders.add(this);
            return TTByIssueKpiDataPointBuilder.this;
        }

        private TouchTimeByIssueKpiDataPoint.Stack build() {
            return new TouchTimeByIssueKpiDataPoint.Stack(stackName, effortInHours);
        }
    }
}