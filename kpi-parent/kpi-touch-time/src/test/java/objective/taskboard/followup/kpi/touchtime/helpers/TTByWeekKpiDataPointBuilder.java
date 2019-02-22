package objective.taskboard.followup.kpi.touchtime.helpers;

import static objective.taskboard.utils.DateTimeUtils.parseDateTime;

import java.time.Instant;
import java.time.ZoneId;

import objective.taskboard.followup.kpi.enviroment.KpiExpectedDataPointBuilder;
import objective.taskboard.followup.kpi.touchtime.TouchTimeByWeekKpiDataPoint;

public class TTByWeekKpiDataPointBuilder implements KpiExpectedDataPointBuilder<TouchTimeByWeekKpiDataPoint> {
    private String stackName;
    private String date;
    private double effortInHours;

    public TTByWeekKpiDataPointBuilder withStackName(String stackName) {
        this.stackName = stackName;
        return this;
    }

    public TTByWeekKpiDataPointBuilder withNoEffort() {
        return withEffortInHours(0);
    }

    public TTByWeekKpiDataPointBuilder withEffortInHours(double effortInHours) {
        this.effortInHours = effortInHours;
        return this;
    }

    public TTByWeekKpiDataPointBuilder withDate(String date) {
        this.date = date;
        return this;
    }

    @Override
    public TouchTimeByWeekKpiDataPoint build() {
        Instant dateInstant = parseInstant(date);
        return new TouchTimeByWeekKpiDataPoint(dateInstant, stackName, effortInHours);
    }

    private Instant parseInstant(String date) {
        return parseDateTime(date, "00:00:00", ZoneId.systemDefault()).toInstant();
    }
}