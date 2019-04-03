package objective.taskboard.followup;

import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.followup.FromJiraRowCalculator.FromJiraRowCalculation;
import objective.taskboard.followup.kpi.services.KpiDataService;

public class PlannedVsBallparkCalculatorTest {
    
    private FollowUpSnapshot snapshot = mock(FollowUpSnapshot.class);
    private KpiDataService kpiService = mock(KpiDataService.class);
    private PlannedVsBallparkCalculator subject = new PlannedVsBallparkCalculator(kpiService);
    
    @Before
    public void setup() {
        when(snapshot.getFromJiraRowCalculations()).thenReturn(emptyList());
        when(snapshot.hasClusterConfiguration()).thenReturn(true);
        
        when(kpiService.getSnapshotFromCurrentState(any(), any())).thenReturn(snapshot);
    }

    @Test
    public void givenAnAccumulator_whenInvokeGetData_thenShouldReturnTwoItens() {
        List<PlannedVsBallparkChartData> result = subject.calculate("PX");
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
        when(snapshot.getFromJiraRowCalculations()).thenReturn(getRowCalculations());

        PlannedVsBallparkChartData plannedData = getDataFromAccumulatorWithType(subject, "Planned");        
        assertEquals(25d, plannedData.totalEffort, 0d);                
    }
    
    @Test
    public void givenSomeSnapshotRows_whenAccumulate_thenShouldSumEffortEstimateOfBallparkRows() {
        when(snapshot.getFromJiraRowCalculations()).thenReturn(getRowCalculations());

        PlannedVsBallparkChartData ballparkData = getDataFromAccumulatorWithType(subject, "Ballpark");        
        assertEquals(45d, ballparkData.totalEffort, 0d);                
    }

    private List<FromJiraRowCalculation> getRowCalculations() {
        FromJiraDataRow plannedRow = new FromJiraDataRow();
        plannedRow.queryType = FromJiraDataRow.QUERY_TYPE_SUBTASK_PLAN;
        
        FromJiraDataRow ballparkRow1 = new FromJiraDataRow();
        ballparkRow1.queryType = FromJiraDataRow.QUERY_TYPE_DEMAND_BALLPARK;
        
        FromJiraDataRow ballparkRow2 = new FromJiraDataRow();
        ballparkRow2.queryType = FromJiraDataRow.QUERY_TYPE_FEATURE_BALLPARK;
        
        List<FromJiraRowCalculation> snapshotRows = Arrays.asList(
                new FromJiraRowCalculation(plannedRow, 10d, 0d, 0d),
                new FromJiraRowCalculation(plannedRow, 15d, 0d, 0d),
                new FromJiraRowCalculation(ballparkRow1, 20d, 0d, 0d),
                new FromJiraRowCalculation(ballparkRow1, 25d, 0d, 0d));
        return snapshotRows;
    }
    
    private PlannedVsBallparkChartData getDataFromAccumulatorWithType(PlannedVsBallparkCalculator accumulator, String type) {
        return accumulator.calculate("PX").stream().filter(data -> data.type.equals(type)).findFirst().get();
    }
}