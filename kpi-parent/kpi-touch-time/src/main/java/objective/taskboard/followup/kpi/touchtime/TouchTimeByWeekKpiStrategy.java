package objective.taskboard.followup.kpi.touchtime;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Range;

import objective.taskboard.followup.WeekRangeNormalizer;
import objective.taskboard.followup.kpi.IssueKpi;
import objective.taskboard.followup.kpi.filters.KpiWeekRange;

public abstract class TouchTimeByWeekKpiStrategy implements TouchTimeKpiStrategy<TouchTimeByWeekKpiDataPoint>{

    protected Range<LocalDate> projectRange;
    protected ZoneId timezone;
    protected List<IssueKpi> issues;
    protected Map<KpiWeekRange, List<IssueKpi>> issuesByWeek;

    protected TouchTimeByWeekKpiStrategy(Range<LocalDate> projectRange,
            ZoneId timezone, List<IssueKpi> issues) {
        this.projectRange = projectRange;
        this.timezone = timezone;
        this.issues = issues;
        this.issuesByWeek = aggregateIssuesByWeek();
    }

    protected abstract List<TouchTimeByWeekKpiDataPoint> getDataPoints();

    @Override
    public List<TouchTimeByWeekKpiDataPoint> getDataSet() {
        if (issues.isEmpty())
            return Collections.emptyList();
        return getDataPoints();
    }

    private Map<KpiWeekRange, List<IssueKpi>> aggregateIssuesByWeek() {
        return getWeeksStream().collect(Collectors.toMap(w -> w, this::filterIssuesFromWeek));
    }

    private Stream<KpiWeekRange> getWeeksStream() {
        Stream<Range<LocalDate>> weeksRanges = WeekRangeNormalizer.splitByWeek(projectRange, DayOfWeek.SUNDAY, DayOfWeek.SATURDAY);
        return weeksRanges.map(w -> new KpiWeekRange(w, timezone));
    }

    private List<IssueKpi> filterIssuesFromWeek(KpiWeekRange week){
        return issues.stream()
                .filter(week::progressOverlaps)
                .collect(Collectors.toList());
    }
}
