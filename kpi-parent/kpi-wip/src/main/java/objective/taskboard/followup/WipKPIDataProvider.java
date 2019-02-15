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
import java.util.stream.LongStream;

import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;

import objective.taskboard.followup.FollowUpData;
import objective.taskboard.followup.FollowUpTimeline;
import objective.taskboard.followup.kpi.WipChartDataSet;
import objective.taskboard.followup.kpi.WipDataPoint;
import objective.taskboard.followup.kpi.WipKPIService;
import objective.taskboard.utils.RangeUtils;

@Component
public class WipKPIDataProvider extends KPIByLevelDataProvider<WipChartDataSet, WipDataSet> {

    @Autowired
    private WipKPIService wipService;


    @Override
    protected WipChartDataSet transform(List<WipDataSet> wipDatas, FollowUpTimeline timeline, ZoneId timezone) {

        final List<WipDataPoint> rowsFilteredByTimeline = new LinkedList<>();
        for (WipDataSet ds : wipDatas) {

            if (isEmpty(ds.rows))
                continue;

            final ZonedDateTime dsStartDate = ds.rows.get(0).date;
            final ZonedDateTime dsEndDate = ds.rows.get(ds.rows.size() - 1).date;
            final Range<ZonedDateTime> dataSetDateRange = RangeUtils.between(dsStartDate, dsEndDate);

            final Range<ZonedDateTime> resultDateRange = WeekRangeNormalizer.normalizeWeekRange(timeline,
                    dataSetDateRange, timezone);

            final Map<ZonedDateTime, List<WipRow>> filteredRows = ds.rows.stream()
                    .filter(row -> resultDateRange.contains(row.date))
                    .collect(Collectors.groupingBy(
                            row -> row.date.with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY)),
                            LinkedHashMap::new, Collectors.toList()));

            filteredRows.forEach((sunday, rowsByWeek) -> {
                Table<String, String, LongStream> weekWipByTypeAndStatus = rowsByWeek.stream()
                        .collect(Tables.toTable(
                                row -> row.type,
                                row -> row.status,
                                row -> LongStream.of(row.count),
                                LongStream::concat,
                                HashBasedTable::create));

                rowsFilteredByTimeline.addAll(
                        weekWipByTypeAndStatus.cellSet().stream()
                                .map(cell ->
                                    new WipDataPoint(
                                        sunday,
                                        cell.getRowKey(),
                                        cell.getColumnKey(),
                                        cell.getValue().average().orElse(0.0)))
                                .collect(Collectors.toList()));
            });
        }

        return new WipChartDataSet(rowsFilteredByTimeline);
    }

    @Override
    protected List<WipDataSet> getDataSets(FollowUpData followupData) {
        return wipService.getData(followupData);
    }
}
