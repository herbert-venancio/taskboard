package objective.taskboard.followup;

import java.util.Arrays;
import java.util.List;

import objective.taskboard.followup.FollowUpDataSnapshot.SnapshotRow;

public class PlannedVsBallparkDataAccumulator {
    private PlannedVsBallparkChartData plannedChartData;
    private PlannedVsBallparkChartData ballparkChartData;
    
    public PlannedVsBallparkDataAccumulator() {
        this.plannedChartData = new PlannedVsBallparkChartData("Planned", 0);
        this.ballparkChartData = new PlannedVsBallparkChartData("Ballpark", 0);
    }        
    
    public void accumulate(SnapshotRow snapshotRow) {
        double effortEstimate = snapshotRow.calcutatedData.getEffortEstimate();
        if (FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN.equals(snapshotRow.rowData.queryType)) {
            plannedChartData.totalEffort += effortEstimate;
        } else {
            ballparkChartData.totalEffort += effortEstimate;
        }
    }
    
    public List<PlannedVsBallparkChartData> getData() {
        return Arrays.asList(plannedChartData, ballparkChartData);
    }
}