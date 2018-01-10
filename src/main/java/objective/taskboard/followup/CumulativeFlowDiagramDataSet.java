package objective.taskboard.followup;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.List;

public class CumulativeFlowDiagramDataSet {

    public final List<String> lanes;
    public final List<String> types;
    public final List<String> labels;
    @JsonFormat(pattern = "yyyy-MM-dd")
    public final List<Date> dates;
    public final List<CumulativeFlowDiagramDataPoint> data;

    public CumulativeFlowDiagramDataSet(List<String> lanes, List<String> types, List<String> labels, List<Date> dates, List<CumulativeFlowDiagramDataPoint> data) {
        this.lanes = lanes;
        this.types = types;
        this.labels = labels;
        this.dates = dates;
        this.data = data;
    }
}
