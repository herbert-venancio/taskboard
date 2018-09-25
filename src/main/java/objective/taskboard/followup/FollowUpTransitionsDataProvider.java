package objective.taskboard.followup;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.google.common.primitives.Ints;

import objective.taskboard.data.Issue;
import objective.taskboard.followup.kpi.KpiLevel;
import objective.taskboard.jira.MetadataService;
import objective.taskboard.jira.properties.JiraProperties;
import objective.taskboard.utils.DateTimeUtils;

public class FollowUpTransitionsDataProvider {
    
    public static final String TYPE_DEMAND = "Demand";
    public static final String TYPE_FEATURES = "Features";
    public static final String TYPE_SUBTASKS = "Subtasks";

    private static final long STATUS_CATEGORY_DONE = 3L;

    private static final String HEADER_ISSUE_KEY_COLUMN_NAME = "PKEY";
    public static final String HEADER_ISSUE_TYPE_COLUMN_NAME = "Type";
    private static final String HEADER_DATE_COLUMN_NAME = "Date";

    private JiraProperties jiraProperties;
    private MetadataService metadataService;
    private IssueTransitionService transitionService;

    public FollowUpTransitionsDataProvider(JiraProperties jiraProperties, IssueTransitionService transitionService, MetadataService metadataService) {
        this.jiraProperties = jiraProperties;
        this.transitionService = transitionService;
        this.metadataService = metadataService;
    }

    public List<AnalyticsTransitionsDataSet> getAnalyticsTransitionsDsList(List<Issue> issuesVisibleToUser, ZoneId timezone) {
        List<Issue> demands = new LinkedList<>();
        List<Issue> features = new LinkedList<>();
        List<Issue> subtasks = new LinkedList<>();
        issuesVisibleToUser.forEach(issue -> {
            if (issue.isDemand())
                demands.add(issue);
            else if (issue.isFeature())
                features.add(issue);
            else if (issue.isSubTask())
                subtasks.add(issue);
        });

        List<AnalyticsTransitionsDataSet> analyticsTransitionsDSs = new LinkedList<>();
        analyticsTransitionsDSs.add(getAnalyticsTransitionsDs(KpiLevel.DEMAND, demands, timezone));
        analyticsTransitionsDSs.add(getAnalyticsTransitionsDs(KpiLevel.FEATURES, features, timezone));
        analyticsTransitionsDSs.add(getAnalyticsTransitionsDs(KpiLevel.SUBTASKS, subtasks, timezone));
        return analyticsTransitionsDSs;
    }

    private AnalyticsTransitionsDataSet getAnalyticsTransitionsDs(KpiLevel level, List<Issue> issuesVisibleToUser, ZoneId timezone) {
        List<String> headers = new LinkedList<>();
        headers.add(HEADER_ISSUE_KEY_COLUMN_NAME);
        headers.add(HEADER_ISSUE_TYPE_COLUMN_NAME);
        Collections.addAll(headers, level.getStatusPriorityOrder(jiraProperties));
        List<AnalyticsTransitionsDataRow> rows = getAnalyticsTransitionsDataRows(level, issuesVisibleToUser, timezone);
        return new AnalyticsTransitionsDataSet(level.getName(), headers, rows);
    }

    private List<AnalyticsTransitionsDataRow> getAnalyticsTransitionsDataRows(KpiLevel level, List<Issue> issuesVisibleToUser, ZoneId timezone) {
        
        String[] statuses = level.getStatusPriorityOrder(jiraProperties);
        
        List<AnalyticsTransitionsDataRow> rows = issuesVisibleToUser.stream()
                .map( issue -> { 
                    Map<String,ZonedDateTime> transitions = transitionService.getTransitions(issue, timezone, statuses);
                    return new AnalyticsTransitionsDataRow(issue.getIssueKey(), issue.getIssueTypeName(), new LinkedList<>(transitions.values()));
                })
                .collect(Collectors.toList());
        
        return rows;
    }

    public List<SyntheticTransitionsDataSet> getSyntheticTransitionsDsList(List<AnalyticsTransitionsDataSet> analyticsTransitionsDSs) {
        List<String[]> statuses = Arrays.asList(
                jiraProperties.getStatusPriorityOrder().getDemandsInOrder()
                , jiraProperties.getStatusPriorityOrder().getTasksInOrder()
                , jiraProperties.getStatusPriorityOrder().getSubtasksInOrder());
        return IntStream.range(0, 3)
                .mapToObj(i -> getSyntheticTransitionsDs(statuses.get(i), analyticsTransitionsDSs.get(i)))
                .collect(Collectors.toList());
    }

    private SyntheticTransitionsDataSet getSyntheticTransitionsDs(String[] statuses, AnalyticsTransitionsDataSet dataset) {
        List<String> headers = new LinkedList<>();
        headers.add(HEADER_DATE_COLUMN_NAME);
        headers.add(HEADER_ISSUE_TYPE_COLUMN_NAME);
        List<String> doneStatuses = metadataService.getStatusesMetadata().values()
                .stream()
                .filter(status -> status.statusCategory.id == STATUS_CATEGORY_DONE)
                .map(status -> status.name)
                .collect(Collectors.toList());
        Collections.addAll(headers, mergeDoneStatusesHeaders(statuses, doneStatuses));

        return getSyntheticTransitionsDs(headers, statuses, doneStatuses, dataset);
    }

