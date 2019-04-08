package objective.taskboard.followup.kpi.touchtime;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Range;

import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.filters.KpiWeekRange;
import objective.taskboard.followup.kpi.properties.TouchTimeSubtaskConfiguration;
import objective.taskboard.utils.DateTimeUtils;

class TouchTimeByWeekKpiSubtaskStrategy extends TouchTimeByWeekKpiStrategy {

    private List<TouchTimeSubtaskConfiguration> touchTimeSubtaskConfigs;
    private Map<Long, String> indexTypeIdToStackName = new HashMap<>();

    protected TouchTimeByWeekKpiSubtaskStrategy(
            Range<LocalDate> projectRange,
            ZoneId timezone,
            List<IssueKpi> issues,
            List<TouchTimeSubtaskConfiguration> touchTimeSubtaskConfigs) {
        super(projectRange, timezone, issues);
        this.touchTimeSubtaskConfigs = touchTimeSubtaskConfigs;
        touchTimeSubtaskConfigs.forEach(conf -> {
            conf.getTypeIds().forEach(typeId ->
                indexTypeIdToStackName.put(typeId, conf.getStackName())
            );
        });
    }

    @Override
    protected List<TouchTimeByWeekKpiDataPoint> getDataPoints() {
        List<TouchTimeByWeekKpiDataPoint> points = new LinkedList<>();
        issuesByWeek.forEach((week, issuesFromWeek) -> {
            Map<String, Stack> stackByName = new StackAggregator(week).aggregate(issuesFromWeek);
            touchTimeSubtaskConfigs.forEach(conf ->
                points.add(new DataPointTransformer(stackByName.get(conf.getStackName()), conf.getStatuses(), issuesFromWeek.size()).transform())
            );
        });
        Collections.sort(points);
        return points;
    }

    private class StackAggregator {
        private Map<String, Stack> stackByName = new HashMap<>();
        private StackAggregator(KpiWeekRange week) {
            touchTimeSubtaskConfigs.forEach(conf ->
                stackByName.put(conf.getStackName(), new Stack(week, conf.getStackName()))
            );
        }
        private Map<String, Stack> aggregate(List<IssueKpi> issuesFromWeek) {
            issuesFromWeek.forEach(issue ->
                issue.getIssueType().ifPresent(type -> {
                    String stackName = indexTypeIdToStackName.get(type.getId());
                    if (stackName == null) {
                        stackByName.values().forEach(stack -> stack.addByStatus(issue));
                    } else {
                        stackByName.get(stackName).addByType(issue);
                    }
                })
            );
            return stackByName;
        }
    }

    private class Stack {
        private KpiWeekRange week;
        private String name;
        private Collection<IssueKpi> issuesFromTypes = new LinkedList<>();
        private Collection<IssueKpi> issuesFromStatuses = new LinkedList<>();
        private Stack(KpiWeekRange week, String stackName) {
            this.week = week;
            this.name = stackName;
        }
        private void addByType(IssueKpi issue) {
            issuesFromTypes.add(issue);
        }
        private void addByStatus(IssueKpi issue) {
            issuesFromStatuses.add(issue);
        }
    }

    private class DataPointTransformer {
        private Stack stack;
        private List<String> statuses;
        private int totalIssues;
        private DataPointTransformer(Stack stack, List<String> statuses, int totalIssues) {
            this.stack = stack;
            this.statuses = statuses;
            this.totalIssues = totalIssues;
        }
        public TouchTimeByWeekKpiDataPoint transform() {
            return new TouchTimeByWeekKpiDataPoint(stack.week.getLastDay().toInstant(), stack.name, getEffortAverageInHours());
        }
        private double getEffortAverageInHours() {
            if (totalIssues == 0) {
                return 0;
            }
            double effortSumFromTypes = getEffortSumFromTypes();
            double effortFromStatuses = getEffortSumFromStatuses();
            return (effortSumFromTypes + effortFromStatuses) / totalIssues;
        }
        private double getEffortSumFromTypes() {
            return stack.issuesFromTypes.stream()
                    .map(i -> i.getEffortUntilDate(stack.week.getLastDay()))
                    .mapToDouble(DateTimeUtils::secondsToHours)
                    .sum();
        }
        private double getEffortSumFromStatuses() {
            return stack.issuesFromStatuses.stream()
                .map(i -> i.getEffortSumInSecondsFromStatusesUntilDate(statuses, stack.week.getLastDay()))
                .mapToDouble(DateTimeUtils::secondsToHours)
                .sum();
        }
    }
}
