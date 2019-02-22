package objective.taskboard.followup.kpi.touchtime;

import java.util.List;

public class TouchTimeChartByWeekDataSet {

    public final List<TouchTimeChartByWeekDataPoint> points;

    public TouchTimeChartByWeekDataSet(List<TouchTimeChartByWeekDataPoint> points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return "TouchTimeChartByWeekDataSet [points=" + points + "]";
    }
}
