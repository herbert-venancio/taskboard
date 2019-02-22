package objective.taskboard.followup.kpi.touchtime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.properties.TouchTimeSubtaskConfiguration;
import objective.taskboard.utils.DateTimeUtils;

class TouchTimeByWeekKpiSubtaskStrategy extends TouchTimeByWeekKpiStrategy {

    private Table<TouchTimeKpiWeekRange, String, List<Double>> effortsByStackNameByWeek = HashBasedTable.create();
    private List<TouchTimeSubtaskConfiguration> touchTimeSubtaskConfigs;

    protected TouchTimeByWeekKpiSubtaskStrategy(
            Range<LocalDate> projectRange, ZoneId timezone, List<IssueKpi> issues,
            List<TouchTimeSubtaskConfiguration> touchTimeSubtaskConfigs) {
        super(projectRange, timezone, issues);
        this.touchTimeSubtaskConfigs = touchTimeSubtaskConfigs;
    }

    @Override
    protected List<TouchTimeByWeekKpiDataPoint> getDataPoints() {
        aggregateEffortAccordingToConfiguration();
        return transformToDataPoints();
    }

    private void aggregateEffortAccordingToConfiguration() {
        super.issuesByWeek.entrySet().forEach(entry -> aggregate(entry.getKey(), entry.getValue()));
    }

    private void aggregate(TouchTimeKpiWeekRange week, List<IssueKpi> issuesFromWeek) {
        List<IssueKpi> issuesFromWeekCopy = new LinkedList<>(issuesFromWeek);

        for (TouchTimeSubtaskConfiguration conf : touchTimeSubtaskConfigs) {
            List<IssueKpi> issuesSelectedByType = filterIssuesByTypes(conf.getTypeIds(), issuesFromWeek);
            effortsByStackNameByWeek.put(week,conf.getStackName(), getEffortsFromIssues(week, issuesSelectedByType));
            issuesFromWeekCopy.removeAll(issuesSelectedByType);

            collectEffortFromStatusesForStack(conf.getStackName(), conf.getStatuses(), week, issuesFromWeekCopy);
        }
    }

    private List<IssueKpi> filterIssuesByTypes(List<Long> typesIds, List<IssueKpi> issuesFromWeek) {
        return issuesFromWeek.stream()
            .filter(i -> filterByTypes(i, typesIds))
            .collect(Collectors.toList());
    }

    private boolean filterByTypes(IssueKpi issue, List<Long> typesIds) {
        return issue.getIssueType().map(t -> typesIds.contains(t.getId())).orElse(false);
    }

    private List<Double> getEffortsFromIssues(TouchTimeKpiWeekRange week, List<IssueKpi> issuesSelectedByType) {
        return issuesSelectedByType.stream()
            .map(i -> i.getEffortUntilDate(week.getLastDay()))
            .map(DateTimeUtils::secondsToHours)
            .collect(Collectors.toList());
    }

    private void collectEffortFromStatusesForStack(String stackName, List<String> statuses, TouchTimeKpiWeekRange week, List<IssueKpi> issuesFromWeek) {
        issuesFromWeek.stream()
            .map(i -> i.getEffortSumInSecondsFromStatusesUntilDate(statuses, week.getLastDay()))
            .mapToDouble(DateTimeUtils::secondsToHours)
            .forEach(effortSumInHours -> effortsByStackNameByWeek.get(week, stackName).add(effortSumInHours));
    }

    private List<TouchTimeByWeekKpiDataPoint> transformToDataPoints() {
        return effortsByStackNameByWeek.cellSet().stream()
                .sorted(Comparator.comparing(Cell::getRowKey))
                .map(c -> new TouchTimeByWeekKpiDataPoint(
                        c.getRowKey().getLastDay().toInstant(),
                        c.getColumnKey(),
                        calculateAverage(c)))
                .collect(Collectors.toList());
    }

    private double calculateAverage(Cell<TouchTimeKpiWeekRange, String, List<Double>> effortsByStackNameByWeekCell) {
        TouchTimeKpiWeekRange week = effortsByStackNameByWeekCell.getRowKey();
        List<Double> efforts = effortsByStackNameByWeekCell.getValue();
        double sum = efforts.stream().collect(Collectors.summingDouble(x -> x));
        if (sum <= 0d)
            return 0d;
        int numberOfIssuesFromWeek = super.issuesByWeek.get(week).size();
        if (numberOfIssuesFromWeek == 0)
            throw new RuntimeException("Number of issues from week cannot be zero!");
        return sum / numberOfIssuesFromWeek;
    }

}
