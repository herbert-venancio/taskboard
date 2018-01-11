package objective.taskboard.followup;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import objective.taskboard.followup.FollowUpDataSnapshot.SnapshotRow;
import objective.taskboard.followup.FromJiraRowCalculator.FromJiraRowCalculation;

import static org.junit.Assert.assertEquals;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class PlannedVsBallparkDataAccumulatorTest {
    
    PlannedVsBallparkDataAccumulator subject;
    
    @Before
    public void setup() {
        subject = new PlannedVsBallparkDataAccumulator();
    }
    
    @Test
    public void givenAnAccumulator_whenInvokeGetData_thenShouldReturnTwoItens() {
        List<PlannedVsBallparkChartData> result = subject.getData();
        assertThat(result, hasSize(2));
    }
    
    @Test
    public void givenNewAccumulator_whenHasNotAccumulate_thenPlannedTotalEffortShouldBeZero() {        
        PlannedVsBallparkChartData plannedData = getDataFromAccumulatorWithType(subject, "Planned");
        
        assertEquals(0d, plannedData.totalEffort, 0d);
    }
    
    @Test
    public void givenNewAccumulator_whenHasNotAccumulate_thenBallparkTotalEffortShouldBeZero() {        
        PlannedVsBallparkChartData plannedData = getDataFromAccumulatorWithType(subject, "Ballpark");
        
        assertEquals(0d, plannedData.totalEffort, 0d);
    }
    
    @Test
    public void givenSomeSnapshotRows_whenAccumulate_thenShouldSumEffortEstimateOfPlannedRows() {
        List<SnapshotRow> snapshotRows = getSnapshotRows();
        
        snapshotRows.forEach(subject::accumulate);
        
        PlannedVsBallparkChartData plannedData = getDataFromAccumulatorWithType(subject, "Planned");        
        assertEquals(25d, plannedData.totalEffort, 0d);                
    }
    
    @Test
    public void givenSomeSnapshotRows_whenAccumulate_thenShouldSumEffortEstimateOfBallparkRows() {
        List<SnapshotRow> snapshotRows = getSnapshotRows();
        
        snapshotRows.forEach(subject::accumulate);
        
        PlannedVsBallparkChartData ballparkData = getDataFromAccumulatorWithType(subject, "Ballpark");        
        assertEquals(45d, ballparkData.totalEffort, 0d);                
    }

    private List<SnapshotRow> getSnapshotRows() {
        FromJiraDataRow plannedRow = new FromJiraDataRow();
        plannedRow.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        
        FromJiraDataRow ballparkRow1 = new FromJiraDataRow();
        ballparkRow1.queryType = FromJiraDataRow.QUERY_TYPE_DEMAND_BALLPARK;
        
        FromJiraDataRow ballparkRow2 = new FromJiraDataRow();
        ballparkRow2.queryType = FromJiraDataRow.QUERY_TYPE_FEATURE_BALLPARK;
        
        List<SnapshotRow> snapshotRows = Arrays.asList(
                new SnapshotRow(plannedRow, new FromJiraRowCalculation(10d, 0d, 0d)),
                new SnapshotRow(plannedRow, new FromJiraRowCalculation(15d, 0d, 0d)),
                new SnapshotRow(ballparkRow1, new FromJiraRowCalculation(20d, 0d, 0d)),
                new SnapshotRow(ballparkRow1, new FromJiraRowCalculation(25d, 0d, 0d)));
        return snapshotRows;
    }
    
    private PlannedVsBallparkChartData getDataFromAccumulatorWithType(PlannedVsBallparkDataAccumulator accumulator, String type) {
        return accumulator.getData().stream().filter(data -> data.type.equals(type)).findFirst().get();
    }
}