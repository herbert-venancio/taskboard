package objective.taskboard.followup;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.followup.FromJiraDataRow.PLANNING_TYPE_BALLPARK;
import static objective.taskboard.followup.FromJiraDataRow.PLANNING_TYPE_PLAN;
import static objective.taskboard.followup.FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.List;
import java.util.Optional;

import objective.taskboard.followup.cluster.FollowUpClusterItem;
import objective.taskboard.followup.cluster.FollowupCluster;

public class FromJiraRowCalculator {

    private final FollowupCluster followupCluster;

    public FromJiraRowCalculator(FollowupCluster cluster) {
        this.followupCluster = cluster;
    }
    
    public List<FromJiraRowCalculation> calculate(List<FromJiraDataRow> allRows) {
        return allRows.stream().map(row -> calculateRow(row, allRows)).collect(toList());
    }

    private FromJiraRowCalculation calculateRow(FromJiraDataRow row, List<FromJiraDataRow> allRows) {
        FromJiraRowCalculation calculation = new FromJiraRowCalculation(row);
        
        fillEffortEstimate(calculation);
        fillEffortDone(calculation);
        fillEffortOnBacklog(calculation);
        fillCycleEstimate(calculation);
        fillCycleDone(calculation);
        fillCycleOnBacklog(calculation);
        fillBallparkEffort(calculation);
        fillPlannedEffort(calculation);
        fillWorklogDone(calculation);
        fillWorklogDoing(calculation);
        fillSubtaskEstimativeForEepCalculation(calculation);
        fillPlannedEffortOnBug(calculation);
        fillWorklogOnBug(calculation);
        fillCountDemands(calculation, allRows);
        fillCountTasks(calculation, allRows);
        fillCountSubtasks(calculation);
        
        return calculation;
    }

    public Double getEffortEstimate(FromJiraDataRow row) {
        return row.taskBallpark != null && row.taskBallpark > 0 && !QUERY_TYPE_SUBTASK_PLAN.equals(row.queryType) 
                ? row.taskBallpark 
                : getClusterFor(row).map(i -> i.getEffort()).orElse(0d);
    }

    private void fillEffortEstimate(FromJiraRowCalculation calculation) {
        calculation.effortEstimate = getEffortEstimate(calculation.row);
    }

    private void fillEffortDone(FromJiraRowCalculation calculation) {
        calculation.effortDone = "Done".equals(calculation.row.subtaskStatus) || "Cancelled".equals(calculation.row.subtaskStatus) 
                ? calculation.effortEstimate 
                : 0.0;
    }
    
    private void fillEffortOnBacklog(FromJiraRowCalculation calculation) {
        calculation.effortOnBacklog = calculation.effortEstimate - calculation.effortDone;
    }

    private void fillCycleEstimate(FromJiraRowCalculation calculation) {
        boolean isBallpark = calculation.row.taskBallpark != null 
                && calculation.row.taskBallpark > 0 
                && !QUERY_TYPE_SUBTASK_PLAN.equals(calculation.row.queryType);

        calculation.cycleEstimate = isBallpark 
                ? calculation.row.taskBallpark * 1.3 
                : getClusterFor(calculation.row).map(i -> i.getCycle()).orElse(0d);
    }

    private void fillCycleDone(FromJiraRowCalculation calculation) {
        calculation.cycleDone = "Done".equals(calculation.row.subtaskStatus) || "Cancelled".equals(calculation.row.subtaskStatus) 
                ? calculation.cycleEstimate 
                : 0.0;
    }

    private void fillCycleOnBacklog(FromJiraRowCalculation calculation) {
        calculation.cycleOnBacklog = calculation.cycleEstimate - calculation.cycleDone;
    }

    private void fillBallparkEffort(FromJiraRowCalculation calculation) {
        calculation.ballparkEffort = PLANNING_TYPE_BALLPARK.equals(calculation.row.planningType) 
                ? calculation.effortEstimate 
                : 0.0;
    }

    private void fillPlannedEffort(FromJiraRowCalculation calculation) {
        calculation.plannedEffort = PLANNING_TYPE_PLAN.equals(calculation.row.planningType) 
                ? calculation.effortEstimate 
                : 0.0;
    }

    private void fillWorklogDone(FromJiraRowCalculation calculation) {
        calculation.worklogDone = "Done".equals(calculation.row.subtaskStatus) || "Cancelled".equals(calculation.row.subtaskStatus) 
                ? defaultZero(calculation.row.worklog) 
                : 0.0;
    }

