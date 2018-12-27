package objective.taskboard.followup.kpi.touchTime;

import java.util.List;

class TouchTimeChartDataSet {
    public final List<TouchTimeDataPoint> points;

    public TouchTimeChartDataSet(List<TouchTimeDataPoint> points) {
         this.points = points;
    }

    @Override
    public String toString() {
        return "TouchTimeChartDataSet [points=" + points + "]";
    }
}
