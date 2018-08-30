package objective.taskboard.followup;

import java.util.List;

public class WipChartDataSet {
    public final List<WipDataPoint> rows;

    public WipChartDataSet(List<WipDataPoint> rows) {
        this.rows = rows;
    }
}
