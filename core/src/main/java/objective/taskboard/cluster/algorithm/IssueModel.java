package objective.taskboard.cluster.algorithm;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;

import objective.taskboard.data.Changelog;
import objective.taskboard.data.Issue;
import objective.taskboard.data.TaskboardTimeTracking;
import objective.taskboard.utils.RangeUtils;

public class IssueModel {

    public final String issueKey;
    public final double workedTime;
    public final double cycleDays;

    public IssueModel(String issueKey, double workedTime, double cycleDays) {
        this.issueKey = issueKey;
        this.workedTime = workedTime;
        this.cycleDays = cycleDays;
    }

    public String getIssueKey() {
        return issueKey;
    }

    public double getWorkedTime() {
        return workedTime;
    }

    public double getCycleDays() {
        return cycleDays;
    }

    public static IssueModel createForFeatureAndSubtasks(Issue feature, List<Issue> subtasks, ClusterAlgorithmRequest.CycleStatuses cycleStatuses) {
        return new IssueModel(feature.getIssueKey(), workTime(subtasks), cycleDays(subtasks, cycleStatuses).map(IssueModel::countDays).orElse(1));
    }

    public static IssueModel createForSubtask(Issue subtask, ClusterAlgorithmRequest.CycleStatuses cycleStatuses) {
        return new IssueModel(subtask.getIssueKey(), workTime(subtask), cycleDays(subtask, cycleStatuses).map(IssueModel::countDays).orElse(1));
    }

    private static double workTime(List<Issue> subTasks) {
        return subTasks.stream()
                .mapToDouble(IssueModel::workTime)
                .sum();
    }

    private static double workTime(Issue issue) {
        double minutes = Optional.ofNullable(issue.getTimeTracking())
                .flatMap(TaskboardTimeTracking::getTimeSpentMinutes)
                .orElse(0).doubleValue();
        return minutes / 60.0;
    }

    private static Optional<Range<LocalDate>> cycleDays(List<Issue> subTasks, ClusterAlgorithmRequest.CycleStatuses cycleStatuses) {
        return subTasks.stream()
                .flatMap(subTask -> cycleDays(subTask, cycleStatuses).map(Stream::of).orElseGet(Stream::empty))
                .reduce(RangeUtils::expand);
    }

    private static Optional<Range<LocalDate>> cycleDays(Issue issue, ClusterAlgorithmRequest.CycleStatuses cycleStatuses) {
        List<Changelog> statusChanges = issue.getChangelog().stream()
                .filter(entry -> "status".equals(entry.field))
                .collect(Collectors.toList());

        if(statusChanges.isEmpty())
            return Optional.empty();

        Iterator<Changelog> it = statusChanges.iterator();
        ZonedDateTime min = null;
        ZonedDateTime max = null;
        if(cycleStatuses == null) {
            Changelog entry = it.next();
            min = max = entry.timestamp;
            while (it.hasNext()) {
                entry = it.next();
                ZonedDateTime next = entry.timestamp;
                if (next.compareTo(min) < 0) {
                    min = next;
                }
                if (next.compareTo(max) > 0) {
                    max = next;
                }
            }
        } else {
            while (it.hasNext()) {
                Changelog entry = it.next();
                ZonedDateTime next = entry.timestamp;
                long to = Long.parseLong(entry.originalTo);
                if (to == cycleStatuses.getFirst() && (min == null || next.compareTo(min) < 0)) {
                    min = next;
                }
                if (to == cycleStatuses.getLast() && (max == null || next.compareTo(max) > 0)) {
                    max = next;
                }
            }
            if(min == null || max == null) {
                // didn't passed through one of the specified statuses
                return Optional.empty();
            }
        }

        return Optional.of(RangeUtils.between(min.toLocalDate(), max.toLocalDate()));
    }

    private static int countDays(Range<LocalDate> range) {
        LocalDate min = range.getMinimum();
        LocalDate max = range.getMaximum();

        int days = 0;
        for (LocalDate date = min; !date.isAfter(max); date = date.plusDays(1)) {
            if(isWorkDay(date))
                ++days;
        }

        if(!isWorkDay(min) || !isWorkDay(max)) {
            System.out.println("There's someone working on weekends...");
        }
        if(!isWorkDay(min) && !min.equals(max))
            ++days;
        if(!isWorkDay(max))
            ++days;
        return days;
    }

    private static boolean isWorkDay(LocalDate date) {
        switch(date.getDayOfWeek()) {
            case SATURDAY:
            case SUNDAY:
                return false;
            default:
                return true;
        }
    }
}
