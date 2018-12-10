package objective.taskboard.followup.kpi.touchTime;

import java.util.Date;

public class TouchTimeChartByWeekDataPoint {
    
    public final Date date;
    public final String status;
    public final double effort;

    public TouchTimeChartByWeekDataPoint(Date date, String status, double effort) {
        this.date = date;
        this.status = status;
        this.effort = effort;
    }

}
