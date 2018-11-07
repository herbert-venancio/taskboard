package objective.taskboard.followup;

import static org.springframework.util.CollectionUtils.isEmpty;

import java.time.DayOfWeek;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.followup.kpi.ThroughputChartDataSet;
import objective.taskboard.followup.kpi.ThroughputDataPoint;
import objective.taskboard.followup.kpi.ThroughputKPIService;
import objective.taskboard.utils.RangeUtils;

@Component
public class ThroughputKPIDataProvider extends KPIByLevelDataProvider<ThroughputChartDataSet,ThroughputDataSet> {

    @Autowired
    private ThroughputKPIService throughputService;


    @Override
    protected ThroughputChartDataSet transform(List<ThroughputDataSet> dataSets, FollowUpTimeline timeline, ZoneId timezone) {
        
        final List<ThroughputDataPoint> rowsFilteredByTimeline = new LinkedList<>();
        for (ThroughputDataSet ds: dataSets) {

            if(isEmpty(ds.rows))
                continue;

            final ZonedDateTime dsStartDate = ds.rows.get(0).date;
            final ZonedDateTime dsEndDate = ds.rows.get(ds.rows.size() - 1).date;
            final Range<ZonedDateTime> dataSetDateRange = RangeUtils.between(dsStartDate, dsEndDate);

            final Range<ZonedDateTime> resultDateRange = WeekRangeNormalizer.normalizeWeekRange(timeline,
                    dataSetDateRange, timezone);

            final Map<ZonedDateTime, List<ThroughputRow>> filteredRows = ds.rows.stream()
                    .filter(row -> resultDateRange.contains(row.date))
                    .collect(Collectors.groupingBy(
                            row -> row.date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)),
                            LinkedHashMap::new, Collectors.toList()));

            filteredRows.forEach((sunday, rowsByWeek) -> {
                Map<String, Long> dataByType = rowsByWeek.stream()
                        .collect(Collectors.groupingBy(
                                throughputRow -> throughputRow.issueType,
                                LinkedHashMap::new,
                                Collectors.summingLong(row -> row.count)));
                dataByType.forEach((issueType, sum) -> rowsFilteredByTimeline.add(new ThroughputDataPoint(sunday, issueType, sum)));
            });
        }
        return new ThroughputChartDataSet(rowsFilteredByTimeline);
    }

    
    @Override
    protected List<ThroughputDataSet> getDataSets(FollowUpData followupData) {
        return throughputService.getData(followupData);
    }

}