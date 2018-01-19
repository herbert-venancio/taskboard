package objective.taskboard.followup.data;

import static objective.taskboard.utils.NumberUtils.linearInterpolation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import objective.taskboard.followup.EffortHistoryRow;
import objective.taskboard.followup.FollowUpDataSnapshot;
import objective.taskboard.followup.FollowUpDataSnapshotHistory;
import objective.taskboard.utils.NumberUtils;

public class FollowupProgressCalculator {

    public ProgressData calculate(FollowUpDataSnapshot followupData, LocalDate projectDeliveryDate) {
        return calculate(followupData, projectDeliveryDate, 20);
    }
    
    public ProgressData calculate(FollowUpDataSnapshot followupData, LocalDate projectDeliveryDate, int projectionSampleSize) {
        ProgressData progressData = new ProgressData();

        if (!followupData.getHistory().isPresent())
            return progressData;

        List<EffortHistoryRow> historyRows = getSortedHistory(followupData.getHistory().get());
        if (historyRows.isEmpty()) 
            return progressData;

        addActualProgress(progressData, historyRows);
        interpolateMissingDays(progressData);

        EffortHistoryRow firstRow = historyRows.get(0);
        EffortHistoryRow lastRow = historyRows.get(historyRows.size()-1);

        LocalDate startingDate = firstRow.date;
        LocalDate finalProjectDate = projectDeliveryDate.isBefore(lastRow.date) ? lastRow.date : projectDeliveryDate;
        
        addExpectedProgress(progressData, startingDate, projectDeliveryDate, finalProjectDate);
        addProjectionData(progressData, historyRows, startingDate, finalProjectDate, projectionSampleSize);
        
        progressData.startingDate = firstRow.date;
        progressData.endingDate = finalProjectDate;
        return progressData;
    }

    private void addProjectionData(
            ProgressData progressData, 
            List<EffortHistoryRow> historyRows,
            LocalDate startingDate, 
            LocalDate finalProjectDate,
            int progressSampleSize) 
    {
        LocalDate startingDateIt = startingDate;
        long totalDayCount = ChronoUnit.DAYS.between(startingDateIt.atStartOfDay(), finalProjectDate.atStartOfDay());
        EffortHistoryRow firstRow = historyRows.get(0);
        EffortHistoryRow lastRow = historyRows.get(historyRows.size()-1);        
        double projectedProgressFactor = calculateProgressFactor(historyRows, progressSampleSize);
        NumberUtils.LineModel backlogProjection = calculateBacklogProjection(historyRows, progressSampleSize);
        LocalDateTime firstActualDate = firstRow.date.atStartOfDay();
        LocalDateTime lastActualDate = lastRow.date.atStartOfDay();
        long countOfExistingDays = ChronoUnit.DAYS.between(firstActualDate, lastActualDate) + 1;
        double projectedProgress = lastRow.progress();
        double total = lastRow.sumEffortDone + lastRow.sumEffortBacklog;
        progressData.actualProjection.add(progressData.actual.get(progressData.actual.size()-1));
        startingDateIt = firstRow.date.plusDays(countOfExistingDays);
        
        for (long i = countOfExistingDays; i <= totalDayCount; i++) {
            projectedProgress = Math.min(1.0, projectedProgress + projectedProgressFactor);
            double projectedActual = total * projectedProgress;
            double projectedBacklog = Math.max(0.0, backlogProjection.y(i));
            progressData.actualProjection.add(new ProgressDataPoint(startingDateIt, projectedProgress, projectedActual, projectedBacklog));
            startingDateIt = startingDateIt.plus(Period.ofDays(1));
        }
    }

