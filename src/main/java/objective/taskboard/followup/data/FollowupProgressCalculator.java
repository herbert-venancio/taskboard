package objective.taskboard.followup.data;

import static objective.taskboard.utils.DateTimeUtils.toDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import objective.taskboard.followup.EffortHistoryRow;
import objective.taskboard.followup.FollowUpDataSnapshot;
import objective.taskboard.followup.FollowUpDataSnapshotHistory;

public class FollowupProgressCalculator {
    private ZoneId zone;

    public FollowupProgressCalculator(ZoneId zone) {
        this.zone = zone;
    }

    public ProgressData calculate(FollowUpDataSnapshot followupData, LocalDate projectionLocalDate) {
        ProgressData progressData = new ProgressData();

        if (!followupData.getHistory().isPresent())
            return progressData;

        List<EffortHistoryRow> historyRows = getSortedHistory(followupData.getHistory().get());
        if (historyRows.isEmpty()) 
            return progressData;

        addActualProgress(progressData, historyRows);

        EffortHistoryRow firstRow = historyRows.get(0);
        EffortHistoryRow lastRow = historyRows.get(historyRows.size()-1);
        
        LocalDate startingDate = firstRow.date;
        LocalDate finalProjectDate = projectionLocalDate.isBefore(lastRow.date) ? lastRow.date : projectionLocalDate;
        
        addExpectedProgress(progressData, startingDate, finalProjectDate);
        addProjectionData(progressData, historyRows, startingDate, finalProjectDate);
        
        progressData.startingDate = toDate(firstRow.date, zone);
        progressData.endingDate = toDate(finalProjectDate, zone);
        return progressData;
    }

    private void addProjectionData(
            ProgressData progressData, 
            List<EffortHistoryRow> historyRows,
            LocalDate startingDate, 
            LocalDate finalProjectDate) 
    {
        LocalDate startingDateIt = startingDate;
        long totalDayCount = ChronoUnit.DAYS.between(startingDateIt.atStartOfDay(), finalProjectDate.atStartOfDay());
        EffortHistoryRow firstRow = historyRows.get(0);
        EffortHistoryRow lastRow = historyRows.get(historyRows.size()-1);        
        double projectedProgressFactor = calculateProgressFactor(historyRows);
        LocalDateTime firstActualDate = firstRow.date.atStartOfDay();
        LocalDateTime lastActualDate = lastRow.date.atStartOfDay();
        long countOfExistingDays = ChronoUnit.DAYS.between(firstActualDate, lastActualDate) + 1;
        double projectedProgress = lastRow.progress();
        progressData.actualProjection.add(progressData.actual.get(progressData.actual.size()-1));
        startingDateIt = firstRow.date.plusDays(countOfExistingDays);
        
        for (long i = countOfExistingDays; i <= totalDayCount; i++) {
            projectedProgress += projectedProgressFactor;
            Date date = toDate(startingDateIt, zone);
            progressData.actualProjection.add(new ProgressDataPoint(date, projectedProgress));
            startingDateIt = startingDateIt.plus(Period.ofDays(1));
        }
    }

    private void addExpectedProgress(ProgressData progressData, LocalDate startingDate, LocalDate finalProjectDate) {
        LocalDate startingDateIt = startingDate;
        
        long totalDayCount = ChronoUnit.DAYS.between(startingDateIt.atStartOfDay(), finalProjectDate.atStartOfDay());
        
        double dayNumber = 0;
        while(startingDateIt.isBefore(finalProjectDate) || startingDateIt.equals(finalProjectDate)) {
            progressData.expected.add(new ProgressDataPoint(toDate(startingDateIt, zone), dayNumber/totalDayCount));
            startingDateIt = startingDateIt.plus(Period.ofDays(1));
            dayNumber++;
        }
    }

    private void addActualProgress(ProgressData progressData, List<EffortHistoryRow> historyRows) {
        historyRows.stream().forEach(h -> {
            Date date = Date.from(h.date.atStartOfDay().atZone(zone).toInstant());
            progressData.actual.add(new ProgressDataPoint(date, h.progress()));
        });
    }

    private List<EffortHistoryRow> getSortedHistory(FollowUpDataSnapshotHistory effortHistory) {
        List<EffortHistoryRow> historyRows = effortHistory.getHistoryRows();
        historyRows.sort((a, b) -> a.date.compareTo(b.date));
        return historyRows;
    }

    private double calculateProgressFactor(List<EffortHistoryRow> historyRows) {
        List<EffortHistoryRow> upToLastFour = historyRows.subList(historyRows.size() - Math.min(4, historyRows.size()), historyRows.size());
        double projectedProgressFactor = 0;
        
        if (upToLastFour.size() == 1)
            return upToLastFour.get(0).progress();
        
        for (int i = 1; i < upToLastFour.size(); i++) {
            projectedProgressFactor += upToLastFour.get(i).progress() - upToLastFour.get(i-1).progress();
        }
        projectedProgressFactor = projectedProgressFactor / (upToLastFour.size()-1);
        return projectedProgressFactor;
    }
}
