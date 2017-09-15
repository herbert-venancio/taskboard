package objective.taskboard.followup.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.springframework.util.CollectionUtils;

import com.google.common.primitives.Ints;

import objective.taskboard.data.Changelog;
import objective.taskboard.data.Issue;
import objective.taskboard.followup.AnalyticsTransitionsDataRow;
import objective.taskboard.followup.AnalyticsTransitionsDataSet;
import objective.taskboard.followup.SyntheticTransitionsDataRow;
import objective.taskboard.followup.SyntheticTransitionsDataSet;
import objective.taskboard.jira.JiraProperties;

class FollowUpTransitionsDataProvider {

    private static final String HEADER_ISSUE_KEY_COLUMN_NAME = "PKEY";
    private static final String HEADER_DATE_COLUMN_NAME = "Date";

    private JiraProperties jiraProperties;

    public FollowUpTransitionsDataProvider(JiraProperties jiraProperties) {
        this.jiraProperties = jiraProperties;
    }

    public List<AnalyticsTransitionsDataSet> getAnalyticsTransitionsDsList(List<Issue> issuesVisibleToUser) {
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
        analyticsTransitionsDSs.add(getAnalyticsTransitionsDs("Demand", reversedCopy(jiraProperties.getStatusPriorityOrder().getDemands()), demands));
        analyticsTransitionsDSs.add(getAnalyticsTransitionsDs("Features", reversedCopy(jiraProperties.getStatusPriorityOrder().getTasks()), features));
        analyticsTransitionsDSs.add(getAnalyticsTransitionsDs("Subtasks", reversedCopy(jiraProperties.getStatusPriorityOrder().getSubtasks()), subtasks));
        return analyticsTransitionsDSs;
    }

    public AnalyticsTransitionsDataSet getAnalyticsTransitionsDs(String issueType, String[] statuses, List<Issue> issuesVisibleToUser) {
        List<String> headers = new LinkedList<>();
        headers.add(HEADER_ISSUE_KEY_COLUMN_NAME);
        Collections.addAll(headers, statuses);
        List<AnalyticsTransitionsDataRow> rows = getAnalyticsTransitionsDataRows(statuses, issuesVisibleToUser);
        return new AnalyticsTransitionsDataSet(issueType, headers, rows);
    }

    private List<AnalyticsTransitionsDataRow> getAnalyticsTransitionsDataRows(String[] statuses, List<Issue> issuesVisibleToUser) {
        final String firstState = statuses[0];
        List<AnalyticsTransitionsDataRow> rows = new LinkedList<>();
        for (Issue issue : issuesVisibleToUser) {
            Map<String, DateTime> lastTransitionDate = new LinkedHashMap<>();
            for (String status : statuses)
                lastTransitionDate.put(status, null);
            lastTransitionDate.put(firstState, new DateTime(issue.getStartDateStepMillis()));
            int lastStatusIndex = ArrayUtils.indexOf(statuses, issue.getStatusName());
            for (Changelog change : issue.getChangelog())
                if ("status".equals(change.field) && ArrayUtils.indexOf(statuses, change.to) <= lastStatusIndex)
                    lastTransitionDate.put(change.to, change.timestamp);
            rows.add(new AnalyticsTransitionsDataRow(issue.getIssueKey(),
                    new LinkedList<>(lastTransitionDate.values())));
        }
        return rows;
    }

    public List<SyntheticTransitionsDataSet> getSyntheticTransitionsDsList(List<AnalyticsTransitionsDataSet> analyticsTransitionsDSs) {
        return analyticsTransitionsDSs.stream()
                .map(this::getSyntheticTransitionsDs)
                .collect(Collectors.toList());
    }

    private SyntheticTransitionsDataSet getSyntheticTransitionsDs(AnalyticsTransitionsDataSet dataset) {
        if (CollectionUtils.isEmpty(dataset.rows))
            return null;

        Interval dateRange = calculateInterval(dataset);


        List<SyntheticTransitionsDataRow> dataRows = new LinkedList<>();
        for (DateTime date = dateRange.getStart(); date.isBefore(dateRange.getEnd()); date = date.plusDays(1)) {
            int[] issuesInStatusCount = new int[dataset.headers.size() - 1];
            Arrays.fill(issuesInStatusCount, 0);
            for (AnalyticsTransitionsDataRow row : dataset.rows) {
                Optional<Integer> index = getTransitionIndexByDate(row, date);
                if (index.isPresent())
                    issuesInStatusCount[index.get()]++;
            }
            dataRows.add(new SyntheticTransitionsDataRow(date, Ints.asList(issuesInStatusCount)));
        }

        List<String> headers = dataset.headers;
        headers.set(0, HEADER_DATE_COLUMN_NAME);

        return new SyntheticTransitionsDataSet(dataset.issueType, headers, dataRows);
    }

    private Interval calculateInterval(AnalyticsTransitionsDataSet dataset) {
        DateTime firstDate = null;
        DateTime lastDate = null;
        for (AnalyticsTransitionsDataRow row : dataset.rows) {
            for (DateTime date : row.transitionsDates) {
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

        return new Interval(firstDate.dayOfMonth().roundFloorCopy(), lastDate.plusDays(1));
    }

    private Optional<Integer> getTransitionIndexByDate(AnalyticsTransitionsDataRow row, DateTime date) {
        List<DateTime> transitionsDates = row.transitionsDates;
        Integer index = null;
        for (int i = 0; i < transitionsDates.size(); ++i) {
            DateTime transitionDate = transitionsDates.get(i);
            if (transitionDate == null)
                continue;
            if (!date.isBefore(transitionDate))
                index = i;
        }
        return Optional.ofNullable(index);
    }

    private String[] reversedCopy(String[] original) {
        String[] clone = original.clone();
        ArrayUtils.reverse(clone);
        return clone;
    }
}
