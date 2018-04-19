package objective.taskboard.followup.data;

import static objective.taskboard.utils.NumberUtils.linearInterpolation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.ToDoubleFunction;
import java.util.stream.IntStream;

import objective.taskboard.followup.EffortHistoryRow;
import objective.taskboard.followup.FollowUpDataSnapshot;
import objective.taskboard.followup.FollowUpDataSnapshotHistory;

public class FollowupProgressCalculator {

    public static final int DEFAULT_PROJECTION_SAMPLE_SIZE = 20;

    public ProgressData calculate(FollowUpDataSnapshot followupData, LocalDate projectStartDate, LocalDate projectDeliveryDate) {
        return calculate(followupData, projectStartDate, projectDeliveryDate, DEFAULT_PROJECTION_SAMPLE_SIZE);
    }

    public ProgressData calculate(FollowUpDataSnapshot followupData, LocalDate projectStartDate, LocalDate projectDeliveryDate, int projectionSampleSize) {
        ProgressData progressData = new ProgressData();

        Optional<FollowUpDataSnapshotHistory> optHistory = followupData.getHistory();
        if (!optHistory.isPresent())
            return progressData;

        List<EffortHistoryRow> historyRows = getSortedHistory(optHistory.get());
        if (historyRows.isEmpty()) 
            return progressData;

        addActualProgress(progressData, historyRows);
        interpolateMissingDays(progressData);

        EffortHistoryRow firstRow = historyRows.get(0);
        EffortHistoryRow lastRow = historyRows.get(historyRows.size()-1);

        LocalDate startingDate = firstRow.date;
        LocalDate finalProjectDate = projectDeliveryDate.isBefore(lastRow.date) ? lastRow.date : projectDeliveryDate;

        addExpectedProgress(progressData, projectStartDate, projectDeliveryDate, finalProjectDate);
        addProjectionData(progressData, historyRows, startingDate, finalProjectDate, projectionSampleSize);

        progressData.startingDate = projectStartDate;
        progressData.endingDate = finalProjectDate;
        progressData.projectionTimespan = projectionSampleSize;

        return progressData;
    }

    private void addProjectionData(
            ProgressData progressData, 
            List<EffortHistoryRow> historyRows,
            LocalDate startingDate, 
            LocalDate finalProjectDate,
            int progressSampleSize) 
    {
        long totalDayCount = ChronoUnit.DAYS.between(startingDate.atStartOfDay(), finalProjectDate.atStartOfDay());
        EffortHistoryRow firstRow = historyRows.get(0);
        EffortHistoryRow lastRow = historyRows.get(historyRows.size()-1);
        CalculateFactors calculate = new CalculateFactors(historyRows, progressSampleSize);
        double projectedProgressFactor = calculate.progressFactor();
        double actualProjectionFactor = calculate.actualFactor();
        double backlogProjectionFactor = calculate.backlogFactor();
        LocalDateTime firstActualDate = firstRow.date.atStartOfDay();
        LocalDateTime lastActualDate = lastRow.date.atStartOfDay();
        long countOfExistingDays = ChronoUnit.DAYS.between(firstActualDate, lastActualDate) + 1;
        double projectedProgress = lastRow.progress();
        double projectedActual = lastRow.sumEffortDone;
        double projectedBacklog = lastRow.sumEffortBacklog;
        progressData.actualProjection.add(progressData.actual.get(progressData.actual.size()-1));
        LocalDate startingDateIt = firstRow.date.plusDays(countOfExistingDays);

        for (long i = countOfExistingDays; i <= totalDayCount; i++) {
            if(projectedProgress < 1.0 || projectedBacklog > 0.0) {
                projectedProgress = Math.min(1.0, projectedProgress + projectedProgressFactor);
                projectedActual += actualProjectionFactor;
                projectedBacklog = Math.max(0.0, projectedBacklog + backlogProjectionFactor);
            }
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

    private static class CalculateFactors {
        private final List<EffortHistoryRow> historyRows;
        private final int progressSampleSize;

        private CalculateFactors(List<EffortHistoryRow> historyRows, int progressSampleSize) {
            this.historyRows = historyRows;
            this.progressSampleSize = progressSampleSize;
        }

        double progressFactor() {
            return averageDelta(row -> row.progress())
                    .orElse(historyRows.get(0).progress());
        }

        double actualFactor() {
            return averageDelta(row -> row.sumEffortDone)
                    .orElse(0.0);
        }

        double backlogFactor() {
            return averageDelta(row -> row.sumEffortBacklog)
                    .orElse(0.0);
        }

        private OptionalDouble averageDelta(ToDoubleFunction<EffortHistoryRow> deltaFunction) {
            int samples = Math.min(progressSampleSize, historyRows.size());
            int firstIndex = historyRows.size() - samples + 1;
            return IntStream.range(firstIndex, historyRows.size())
                    .mapToDouble(i -> deltaFunction.applyAsDouble(historyRows.get(i)) - deltaFunction.applyAsDouble(historyRows.get(i - 1)))
                    .average();
        }
    }
}
