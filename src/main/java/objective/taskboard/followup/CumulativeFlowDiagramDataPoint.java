package objective.taskboard.followup;

public class CumulativeFlowDiagramDataPoint {

    public final int lane;
    public final int type;
    public final int label;
    public final int index;
    public final int count;

    public CumulativeFlowDiagramDataPoint(int lane, int type, int label, int index, int count) {
        this.lane = lane;
        this.type = type;
        this.label = label;
        this.index = index;
        this.count = count;
    }
}
