package objective.taskboard.followup;

import java.util.List;
import java.util.Map;

public class CumulativeFlowDiagramDataSet {
    public final Map<String, List<CumulativeFlowDiagramDataPoint>> dataByStatus;

    public CumulativeFlowDiagramDataSet(Map<String, List<CumulativeFlowDiagramDataPoint>> data) {
        this.dataByStatus = data;
    }
}
