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

import objective.taskboard.followup.kpi.WipKPIService;
import objective.taskboard.utils.DateTimeUtils;

@Component
public class WipKPIDataProvider {

    @Autowired
    private FollowUpSnapshotService snapshotService;
    
    @Autowired
    private WipKPIService wipService;
    
    public WipChartDataSet getWipChartDataSet(String projectKey, String level, ZoneId timezone) {

        FollowUpSnapshot followupData = snapshotService.getFromCurrentState(ZoneId.systemDefault(), projectKey);
        return transform(followupData, Level.valueOf(level.toUpperCase()), timezone);
    }
    
    private WipChartDataSet transform(FollowUpSnapshot followUpDataSnapshot, Level selectedLevel, ZoneId timezone) {
        FollowUpData followupData = followUpDataSnapshot.getData();
        List<WipDataSet> wipDatas = wipService.getData(followupData);
        
        final List<WipDataPoint> rowsFilteredByTimeline = new LinkedList<>();
        for (int i = 0; i < wipDatas.size(); ++i) {
            if(!selectedLevel.includeLevel(i))
                continue;
            WipDataSet ds = wipDatas.get(i);

            if(isEmpty(ds.rows))
                continue;

            Range<ZonedDateTime> dateRange = getDateRange(followUpDataSnapshot.getTimeline(), ds, timezone);
            rowsFilteredByTimeline.addAll(ds.rows.stream()
                    .filter(row -> dateRange.contains(row.date))
                    .map(row -> new WipDataPoint(row.date, row.type, row.status, row.count))
                    .collect(Collectors.toList()));
        }

        return new WipChartDataSet(rowsFilteredByTimeline);
    }

    enum Level {
        ALL(-1)
        , DEMAND(0)
        , FEATURE(1)
        , SUBTASK(2);

        public final int index;

        Level(int index) {
            this.index = index;
        }

        public boolean includeLevel(int index) {
            return this == ALL || this.index == index;
        }
    }
    
    private static Range<ZonedDateTime> getDateRange(FollowUpTimeline timeline, WipDataSet ds, ZoneId timezone) {
        ZonedDateTime startDate = timeline.getStart()
                .map(d -> d.atStartOfDay(timezone))
                .orElseGet(() -> ds.rows.get(0).date);

        ZonedDateTime endDate = timeline.getEnd()
                .map(d -> d.atStartOfDay(timezone))
                .orElseGet(() -> ds.rows.get(ds.rows.size()-1).date);

        return DateTimeUtils.range(startDate, endDate);
    }

}
