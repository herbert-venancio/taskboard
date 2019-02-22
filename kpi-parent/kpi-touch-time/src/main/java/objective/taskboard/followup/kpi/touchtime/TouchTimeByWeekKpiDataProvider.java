package objective.taskboard.followup.kpi.touchtime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.WeekRangeNormalizer;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.properties.KpiTouchTimeProperties;
import objective.taskboard.followup.kpi.properties.TouchTimeSubtaskConfiguration;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.DateTimeUtils;
import objective.taskboard.utils.RangeUtils;

@Service
public class TouchTimeByWeekKpiDataProvider implements TouchTimeKpiDataProvider<TouchTimeByWeekKpiDataSet>{

    private IssueKpiService issueKpiService;

    private ProjectService projectService;

    private KpiTouchTimeProperties properties;

    private JiraProperties jiraProperties;

    @Autowired
    public TouchTimeByWeekKpiDataProvider(
            IssueKpiService issueKpiService,
            ProjectService projectService,
            KpiTouchTimeProperties properties,
            JiraProperties jiraProperties
            ) {
        this.issueKpiService = issueKpiService;
        this.projectService = projectService;
        this.properties = properties;
        this.jiraProperties = jiraProperties;
    }

    @Override
    public TouchTimeByWeekKpiDataSet getDataSet(String projectKey, KpiLevel level, ZoneId timezone) {

        ProjectFilterConfiguration project = projectService.getTaskboardProjectOrCry(projectKey);
        List<IssueKpi> issues = issueKpiService.getIssuesFromCurrentState(projectKey, timezone,level);
        if (issues.isEmpty()) {
            return new TouchTimeByWeekKpiDataSet(Collections.emptyList());
        }
        Range<LocalDate> projectRange = getRangeOrCry(project);
        DataCollector dataCollector = new DataCollectorFactory(projectRange, timezone, issues).getDataCollector(level);
        return dataCollector.getDataSet();
    }

    private Range<LocalDate> getRangeOrCry(ProjectFilterConfiguration project) {
        Optional<LocalDate> opStartDate = project.getStartDate();
        Optional<LocalDate> opDeliveryDate = project.getDeliveryDate();
        if(!opStartDate.isPresent() || !opDeliveryDate.isPresent())
            throw new ProjectDatesNotConfiguredException();

        LocalDate startDate = opStartDate.get();
        LocalDate deliveryDate = opDeliveryDate.get();
        return RangeUtils.between(startDate, deliveryDate);
    }

    private class DataCollectorFactory {
        private Range<LocalDate> projectRange;
        private ZoneId timezone;
        private List<IssueKpi> issues;

        private DataCollectorFactory (Range<LocalDate> projectRange, ZoneId timezone, List<IssueKpi> issues) {
            this.projectRange = projectRange;
            this.timezone = timezone;
            this.issues = issues;
        }
        private DataCollector getDataCollector(KpiLevel level) {
            if (level == KpiLevel.SUBTASKS)
                return new DataCollectorBySubtasksConfigs(projectRange, timezone, issues);
            return new DataCollectorByProgressingStatuses(projectRange, timezone, issues, level);
        }
    }

    private abstract class DataCollector {
        private Range<LocalDate> projectRange;
        private ZoneId timezone;
        private List<IssueKpi> issues;
        private Map<TouchTimeKpiWeekRange, List<IssueKpi>> issuesByWeek;

        public DataCollector(Range<LocalDate> projectRange, ZoneId timezone, List<IssueKpi> issues) {
            this.projectRange = projectRange;
            this.timezone = timezone;
            this.issues = issues;
            this.issuesByWeek = aggregateIssuesByWeek();
        }

        private Map<TouchTimeKpiWeekRange, List<IssueKpi>> aggregateIssuesByWeek() {
            return getWeeksStream().collect(Collectors.toMap(w -> w, w -> filterIssuesFromWeek(w)));
        }

