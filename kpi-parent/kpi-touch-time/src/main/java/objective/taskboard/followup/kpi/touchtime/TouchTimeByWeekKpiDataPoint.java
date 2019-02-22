package objective.taskboard.followup.kpi.touchtime;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TouchTimeByWeekKpiDataPoint {

    public final Instant date;
    public final String stackName;
    public final double effortInHours;

    public TouchTimeByWeekKpiDataPoint(Instant date, String stackName, double effortInHours) {
        this.date = date;
        this.stackName = stackName;
        this.effortInHours = effortInHours;
    }

    @Override
    public String toString() {
        return "{date=" + ZonedDateTime.ofInstant(date, ZoneId.systemDefault()) + ", stackName=" + stackName + ", effortInHours=" + effortInHours + "}";
    }
}