    public static SyntheticTransitionsDataSet getSyntheticTransitionsDs(List<String> headers, String[] statuses, List<String> doneStatuses, AnalyticsTransitionsDataSet dataset) {
        List<SyntheticTransitionsDataRow> dataRows = new LinkedList<>();
        if (!CollectionUtils.isEmpty(dataset.rows)) {
            Set<String> types = extractIssueTypes(dataset);
            Range<ZonedDateTime> dateRange = calculateInterval(dataset);
            for (ZonedDateTime date = dateRange.getMinimum(); !date.isAfter(dateRange.getMaximum()); date = date.plusDays(1)) {
                for(String issueType : types) {
                    int[] issuesInStatusCount = countIssuesInStatus(issueType, statuses, dataset, date);
                    issuesInStatusCount = mergeDoneStatusesCount(statuses, doneStatuses, issuesInStatusCount);
                    dataRows.add(new SyntheticTransitionsDataRow(date, issueType, Ints.asList(issuesInStatusCount)));
                }
            }
        }

        return new SyntheticTransitionsDataSet(dataset.issueType, headers, dataRows);
    }

    private static int[] countIssuesInStatus(String issueType, String[] statuses, AnalyticsTransitionsDataSet dataset, ZonedDateTime date) {
        int[] issuesInStatusCount = new int[statuses.length];
        Arrays.fill(issuesInStatusCount, 0);
        dataset.rows.stream()
                .filter(row -> issueType.equals(row.issueType))
                .forEach(row -> {
                    Optional<Integer> index = getTransitionIndexByDate(row, DateTimeUtils.roundUp(date));
                    if (index.isPresent())
                        issuesInStatusCount[index.get()]++;
                });
        return issuesInStatusCount;
    }

    private static String[] mergeDoneStatusesHeaders(String[] statuses, List<String> doneStatuses) {
        if (doneStatuses.isEmpty())
            return statuses;

        List<String> statusList = new ArrayList<>();
        List<String> doneList = new ArrayList<>();
        for (String status : statuses) {
            if (doneStatuses.contains(status))
                doneList.add(status);
            else
                statusList.add(status);
        }
        if (doneList.isEmpty())
            return statuses;

        statusList.add(0, StringUtils.join(doneList, "/"));
        return statusList.toArray(new String[0]);
    }

    private static int[] mergeDoneStatusesCount(String[] statuses, List<String> doneStatuses, int[] issuesInStatusCount) {
        if (doneStatuses.isEmpty())
            return issuesInStatusCount;

        List<Integer> issuesInStatusList = new ArrayList<>();
        int doneStatusesCount = 0;
        boolean hasCountToMerge = false;
        for (int i = 0; i < statuses.length; ++i) {
            String status = statuses[i];
            int count = issuesInStatusCount[i];
            if (doneStatuses.contains(status)) {
                hasCountToMerge = true;
                doneStatusesCount += count;
            } else {
                issuesInStatusList.add(count);
            }
        }
        if (!hasCountToMerge)
            return issuesInStatusCount;

        issuesInStatusList.add(0, doneStatusesCount);
        return Ints.toArray(issuesInStatusList);
    }

    private static Set<String> extractIssueTypes(AnalyticsTransitionsDataSet dataset) {
        return dataset.rows.stream()
                .map(row -> row.issueType)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

	public static Range<ZonedDateTime> calculateInterval(AnalyticsTransitionsDataSet dataset) {
        ZonedDateTime firstDate = null;
        ZonedDateTime lastDate = null;
        for (AnalyticsTransitionsDataRow row : dataset.rows) {
            for (ZonedDateTime date : row.transitionsDates) {
                if (date == null)
                    continue;
                if (firstDate == null || date.isBefore(firstDate))
                    firstDate = date;
                if (lastDate == null || date.isAfter(lastDate))
                    lastDate = date;
            }
        }

        if (firstDate == null || lastDate == null)
            throw new IllegalArgumentException("Invalid dates.");

        return DateTimeUtils.range(DateTimeUtils.roundDown(firstDate), DateTimeUtils.roundUp(lastDate));
    }

    private static Optional<Integer> getTransitionIndexByDate(AnalyticsTransitionsDataRow row, ZonedDateTime date) {
        List<ZonedDateTime> transitionsDates = row.transitionsDates;
        Integer index = null;
        for (int i = transitionsDates.size() - 1; i >= 0; --i) {
            ZonedDateTime transitionDate = transitionsDates.get(i);
            if (transitionDate == null)
                continue;
            if (!date.isBefore(transitionDate))
                index = i;
        }
        return Optional.ofNullable(index);
    }
    
}
