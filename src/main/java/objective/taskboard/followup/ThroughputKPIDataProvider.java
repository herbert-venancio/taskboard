package objective.taskboard.followup;

import static org.springframework.util.CollectionUtils.isEmpty;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.followup.kpi.ThroughputChartDataSet;
import objective.taskboard.followup.kpi.ThroughputDataPoint;
import objective.taskboard.followup.kpi.ThroughputKPIService;
import objective.taskboard.utils.DateTimeUtils;

@Component
public class ThroughputKPIDataProvider extends KPIByLevelDataProvider<ThroughputChartDataSet,ThroughputDataSet> {

    @Autowired
    private ThroughputKPIService throughputService;

    @Override
    protected ThroughputChartDataSet transform(List<ThroughputDataSet> dataSets, FollowUpTimeline timeline, ZoneId timezone) {
        
        final List<ThroughputDataPoint> rowsFilteredByTimeline = new LinkedList<>();
        for (int i = 0; i < dataSets.size(); ++i) {
            ThroughputDataSet ds = dataSets.get(i);

            if(isEmpty(ds.rows))
                continue;

            Range<ZonedDateTime> dateRange = getDateRange(timeline, ds, timezone);
            rowsFilteredByTimeline.addAll(ds.rows.stream()
                    .filter(row -> dateRange.contains(row.date))
                    .map(row -> new ThroughputDataPoint(row.date, row.issueType, row.count))
                    .collect(Collectors.toList()));
        }
        return new ThroughputChartDataSet(rowsFilteredByTimeline);
    }

    
    @Override
    protected List<ThroughputDataSet> getDataSets(FollowUpData followupData) {
        return throughputService.getData(followupData);
    }
    
    private Range<ZonedDateTime> getDateRange(FollowUpTimeline timeline, ThroughputDataSet ds, ZoneId timezone) {
        ZonedDateTime startDate = timeline.getStart()
                .map(d -> d.atStartOfDay(timezone))
                .orElseGet(() -> ds.rows.get(0).date);

        ZonedDateTime endDate = timeline.getEnd()
                .map(d -> d.atStartOfDay(timezone))
                .orElseGet(() -> ds.rows.get(ds.rows.size()-1).date);

        return DateTimeUtils.range(startDate, endDate);
    }

}