        private Stream<TouchTimeKpiWeekRange> getWeeksStream() {
            Stream<Range<LocalDate>> weeksRanges = WeekRangeNormalizer.splitByWeek(projectRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
            return weeksRanges.map(w -> new TouchTimeKpiWeekRange(w, timezone));
        }

        private List<IssueKpi> filterIssuesFromWeek(TouchTimeKpiWeekRange week){
            return issues.stream()
                    .filter(week::progressOverlaps)
                    .collect(Collectors.toList());
        }

        public TouchTimeByWeekKpiDataSet getDataSet() {
            return new TouchTimeByWeekKpiDataSet(getDataPoints());
        }

        abstract List<TouchTimeByWeekKpiDataPoint> getDataPoints();

    }

    private class DataCollectorByProgressingStatuses extends DataCollector {

        private List<String> progressingStatuses;

        public DataCollectorByProgressingStatuses(Range<LocalDate> projectRange, ZoneId timezone, List<IssueKpi> issues, KpiLevel level) {
            super(projectRange, timezone, issues);
            this.progressingStatuses = getProgressingStatuses(level);
        }

        @Override
        List<TouchTimeByWeekKpiDataPoint> getDataPoints() {
            return super.issuesByWeek.entrySet().stream()
                    .sorted(Comparator.comparing(Entry::getKey))
                    .map(this::transformToDataPoints)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }

        private List<TouchTimeByWeekKpiDataPoint> transformToDataPoints(Entry<TouchTimeKpiWeekRange, List<IssueKpi>> entry) {
            TouchTimeKpiWeekRange week = entry.getKey();
            List<IssueKpi> issues = entry.getValue();
            return progressingStatuses.stream()
                .map(status -> new TouchTimeByWeekKpiDataPoint(
                        week.getFirstDay().toInstant(),
                        status,
                        getWeekAvgEffortForStatusFromIssues(week, status, issues)))
                .collect(Collectors.toList());
        }

        private double getWeekAvgEffortForStatusFromIssues(TouchTimeKpiWeekRange week, String status, List<IssueKpi> issues) {
            return issues.stream()
                .map(i -> i.getEffortFromStatusUntilDate(status, week.getLastDay()))
                .mapToDouble(DateTimeUtils::secondsToHours)
                .average().orElse(0D);
        }

        private List<String> getProgressingStatuses(KpiLevel level) {
            final List<String> allProgressingStatuses = properties.getProgressingStatuses();
            return level.filterProgressingStatuses(allProgressingStatuses, jiraProperties);
        }

    }

    private class DataCollectorBySubtasksConfigs extends DataCollector {
        private Table<TouchTimeKpiWeekRange, String, List<Double>> effortsByStackNameByWeek = HashBasedTable.create();

        public DataCollectorBySubtasksConfigs(Range<LocalDate> projectRange, ZoneId timezone, List<IssueKpi> issues) {
            super(projectRange, timezone, issues);
        }

        @Override
        List<TouchTimeByWeekKpiDataPoint> getDataPoints() {
            aggregateEffortAccordingToConfiguration();
            return transformToDataPoints();
        }

        private void aggregateEffortAccordingToConfiguration() {
            super.issuesByWeek.entrySet().forEach(entry -> aggregate(entry.getKey(), entry.getValue()));
        }

        private void aggregate(TouchTimeKpiWeekRange week, List<IssueKpi> issuesFromWeek) {
            List<IssueKpi> issuesFromWeekCopy = new LinkedList<>(issuesFromWeek);
            
            for (TouchTimeSubtaskConfiguration conf : properties.getTouchTimeSubtaskConfigs()) {
                List<IssueKpi> issuesSelectedByType = filterIssuesByTypes(conf.getTypeIds(), issuesFromWeek);
                effortsByStackNameByWeek.put(week,conf.getStackName(),getEffortsFromIssues(week, issuesSelectedByType));
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
                            c.getRowKey().getFirstDay().toInstant(),
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
}
