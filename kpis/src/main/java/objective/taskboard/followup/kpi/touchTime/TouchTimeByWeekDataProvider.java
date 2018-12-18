package objective.taskboard.followup.kpi.touchTime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import objective.taskboard.domain.ProjectFilterConfiguration;
import objective.taskboard.followup.ProjectDatesNotConfiguredException;
import objective.taskboard.followup.WeekRangeNormalizer;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.IssueKpiService;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.followup.kpi.WeekTimelineRange;
import objective.taskboard.followup.kpi.properties.KPIProperties;
import objective.taskboard.jira.ProjectService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.DateTimeUtils;
import objective.taskboard.utils.RangeUtils;

@Service
public class TouchTimeByWeekDataProvider implements TouchTimeProvider<TouchTimeChartByWeekDataSet>{

    private IssueKpiService issueKpiService;
    
    private ProjectService projectService;
    
    private KPIProperties kpiProperties;
    
    private JiraProperties jiraProperties;
    
    @Autowired
    public TouchTimeByWeekDataProvider(
            IssueKpiService issueKpiService,
            ProjectService projectService,
            KPIProperties kpiProperties,
            JiraProperties jiraProperties
            ) {
        this.issueKpiService = issueKpiService;
        this.projectService = projectService;
        this.kpiProperties = kpiProperties;
        this.jiraProperties = jiraProperties;
    }

    @Override
    public TouchTimeChartByWeekDataSet getDataSet(String projectKey, KpiLevel level, ZoneId timezone) {
        
        ProjectFilterConfiguration project = projectService.getTaskboardProjectOrCry(projectKey);
        List<IssueKpi> issues = issueKpiService.getIssuesFromCurrentState(projectKey, timezone,level);
        
        return new DataCollector(project, timezone, getProgressingStatuses(level)).getDataSet(issues);
    }

    private List<String> getProgressingStatuses(KpiLevel level) {
        final List<String> allProgressingStatuses = kpiProperties.getProgressingStatuses();
        return level.filterProgressingStatuses(allProgressingStatuses, jiraProperties);
    }
   
    private class DataCollector {
        private ProjectFilterConfiguration project;
        private List<String> progressingStatuses;
        private ZoneId timezone;

        public DataCollector(ProjectFilterConfiguration project, ZoneId timezone, List<String> progressingStatuses) {
            this.project = project;
            this.timezone = timezone;
            this.progressingStatuses = progressingStatuses;
        }
        
        private TouchTimeChartByWeekDataSet getDataSet(List<IssueKpi> issues) {
            Range<LocalDate> projectRange = getRangeOrCry();
            Stream<Range<LocalDate>> weeks = getWeeks(projectRange);
            
            return new TouchTimeChartByWeekDataSet(getPoints(weeks,issues));
        }
        
        private List<TouchTimeChartByWeekDataPoint> getPoints(Stream<Range<LocalDate>> weeks, List<IssueKpi> issues) {
            if(issues.isEmpty())
                return Collections.emptyList();
            
            return weeks.flatMap(week -> getPointsForDate(week,issues)).collect(Collectors.toList());
        }

        private Stream<TouchTimeChartByWeekDataPoint> getPointsForDate(Range<LocalDate> week, List<IssueKpi> issues) {
            
            return progressingStatuses.stream().map(status -> getPointForDateAndStatus(week,status,issues));
        }

        private TouchTimeChartByWeekDataPoint getPointForDateAndStatus(Range<LocalDate> week, String status, List<IssueKpi> issues) {
            TouchTimeFilter filter = new TouchTimeFilter(timezone, new WeekTimelineRange(week));
            List<IssueKpi> issuesToCount = issues.stream().filter(filter).collect(Collectors.toList());
            ZonedDateTime lastDayOfWeek = endOfWeek(week);
            double averageEffortInHours = issuesToCount.stream()
                .mapToLong(issue -> issue.getEffortUntilDate(status, lastDayOfWeek))
                .mapToDouble(DateTimeUtils::secondsToHours)
                .average()
                .orElse(0d);
            ZonedDateTime startOfWeek = startOfWeek(week);
            return new TouchTimeChartByWeekDataPoint(Date.from(startOfWeek.toInstant()), status, averageEffortInHours);
        }
        
        private ZonedDateTime startOfWeek(Range<LocalDate> week) {
            return week.getMinimum().atStartOfDay(timezone).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        }

        private ZonedDateTime endOfWeek(Range<LocalDate> week) {
            return week.getMaximum().atStartOfDay(timezone);
        }

        private Stream<Range<LocalDate>> getWeeks(Range<LocalDate> range) {
            return WeekRangeNormalizer.splitByWeek(range, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
        }

        private Range<LocalDate> getRangeOrCry() {
            
            Optional<LocalDate> opStartDate = project.getStartDate();
            Optional<LocalDate> opDeliveryDate = project.getDeliveryDate();
            if(!opStartDate.isPresent() || !opDeliveryDate.isPresent())
                throw new ProjectDatesNotConfiguredException();
            
            LocalDate startDate = opStartDate.get();
            LocalDate deliveryDate = opDeliveryDate.get();
            return RangeUtils.between(startDate, deliveryDate);
        }
        
    }
}
