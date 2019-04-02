package objective.taskboard.followup;

import static java.util.stream.Collectors.toList;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;

import objective.taskboard.followup.kpi.KpiDataService;
import objective.taskboard.followup.kpi.properties.KpiCfdProperties;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.client.JiraIssueTypeDto;
import objective.taskboard.repository.ProjectFilterConfigurationCachedRepository;
import objective.taskboard.utils.DateTimeUtils;
import objective.taskboard.utils.RangeUtils;

@Component
public class CumulativeFlowDiagramDataProvider {

    @Autowired
    private ProjectFilterConfigurationCachedRepository projectRepository;

    @Autowired
    private KpiDataService kpiService;

    @Autowired
    private KpiCfdProperties properties;

    @Autowired
    private MetadataService metaDataService;

    public CumulativeFlowDiagramDataSet getCumulativeFlowDiagramDataSet(String project, String level) {
        if(!belongsToAnyProject(project))
            throw new IllegalArgumentException(String.format("Unknown project <%s>", project));

        FollowUpSnapshot followupData = kpiService.getSnapshotFromCurrentState(project, ZoneId.systemDefault());
        return transform(followupData, Level.valueOf(level.toUpperCase()));
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

    private CumulativeFlowDiagramDataSet transform(FollowUpSnapshot followUpDataSnapshot, Level selectedLevel) {
        List<SyntheticTransitionsDataSet> filteredSyntheticTransitions = getFilteredSyntheticTransitions(followUpDataSnapshot);

        ListMultimap<String, CumulativeFlowDiagramDataPoint> dataByStatus = LinkedListMultimap.create();

        for (int i = 0; i < filteredSyntheticTransitions.size(); ++i) {
            if(!selectedLevel.includeLevel(i))
                continue;
            SyntheticTransitionsDataSet ds = filteredSyntheticTransitions.get(i);

            if(isEmpty(ds.rows))
                continue;

            Sampler sampler = new Sampler(ds);

            Range<ZonedDateTime> dateRange = getDateRange(followUpDataSnapshot.getTimeline(), ds);
            DateTimeUtils.dayStream(dateRange)
                    .forEach(date -> dataByStatus.putAll(sampler.sample(date)));
        }

        return new CumulativeFlowDiagramDataSet(asMap(dataByStatus));
    }

    private boolean belongsToAnyProject(String projectKey) {
        return projectRepository.exists(projectKey);
    }

    private List<SyntheticTransitionsDataSet> getFilteredSyntheticTransitions(FollowUpSnapshot followUpDataSnapshot) {
        if (properties.getCumulativeFlowDiagram().getExcludeIssueTypes().isEmpty())
            return followUpDataSnapshot.getData().syntheticsTransitionsDsList;

        return followUpDataSnapshot.getData().syntheticsTransitionsDsList.stream()
                .map(syntheticsTransitions -> {
                    List<SyntheticTransitionsDataRow> filteredRows = syntheticsTransitions.rows.stream()
                        .filter(row -> !excludeIssueType(row.issueType))
                        .collect(toList());

                    return new SyntheticTransitionsDataSet(syntheticsTransitions.issueType, syntheticsTransitions.headers, filteredRows);
                })
                .collect(toList());
    }

    private boolean excludeIssueType(String issueTypeName) {
        JiraIssueTypeDto issueType = metaDataService.getIssueTypeByName(issueTypeName).orElseThrow(IllegalStateException::new);
        return properties.getCumulativeFlowDiagram().getExcludeIssueTypes().contains(issueType.getId());
    }

    private static class Sampler {

        private final Range<ZonedDateTime> dateRange;
        private final ListMultimap<ZonedDateTime, SyntheticTransitionsDataRow> dateRowMap;
        private final List<String> issueTypes;
        private final List<String> statuses;
        private final List<String> reverseStatuses;

        public Sampler(SyntheticTransitionsDataSet ds) {
            dateRange = RangeUtils.between(ds.rows.get(0).date, ds.rows.get(ds.rows.size() - 1).date);
            dateRowMap = ds.rows
                    .stream()
                    .collect(Multimaps.toMultimap(
                            row -> row.date
                            , Function.identity()
                            , LinkedListMultimap::create));

            issueTypes = ds.rows
                    .stream()
                    .map(row -> row.issueType)
                    .distinct()
                    .collect(Collectors.toList());
            int initial = ds.getInitialIndexStatusHeaders();
            statuses = new ArrayList<>(ds.headers.subList(initial, ds.headers.size()));
            Collections.reverse(statuses);
            reverseStatuses = ds.headers.subList(initial, ds.headers.size());
        }

        private ListMultimap<String, CumulativeFlowDiagramDataPoint> sample(final ZonedDateTime date) {
            ListMultimap<String, CumulativeFlowDiagramDataPoint> sample = LinkedListMultimap.create();
            if(dateRange.isAfter(date)) {
                statuses.forEach(status ->
                        issueTypes.forEach(issueType ->
                                sample.put(status, new CumulativeFlowDiagramDataPoint(issueType, date, 0))));
            } else {
                ZonedDateTime sampleDate = dateRange.contains(date) ? date : dateRange.getMaximum();
                dateRowMap.get(sampleDate).forEach(row -> {
                    for (int rowIndex = row.amountOfIssueInStatus.size() - 1; rowIndex >= 0; --rowIndex) {
                        String status = reverseStatuses.get(rowIndex);
                        int count = row.amountOfIssueInStatus.get(rowIndex);
                        sample.put(status, new CumulativeFlowDiagramDataPoint(row.issueType, date, count));
                    }
                });
            }

            return sample;
        }
    }

    private static Range<ZonedDateTime> getDateRange(FollowUpTimeline timeline, SyntheticTransitionsDataSet ds) {
        ZoneId zone = ZoneId.systemDefault();

        ZonedDateTime startDate = timeline.getStart()
                .map(d -> d.atStartOfDay(zone))
                .orElseGet(() -> ds.rows.get(0).date);

        ZonedDateTime endDate = timeline.getEnd()
                .map(d -> d.atStartOfDay(zone))
                .orElseGet(() -> ds.rows.get(ds.rows.size()-1).date);

        return RangeUtils.between(startDate, endDate);
    }

    private static <K, V> Map<K, List<V>> asMap(ListMultimap<K, V> multimap) {
        return new LinkedHashMap<>(Multimaps.asMap(multimap));
    }

}
