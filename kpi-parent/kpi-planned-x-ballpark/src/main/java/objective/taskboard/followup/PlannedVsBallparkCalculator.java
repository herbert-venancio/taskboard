package objective.taskboard.followup;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.followup.FromJiraRowCalculator.FromJiraRowCalculation;
import objective.taskboard.followup.cluster.ClusterNotConfiguredException;
import objective.taskboard.followup.kpi.KpiDataService;

@Component
public class PlannedVsBallparkCalculator {

    private final KpiDataService kpiService;
    
    @Autowired
    public PlannedVsBallparkCalculator(KpiDataService kpiService) {
        this.kpiService = kpiService;
    }

    public List<PlannedVsBallparkChartData> calculate(String projectKey) throws ClusterNotConfiguredException {
        FollowUpSnapshot snapshot = kpiService.getSnapshotFromCurrentState(ZoneId.systemDefault(), projectKey);

        if (!snapshot.hasClusterConfiguration())
            throw new ClusterNotConfiguredException();

        PlannedVsBallparkChartData plannedChartData = new PlannedVsBallparkChartData("Planned", 0);
        PlannedVsBallparkChartData ballparkChartData = new PlannedVsBallparkChartData("Ballpark", 0);

        for (FromJiraRowCalculation rowCalculation : snapshot.getFromJiraRowCalculations()) {
            double effortEstimate = rowCalculation.getEffortEstimate();
            if (FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN.equals(rowCalculation.getRow().queryType)) {
                plannedChartData.totalEffort += effortEstimate;
            } else {
                ballparkChartData.totalEffort += effortEstimate;
            }
        }

        return Arrays.asList(plannedChartData, ballparkChartData);
    }
}