package objective.taskboard.followup;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.function.Consumer;

import org.junit.Test;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.FromJiraRowCalculator.FromJiraRowCalculation;
import objective.taskboard.followup.cluster.FollowUpClusterItem;
import objective.taskboard.followup.cluster.FollowupClusterImpl;

public class FromJiraRowCalculatorTest {
    
    private ProjectFilterConfiguration project = mock(ProjectFilterConfiguration.class);

    private List<FollowUpClusterItem> clusterItems = asList(
            new FollowUpClusterItem(project, "Alpha Test",   "unused", "S", 1.0, 1.5),
            new FollowUpClusterItem(project, "Alpha Test",   "unused", "M", 2.0, 2.5),
            new FollowUpClusterItem(project, "Alpha Test",   "unused", "L", 3.0, 3.5),

            new FollowUpClusterItem(project, "Backend Dev",  "unused", "S", 5.0, 5.5),
            new FollowUpClusterItem(project, "Backend Dev",  "unused", "M", 6.0, 6.5),
            new FollowUpClusterItem(project, "Backend Dev",  "unused", "L", 7.0, 7.5));
    
    private FromJiraRowCalculator subject = new FromJiraRowCalculator(new FollowupClusterImpl(clusterItems));

    @Test
    public void calculateEffortEstimate_fromBallpark() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.taskBallpark = 10.0;
        row.queryType = FromJiraDataRow.QUERY_TYPE_FEATURE_BALLPARK;

