package objective.taskboard.followup.kpi.touchtime;

import java.util.List;

class TouchTimeByIssueKpiDataSet {
    public final List<TouchTimeByIssueKpiDataPoint> points;

    public TouchTimeByIssueKpiDataSet(List<TouchTimeByIssueKpiDataPoint> points) {
         this.points = points;
    }

    @Override
    public String toString() {
        return "TouchTimeChartDataSet [points=" + points + "]";
    }
}
