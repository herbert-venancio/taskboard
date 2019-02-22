package objective.taskboard.followup.kpi.touchtime;

import java.util.List;

public class TouchTimeByWeekKpiDataSet {

    public final List<TouchTimeByWeekKpiDataPoint> points;

    public TouchTimeByWeekKpiDataSet(List<TouchTimeByWeekKpiDataPoint> points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "TouchTimeChartByWeekDataSet [points=" + points + "]";
    }
}
