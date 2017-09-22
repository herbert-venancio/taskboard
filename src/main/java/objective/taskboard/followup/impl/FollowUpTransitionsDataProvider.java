package objective.taskboard.followup.impl;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.Range;
import org.springframework.util.CollectionUtils;

import com.google.common.primitives.Ints;

import objective.taskboard.data.Changelog;
import objective.taskboard.data.Issue;
import objective.taskboard.followup.AnalyticsTransitionsDataRow;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.SyntheticTransitionsDataRow;
import objective.taskboard.followup.SyntheticTransitionsDataSet;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.utils.DateTimeUtils;

class FollowUpTransitionsDataProvider {

    private static final String HEADER_ISSUE_KEY_COLUMN_NAME = "PKEY";
    private static final String HEADER_DATE_COLUMN_NAME = "Date";

    private JiraProperties jiraProperties;

    public FollowUpTransitionsDataProvider(JiraProperties jiraProperties) {
        this.jiraProperties = jiraProperties;
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
        analyticsTransitionsDSs.add(getAnalyticsTransitionsDs("Demand", jiraProperties.getStatusPriorityOrder().getDemandsInOrder(), demands, timezone));
        analyticsTransitionsDSs.add(getAnalyticsTransitionsDs("Features", jiraProperties.getStatusPriorityOrder().getTasksInOrder(), features, timezone));
        analyticsTransitionsDSs.add(getAnalyticsTransitionsDs("Subtasks", jiraProperties.getStatusPriorityOrder().getSubtasksInOrder(), subtasks, timezone));
        return analyticsTransitionsDSs;
    }

    public AnalyticsTransitionsDataSet getAnalyticsTransitionsDs(String issueType, String[] statuses, List<Issue> issuesVisibleToUser, ZoneId timezone) {
        List<String> headers = new LinkedList<>();
        headers.add(HEADER_ISSUE_KEY_COLUMN_NAME);
        Collections.addAll(headers, statuses);
        List<AnalyticsTransitionsDataRow> rows = getAnalyticsTransitionsDataRows(statuses, issuesVisibleToUser, timezone);
        return new AnalyticsTransitionsDataSet(issueType, headers, rows);
    }

    private List<AnalyticsTransitionsDataRow> getAnalyticsTransitionsDataRows(String[] statuses, List<Issue> issuesVisibleToUser, ZoneId timezone) {
        List<AnalyticsTransitionsDataRow> rows = new LinkedList<>();
        for (Issue issue : issuesVisibleToUser) {
            Map<String, ZonedDateTime> lastTransitionDate = mapStatusLastTransitionDates(issue, statuses, timezone);
            rows.add(new AnalyticsTransitionsDataRow(issue.getIssueKey(), issue.getIssueTypeName(),
                    new LinkedList<>(lastTransitionDate.values())));
        }
        return rows;
    }

    private Map<String, ZonedDateTime> mapStatusLastTransitionDates(Issue issue, String[] statuses, ZoneId timezone) {
        final String firstState = statuses[0];
        Map<String, ZonedDateTime> lastTransitionDate = new LinkedHashMap<>();
        for (String status : statuses)
            lastTransitionDate.put(status, null);
        lastTransitionDate.put(firstState, DateTimeUtils.get(issue.getCreated(), timezone));
        int lastStatusIndex = ArrayUtils.indexOf(statuses, issue.getStatusName());
        for (Changelog change : issue.getChangelog())
            if ("status".equals(change.field)) {
                int statusIndex = ArrayUtils.indexOf(statuses, change.to);
                if (statusIndex != ArrayUtils.INDEX_NOT_FOUND && statusIndex <= lastStatusIndex)
                    lastTransitionDate.put(change.to, DateTimeUtils.get(change.timestamp, timezone));
            }
        return lastTransitionDate;
    }

    public List<SyntheticTransitionsDataSet> getSyntheticTransitionsDsList(List<AnalyticsTransitionsDataSet> analyticsTransitionsDSs) {
        return analyticsTransitionsDSs.stream()
                .map(this::getSyntheticTransitionsDs)
                .collect(Collectors.toList());
    }

    private SyntheticTransitionsDataSet getSyntheticTransitionsDs(AnalyticsTransitionsDataSet dataset) {
        List<String> headers = new LinkedList<>(dataset.headers);
        headers.set(0, HEADER_DATE_COLUMN_NAME);

        List<SyntheticTransitionsDataRow> dataRows = new LinkedList<>();
        if (!CollectionUtils.isEmpty(dataset.rows)) {
            Range<ZonedDateTime> dateRange = calculateInterval(dataset);
            for (ZonedDateTime date = dateRange.getMinimum(); !date.isAfter(dateRange.getMaximum()); date = date.plusDays(1)) {
                int[] issuesInStatusCount = countIssuesInStatus(dataset, date);
                dataRows.add(new SyntheticTransitionsDataRow(date, Ints.asList(issuesInStatusCount)));
            }
        }

        return new SyntheticTransitionsDataSet(dataset.issueType, headers, dataRows);
    }

    private int[] countIssuesInStatus(AnalyticsTransitionsDataSet dataset, ZonedDateTime date) {
        int[] issuesInStatusCount = new int[dataset.headers.size() - 1];
        Arrays.fill(issuesInStatusCount, 0);
        for (AnalyticsTransitionsDataRow row : dataset.rows) {
            Optional<Integer> index = getTransitionIndexByDate(row, DateTimeUtils.roundUp(date));
            if (index.isPresent())
                issuesInStatusCount[index.get()]++;
        }
        return issuesInStatusCount;
    }

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Range<ZonedDateTime> calculateInterval(AnalyticsTransitionsDataSet dataset) {
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

        return (Range) Range.between(DateTimeUtils.roundDown(firstDate), DateTimeUtils.roundUp(lastDate));
    }

    private Optional<Integer> getTransitionIndexByDate(AnalyticsTransitionsDataRow row, ZonedDateTime date) {
        List<ZonedDateTime> transitionsDates = row.transitionsDates;
        Integer index = null;
        for (int i = 0; i < transitionsDates.size(); ++i) {
            ZonedDateTime transitionDate = transitionsDates.get(i);
            if (transitionDate == null)
                continue;
            if (!date.isBefore(transitionDate))
                index = i;
        }
        return Optional.ofNullable(index);
    }
}
