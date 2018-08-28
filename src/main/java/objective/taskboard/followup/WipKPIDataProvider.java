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

import objective.taskboard.followup.kpi.WipChartDataSet;
import objective.taskboard.followup.kpi.WipDataPoint;
import objective.taskboard.followup.kpi.WipKPIService;
import objective.taskboard.utils.DateTimeUtils;

@Component
public class WipKPIDataProvider extends KPIByLevelDataProvider<WipChartDataSet,WipDataSet> {

    @Autowired
    private WipKPIService wipService;
    
    @Override
    protected WipChartDataSet transform(List<WipDataSet> wipDatas, FollowUpTimeline timeline, ZoneId timezone) {
        
        final List<WipDataPoint> rowsFilteredByTimeline = new LinkedList<>();
        for (int i = 0; i < wipDatas.size(); ++i) {
            
            WipDataSet ds = wipDatas.get(i);

            if(isEmpty(ds.rows))
                continue;

            Range<ZonedDateTime> dateRange = getDateRange(timeline, ds, timezone);
            rowsFilteredByTimeline.addAll(ds.rows.stream()
                    .filter(row -> dateRange.contains(row.date))
                    .map(row -> new WipDataPoint(row.date, row.type, row.status, row.count))
                    .collect(Collectors.toList()));
        }

        return new WipChartDataSet(rowsFilteredByTimeline);
    }
    
    @Override
    protected List<WipDataSet> getDataSets(FollowUpData followupData) {
        return wipService.getData(followupData);
    }

    private Range<ZonedDateTime> getDateRange(FollowUpTimeline timeline, WipDataSet ds, ZoneId timezone) {
        ZonedDateTime startDate = timeline.getStart()
                .map(d -> d.atStartOfDay(timezone))
                .orElseGet(() -> ds.rows.get(0).date);

        ZonedDateTime endDate = timeline.getEnd()
                .map(d -> d.atStartOfDay(timezone))
                .orElseGet(() -> ds.rows.get(ds.rows.size()-1).date);

        return DateTimeUtils.range(startDate, endDate);
    }

}