        assertEquals(10.0, calculateSingle(row).getEffortEstimate(), 0.0);
    }
 
    @Test
    public void calculateEffortEstimate_subtaskWithWronglyFilledTaskBallpark() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.taskBallpark = 10.0;
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.tshirtSize = "M";

        assertEquals(2.0, calculateSingle(row).getEffortEstimate(), 0.0);
    }
    
    @Test
    public void calculateEffortEstimate_fromCluster() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.tshirtSize = "M";

        assertEquals(2.0, calculateSingle(row).getEffortEstimate(), 0.0);
    }
    
    @Test
    public void calculateEffortEstimate_noMatchingCluster() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "UX";
        row.tshirtSize = "M";

        assertEquals(0.0, calculateSingle(row).getEffortEstimate(), 0.0);
    }
    
    @Test
    public void calculateAll_emptyRow() {
        FromJiraDataRow row = new FromJiraDataRow();

        FromJiraRowCalculation calculation = calculateSingle(row);
        assertEquals(0.0, calculation.getEffortEstimate(), 0.0);
        assertEquals(0.0, calculation.getEffortDone(), 0.0);
        assertEquals(0.0, calculation.getEffortOnBacklog(), 0.0);
        assertEquals(0.0, calculation.getCycleEstimate(), 0.0);
        assertEquals(0.0, calculation.getCycleOnBacklog(), 0.0);
        assertEquals(0.0, calculation.getBallparkEffort(), 0.0);
        assertEquals(0.0, calculation.getPlannedEffort(), 0.0);
        assertEquals(0.0, calculation.getCycleDone(), 0.0);
        assertEquals(0.0, calculation.getWorklogDone(), 0.0);
        assertEquals(0.0, calculation.getWorklogDoing(), 0.0);
        assertEquals(0.0, calculation.getSubtaskEstimativeForEepCalculation(), 0.0);
        assertEquals(0.0, calculation.getPlannedEffortOnBug(), 0.0);
        assertEquals(0.0, calculation.getWorklogOnBug(), 0.0);
        assertEquals(0.0, calculation.getCountTasks(), 0.0);
        assertEquals(0.0, calculation.getCountDemands(), 0.0);
        assertEquals(0.0, calculation.getCountSubtasks(), 0.0);
    }

    @Test
    public void calculateEffortDone_doneSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.subtaskStatus = "Done";
        row.tshirtSize = "M";

        assertEquals(2.0, calculateSingle(row).getEffortDone(), 0.0);
    }
    
    @Test
    public void calculateEffortDone_cancelledSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.subtaskStatus = "Cancelled";
        row.tshirtSize = "M";

        assertEquals(2.0, calculateSingle(row).getEffortDone(), 0.0);
    }
    
    @Test
    public void calculateEffortDone_doingSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.subtaskStatus = "Doing";
        row.tshirtSize = "M";

        assertEquals(0.0, calculateSingle(row).getEffortDone(), 0.0);
    }
    
    @Test
    public void calculateEffortOnBacklog_doneSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.subtaskStatus = "Done";
        row.tshirtSize = "M";

        assertEquals(0.0, calculateSingle(row).getEffortOnBacklog(), 0.0);
    }
    
    @Test
    public void calculateEffortOnBacklog_doingSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.subtaskStatus = "Doing";
        row.tshirtSize = "M";

        assertEquals(2.0, calculateSingle(row).getEffortOnBacklog(), 0.0);
    }
    

    @Test
    public void calculateCycleEstimate_fromBallpark() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.taskBallpark = 10.0;
        row.queryType = FromJiraDataRow.QUERY_TYPE_FEATURE_BALLPARK;

        assertEquals(13.0, calculateSingle(row).getCycleEstimate(), 0.0);
    }

    @Test
    public void calculateCycleEstimate_fromCluster() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.tshirtSize = "M";

        assertEquals(2.5, calculateSingle(row).getCycleEstimate(), 0.0);
    }
    
    @Test
    public void calculateCycleDone_doneSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.subtaskStatus = "Done";
        row.tshirtSize = "M";

        assertEquals(2.5, calculateSingle(row).getCycleDone(), 0.0);
    }

    @Test
    public void calculateCycleDone_doingSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.subtaskStatus = "Doing";
        row.tshirtSize = "M";

        assertEquals(0.0, calculateSingle(row).getCycleDone(), 0.0);
    }
    
    @Test
    public void calculateBallparkEffort_ballparkRow() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.planningType = FromJiraDataRow.PLANNING_TYPE_BALLPARK;
        row.queryType = FromJiraDataRow.QUERY_TYPE_FEATURE_BALLPARK;
        row.taskBallpark = 10.0;

        assertEquals(10.0, calculateSingle(row).getBallparkEffort(), 0.0);
    }
    
    @Test
    public void calculateBallparkEffort_planRow() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.planningType = FromJiraDataRow.PLANNING_TYPE_PLAN;
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.tshirtSize = "M";
        
        assertEquals(0.0, calculateSingle(row).getBallparkEffort(), 0.0);
    }
    
    @Test
    public void calculatePlannedEffort_ballparkRow() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.planningType = FromJiraDataRow.PLANNING_TYPE_BALLPARK;
        row.queryType = FromJiraDataRow.QUERY_TYPE_FEATURE_BALLPARK;
        row.taskBallpark = 10.0;

        assertEquals(0.0, calculateSingle(row).getPlannedEffort(), 0.0);
    }
    
    @Test
    public void calculatePlannedffort_planRow() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.planningType = FromJiraDataRow.PLANNING_TYPE_PLAN;
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.tshirtSize = "M";
        
        assertEquals(2.0, calculateSingle(row).getPlannedEffort(), 0.0);
    }
    
    @Test
    public void calculateWorklogDone_doneSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.worklog = 30.0;
        row.subtaskStatus = "Done";

        assertEquals(30.0, calculateSingle(row).getWorklogDone(), 0.0);
    }
    
    @Test
    public void calculateWorklogDone_doingSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.worklog = 30.0;
        row.subtaskStatus = "Doing";

        assertEquals(0.0, calculateSingle(row).getWorklogDone(), 0.0);
    }
    
    @Test
    public void calculateWorklogDoing_doneSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.worklog = 30.0;
        row.subtaskStatus = "Done";

        assertEquals(0.0, calculateSingle(row).getWorklogDoing(), 0.0);
    }
    
    @Test
    public void calculateWorklogDoing_doingSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.worklog = 30.0;
        row.subtaskStatus = "Doing";

        assertEquals(30.0, calculateSingle(row).getWorklogDoing(), 0.0);
    }
    
    @Test
    public void calculateSubtaskEstimativeForEepCalculation_doneSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.subtaskStatus = "Done";
        row.tshirtSize = "M";

        assertEquals(2.0, calculateSingle(row).getSubtaskEstimativeForEepCalculation(), 0.0);
    }
    
    @Test
    public void calculateSubtaskEstimativeForEepCalculation_cancelledSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.subtaskStatus = "Cancelled";
        row.tshirtSize = "M";

        assertEquals(0.0, calculateSingle(row).getSubtaskEstimativeForEepCalculation(), 0.0);
    }
    
    @Test
    public void calculateSubtaskEstimativeForEepCalculation_doingSubtask() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.subtaskType = "Alpha Test";
        row.subtaskStatus = "Doing";
        row.tshirtSize = "M";

        assertEquals(0.0, calculateSingle(row).getSubtaskEstimativeForEepCalculation(), 0.0);
    }
    
    @Test
    public void calculatePlannedEffortOnBug_bugRow() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.taskType = "Bug";
        row.subtaskType = "Alpha Test";
        row.subtaskStatus = "Doing";
        row.tshirtSize = "M";

        assertEquals(2.0, calculateSingle(row).getPlannedEffortOnBug(), 0.0);
    }
    
    @Test
    public void calculatePlannedEffortOnBug_taskRow() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        row.taskType = "Task";
        row.subtaskType = "Alpha Test";
        row.subtaskStatus = "Doing";
        row.tshirtSize = "M";

        assertEquals(0.0, calculateSingle(row).getPlannedEffortOnBug(), 0.0);
    }
    
    @Test
    public void calculateWorklogOnBug_bugRow() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.taskType = "Bug";
        row.worklog = 30.0;

        assertEquals(30.0, calculateSingle(row).getWorklogOnBug(), 0.0);
    }
    
    @Test
    public void calculateWorklogOnBug_taskRow() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.taskType = "Task";
        row.worklog = 30.0;

        assertEquals(0.0, calculateSingle(row).getWorklogOnBug(), 0.0);
    }
    
    @Test
    public void calculateCountDemands() {
        List<FromJiraDataRow> rows = asList(
                row(r -> r.demandDescription = "App"),
                row(r -> r.demandDescription = "App"),
                row(r -> r.demandDescription = "Site"),
                row(r -> r.demandDescription = ""),
                row(r -> r.demandDescription = null));

        List<FromJiraRowCalculation> calculations = subject.calculate(rows);
        
        assertEquals(0.5, calculations.get(0).getCountDemands(), 0.0);
        assertEquals(0.5, calculations.get(1).getCountDemands(), 0.0);
        assertEquals(1.0, calculations.get(2).getCountDemands(), 0.0);
        assertEquals(0.0, calculations.get(3).getCountDemands(), 0.0);
        assertEquals(0.0, calculations.get(4).getCountDemands(), 0.0);
    }
    
    @Test
    public void calculateCountTasks() {
        List<FromJiraDataRow> rows = asList(
                row(r -> r.taskId = 15L),
                row(r -> r.taskId = 15L),
                row(r -> r.taskId = 20L),
                row(r -> r.taskId = 0L),
                row(r -> r.taskId = null));

        List<FromJiraRowCalculation> calculations = subject.calculate(rows);
        
        assertEquals(0.5, calculations.get(0).getCountTasks(), 0.0);
        assertEquals(0.5, calculations.get(1).getCountTasks(), 0.0);
        assertEquals(1.0, calculations.get(2).getCountTasks(), 0.0);
        assertEquals(0.0, calculations.get(3).getCountTasks(), 0.0);
        assertEquals(0.0, calculations.get(4).getCountTasks(), 0.0);
    }

    @Test
    public void calculateCountSubtasks_subtaskRow() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.planningType = FromJiraDataRow.PLANNING_TYPE_PLAN;

        assertEquals(1.0, calculateSingle(row).getCountSubtasks(), 0.0);
    }
    
    @Test
    public void calculateCountSubtasks_ballparkRow() {
        FromJiraDataRow row = new FromJiraDataRow();
        row.planningType = FromJiraDataRow.PLANNING_TYPE_BALLPARK;

        assertEquals(0.0, calculateSingle(row).getCountSubtasks(), 0.0);
    }

    private static FromJiraDataRow row(Consumer<FromJiraDataRow> initializer) {
        FromJiraDataRow row = new FromJiraDataRow();
        initializer.accept(row);
        return row;
    }
    
    private FromJiraRowCalculation calculateSingle(FromJiraDataRow row) {
        return subject.calculate(asList(row)).get(0);
    }
}