    private void addExpectedProgress(ProgressData progressData, LocalDate startingDate, LocalDate expectedDeliveryDate, LocalDate finalProjectDate) {
        LocalDate startingDateIt = startingDate;
        
        long expectedProjectDuration = ChronoUnit.DAYS.between(startingDateIt.atStartOfDay(), expectedDeliveryDate.atStartOfDay());
        
        double dayNumber = 0;
        while(startingDateIt.isBefore(finalProjectDate) || startingDateIt.equals(finalProjectDate)) {
            double progress = Math.min(dayNumber/expectedProjectDuration, 1.0);
            progressData.expected.add(new ProgressDataPoint(startingDateIt, progress));
            startingDateIt = startingDateIt.plus(Period.ofDays(1));
            dayNumber++;
        }
    }

    private void addActualProgress(ProgressData progressData, List<EffortHistoryRow> historyRows) {
        historyRows.stream().forEach(h -> {
            progressData.actual.add(new ProgressDataPoint(h.date, h.progress(), h.sumEffortDone, h.sumEffortBacklog));
        });
    }

    private void interpolateMissingDays(ProgressData progressData) {
        List<ProgressDataPoint> original = progressData.actual;
        List<ProgressDataPoint> interpolated = new ArrayList<>();

        interpolated.add(original.get(0));

        for (int i = 1; i < original.size(); ++i) {
            ProgressDataPoint lower = original.get(i - 1);
            ProgressDataPoint upper = original.get(i);
            LocalDate lowerDate = lower.date;
            LocalDate upperDate = upper.date;

            long days = ChronoUnit.DAYS.between(lowerDate, upperDate);
            if(days > 1) {
                for(int day = 1; day < days; ++day) {
                    LocalDate date = lowerDate.plus(day, ChronoUnit.DAYS);

                    double factor = day / (double) days;
                    double interpolatedProgress = linearInterpolation(lower.progress, upper.progress, factor);
                    double interpolatedSumEffortDone = linearInterpolation(lower.sumEffortDone, upper.sumEffortDone, factor);
                    double interpolatedSumEffortBacklog = linearInterpolation(lower.sumEffortBacklog, upper.sumEffortBacklog, factor);

                    interpolated.add(new ProgressDataPoint(date, interpolatedProgress, interpolatedSumEffortDone, interpolatedSumEffortBacklog));
                }
            }
            interpolated.add(upper);
        }

        progressData.actual = interpolated;
    }

    private List<EffortHistoryRow> getSortedHistory(FollowUpDataSnapshotHistory effortHistory) {
        List<EffortHistoryRow> historyRows = effortHistory.getHistoryRows();
        historyRows.sort((a, b) -> a.date.compareTo(b.date));
        return historyRows;
    }

    private double calculateProgressFactor(List<EffortHistoryRow> historyRows, int progressSampleSize) {
        List<EffortHistoryRow> samplesToUseForProjection = 
                historyRows.subList(historyRows.size() - Math.min(progressSampleSize, historyRows.size()), historyRows.size());
        double projectedProgressFactor = 0;
        
        if (samplesToUseForProjection.size() == 1)
            return samplesToUseForProjection.get(0).progress();
        
        for (int i = 1; i < samplesToUseForProjection.size(); i++) {
            projectedProgressFactor += samplesToUseForProjection.get(i).progress() - samplesToUseForProjection.get(i-1).progress();
        }
        projectedProgressFactor = projectedProgressFactor / (samplesToUseForProjection.size()-1);
        return projectedProgressFactor;
    }

    private NumberUtils.LineModel calculateBacklogProjection(List<EffortHistoryRow> historyRows, int progressSampleSize) {
        LocalDate firstDay = historyRows.get(0).date;
        int length = Math.min(progressSampleSize, historyRows.size());
        List<EffortHistoryRow> sampledRows = historyRows.subList(historyRows.size() - length, historyRows.size());
        NumberUtils.Point2D[] samples = new NumberUtils.Point2D[length];
        for (int i = 0; i < length; ++i) {
            EffortHistoryRow row = sampledRows.get(i);
            double x = ChronoUnit.DAYS.between(firstDay, row.date);
            double y = row.sumEffortBacklog;
            samples[i] = new NumberUtils.Point2D(x, y);
        }
        return NumberUtils.linearRegression(samples);
    }
}