    private void fillWorklogDoing(FromJiraRowCalculation calculation) {
        calculation.worklogDoing = "Done".equals(calculation.row.subtaskStatus) || "Cancelled".equals(calculation.row.subtaskStatus) 
                ? 0.0
                : defaultZero(calculation.row.worklog);
    }

    private void fillSubtaskEstimativeForEepCalculation(FromJiraRowCalculation calculation) {
        calculation.subtaskEstimativeForEepCalculation = "Done".equals(calculation.row.subtaskStatus) 
                ? calculation.effortDone 
                : 0.0;
    }

    private void fillPlannedEffortOnBug(FromJiraRowCalculation calculation) {
        calculation.plannedEffortOnBug = "Bug".equals(calculation.row.taskType) 
                ? calculation.effortEstimate 
                : 0.0;
    }

    private void fillWorklogOnBug(FromJiraRowCalculation calculation) {
        calculation.worklogOnBug = "Bug".equals(calculation.row.taskType) 
                ? defaultZero(calculation.row.worklog) 
                : 0.0;
    }

    private void fillCountDemands(FromJiraRowCalculation calculation, List<FromJiraDataRow> allRows) {
        calculation.countDemands = isNotBlank(calculation.row.demandDescription) 
                ? 1.0 / allRows.stream().filter(r -> calculation.row.demandDescription.equals(r.demandDescription)).count() 
                : 0.0;
    }

    private void fillCountTasks(FromJiraRowCalculation calculation, List<FromJiraDataRow> allRows) {
        calculation.countTasks = calculation.row.taskId != null && calculation.row.taskId > 0 
                ? 1.0 / allRows.stream().filter(r -> calculation.row.taskId.equals(r.taskId)).count() 
                : 0.0;
    }

    private void fillCountSubtasks(FromJiraRowCalculation calculation) {
        calculation.countSubtasks = PLANNING_TYPE_PLAN.equals(calculation.row.planningType) 
                ? 1.0 
                : 0.0;
    }

    private Optional<FollowUpClusterItem> getClusterFor(FromJiraDataRow row) {
        return followupCluster.getClusterFor(row.subtaskType, row.tshirtSize);
    }

    private static double defaultZero(Double value) {
        return value == null ? 0.0 : value;
    }

    public static class FromJiraRowCalculation {
        private final FromJiraDataRow row;

        private double effortEstimate;
        private double effortDone;
        private double effortOnBacklog;
        private double cycleEstimate;
        private double cycleOnBacklog;
        private double ballparkEffort;
        private double plannedEffort;
        private double cycleDone;
        private double worklogDone;
        private double worklogDoing;
        private double countDemands;
        private double countTasks;
        private double countSubtasks;
        private double subtaskEstimativeForEepCalculation;
        private double plannedEffortOnBug;
        private double worklogOnBug;

        public FromJiraRowCalculation(FromJiraDataRow row, double effortEstimate, double effortDone, double effortOnBacklog) {
            this(row);

            this.effortEstimate = effortEstimate;
            this.effortDone = effortDone;
            this.effortOnBacklog = effortOnBacklog;
        }
        
        private FromJiraRowCalculation(FromJiraDataRow row) {
            this.row = row;
        }
        
        public FromJiraDataRow getRow() {
            return row;
        }

        public double getEffortEstimate() {
            return effortEstimate;
        }

        public double getEffortDone() {
            return effortDone;
        }

        public double getEffortOnBacklog() {
            return effortOnBacklog;
        }

        public double getCycleEstimate() {
            return cycleEstimate;
        }

        public double getCycleOnBacklog() {
            return cycleOnBacklog;
        }

        public double getBallparkEffort() {
            return ballparkEffort;
        }

        public double getPlannedEffort() {
            return plannedEffort;
        }

        public double getCycleDone() {
            return cycleDone;
        }

        public double getWorklogDone() {
            return worklogDone;
        }

        public double getWorklogDoing() {
            return worklogDoing;
        }

        public double getCountDemands() {
            return countDemands;
        }

        public double getCountTasks() {
            return countTasks;
        }

        public double getCountSubtasks() {
            return countSubtasks;
        }

        public double getSubtaskEstimativeForEepCalculation() {
            return subtaskEstimativeForEepCalculation;
        }

        public double getPlannedEffortOnBug() {
            return plannedEffortOnBug;
        }

        public double getWorklogOnBug() {
            return worklogOnBug;
        }
    }
}
