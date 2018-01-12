package objective.taskboard.followup.data;

import static objective.taskboard.testUtils.DateTimeUtilSupport.date;
import static objective.taskboard.testUtils.DateTimeUtilSupport.localDate;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import objective.taskboard.followup.EffortHistoryRow;
import objective.taskboard.followup.FollowUpDataSnapshot;
import objective.taskboard.followup.FollowUpDataSnapshotHistory;

public class FollowupProgressCalculatorTest {
    ZoneId zone = ZoneId.of("Z");
    
    @Test
    public void calculateWithEmptyHistory_shouldReturnEmptyActualAndProjectionShouldBeEqualToExpected() {
        FollowupProgressCalculator subject = new FollowupProgressCalculator(zone);
        
        FollowUpDataSnapshot followupData = makeSnapshotForHistory();
        
        ProgressData progressData = subject.calculate(followupData, localDate(2018, 1, 10, zone));
        
        assertEquals(0, progressData.actual.size());
        assertEquals(0, progressData.actualProjection.size());
        assertEquals(0, progressData.expected.size());
    }
    
    @Test
    public void calculate_shouldCalculateEffort() {
        FollowupProgressCalculator subject = new FollowupProgressCalculator(zone);
        
        FollowUpDataSnapshot followupData = makeSnapshotForHistory(
                new EffortHistoryRow(localDate(2018, 1, 1, zone),      1,       10)
                );
        
        ProgressData progressData = subject.calculate(followupData, localDate(2018, 1, 10, zone));
        
        assertEquals(.091, progressData.actual.get(0).progress, 0.001);
        assertEquals(.091, progressData.actualProjection.get(0).progress, 0.001);
        assertEquals(.182, progressData.actualProjection.get(1).progress, 0.001);
    }
    
    @Test
    public void calculate_shouldCalculateExpectedEffortForAllDatesInTheRange() {
        FollowupProgressCalculator subject = new FollowupProgressCalculator(zone);
        
        FollowUpDataSnapshot followupData = makeSnapshotForHistory(
                new EffortHistoryRow(localDate(2018, 1, 1, zone),      1,       10)
                );
        
        ProgressData progressData = subject.calculate(followupData, localDate(2018, 1, 10, zone));
        
        List<ProgressDataPoint> expected = progressData.expected;
        assertEquals(10, expected.size());
        assertEquals(.0, expected.get(0).progress, 0.001);
        assertEquals(.11, expected.get(1).progress, 0.01);
        assertEquals(1.0, expected.get(expected.size()-1).progress, 0.001);
    }
    
    @Test
    public void calculate_whenDeliveryDateIsInThePast_ExpectedProgressPastDeliveryIsAlways100percent() {
        FollowupProgressCalculator subject = new FollowupProgressCalculator(zone);
        
        FollowUpDataSnapshot followupData = makeSnapshotForHistory(
                new EffortHistoryRow(localDate(2018, 1, 1, zone),  1,       10),
                new EffortHistoryRow(localDate(2018, 1, 11, zone), 1,       10)
                );
        
        ProgressData progressData = subject.calculate(followupData, localDate(2018, 1, 10, zone));
        
        List<ProgressDataPoint> expected = progressData.expected;
        assertEquals(11, expected.size());
        assertEquals(1.0, expected.get(expected.size()-2).progress, 0.001);
        assertEquals(1.0, expected.get(expected.size()-1).progress, 0.001);
    }
    
    
    
    @Test
    public void calculateWithOneSample_shouldCalculateProjectionBasedOnAvgOfLastEffort() {
        FollowupProgressCalculator subject = new FollowupProgressCalculator(zone);
        
        FollowUpDataSnapshot followupData = makeSnapshotForHistory(
                new EffortHistoryRow(localDate(2018, 1, 1, zone),      0,       10),
                new EffortHistoryRow(localDate(2018, 1, 2, zone),      0.7,     10)
                );
        
        ProgressData progressData = subject.calculate(followupData, localDate(2018, 1, 10, zone));
        
        assertEquals(.065, progressData.actual.get(1).progress, 0.001);
        assertEquals(9, progressData.actualProjection.size());
        assertEquals(localDate(2018, 1, 2, zone), localDate(progressData.actualProjection.get(0).date, zone));
        assertEquals(localDate(2018, 1, 3, zone), localDate(progressData.actualProjection.get(1).date, zone));
        assertEquals(localDate(2018, 1, 6, zone), localDate(progressData.actualProjection.get(4).date, zone));
        assertEquals(progressData.actual.get(1).progress,  progressData.actualProjection.get(0).progress, 0.001);
        
        assertEquals(.065,  progressData.actualProjection.get(0).progress, 0.001);
        assertEquals(.13,  progressData.actualProjection.get(1).progress, 0.001);
        assertEquals(.327, progressData.actualProjection.get(4).progress, 0.001);
    }
    
    @Test
    public void calculateWhenDeliveryDateIsBeforeLastActualDate_projectionShouldBeEmptyAndEndDateMustMatchLastActualDate() {
        FollowupProgressCalculator subject = new FollowupProgressCalculator(zone);
        
        FollowUpDataSnapshot followupData = makeSnapshotForHistory(
                new EffortHistoryRow(localDate(2018, 1, 1, zone), 0,   10),
                new EffortHistoryRow(localDate(2018, 1, 2, zone), 0.7, 10),
                new EffortHistoryRow(localDate(2018, 1, 3, zone), 1,   10)
                );
        
        ProgressData progressData = subject.calculate(followupData, localDate(2018, 1, 2, zone));
        
        assertEquals(1, progressData.actualProjection.size());
        assertEquals(date(2018, 1, 3, zone), progressData.endingDate);
    }
    
    
    @Test
    public void calculate_givenProjectionSize_shouldCalculateProjectionWithGivenSize() {
        FollowupProgressCalculator subject = new FollowupProgressCalculator(zone);
        
        FollowUpDataSnapshot followupData = makeSnapshotForHistory(
                new EffortHistoryRow(localDate(2018, 1, 1, zone),      0,       10),
                new EffortHistoryRow(localDate(2018, 1, 2, zone),      0.7,     9.3),
                new EffortHistoryRow(localDate(2018, 1, 3, zone),      1.8,     8.2)
                );
        
        ProgressData progressData = subject.calculate(followupData, localDate(2018, 1, 10, zone), 2);
        
        assertEquals(.07, progressData.actual.get(1).progress, 0.001);
        assertEquals(8, progressData.actualProjection.size());
        assertEquals(localDate(2018, 1, 4, zone), localDate(progressData.actualProjection.get(1).date, zone));
        assertEquals(localDate(2018, 1, 6, zone), localDate(progressData.actualProjection.get(3).date, zone));
        assertEquals(progressData.actual.get(2).progress,  progressData.actualProjection.get(0).progress, 0.001);
        
        assertEquals(.29,  progressData.actualProjection.get(1).progress, 0.001);
    }
    

    private FollowUpDataSnapshot makeSnapshotForHistory(EffortHistoryRow... rows) {
        List<EffortHistoryRow> historyRows = Arrays.asList(rows);
        
        FollowUpDataSnapshotHistory history = mock(FollowUpDataSnapshotHistory.class);
        when(history.getHistoryRows()).thenReturn(historyRows);
        FollowUpDataSnapshot followupData = mock(FollowUpDataSnapshot.class);
        when(followupData.getHistory()).thenReturn(Optional.of(history));
        return followupData;
    }
}