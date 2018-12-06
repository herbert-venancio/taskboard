/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */
package objective.taskboard.followup;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_DEMAND;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_FEATURES;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_SUBTASKS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.primitives.Ints;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import objective.taskboard.Constants;
import objective.taskboard.utils.DateTimeUtils;
import objective.taskboard.utils.DateTimeUtils.ZonedDateTimeAdapter;

public class FollowUpHelper {

    private static final String TIMEZONE_ID = "America/Sao_Paulo";
    public static final String COST_CENTER_FIELD_ID = "customfield_10390";
    public static final String COST_CENTER_FIELD_NAME = "Cost Center/Project Requester";

    public static FromJiraDataRow getDefaultFromJiraDataRow() {
        return getDefaultFromJiraDataRow("Doing", 1D, "M", "Type");
    }
    
    public static FromJiraDataRow getDefaultFromJiraDataRow(String subtaskStatus, Double taskballpark, String tshirtSize, String queryPlan) {
        FromJiraDataRow followUpData = new FromJiraDataRow();
        followUpData.planningType = "Ballpark";
        followUpData.project = "PROJECT TEST";
        followUpData.demandType = "Demand";
        followUpData.demandStatus = "Doing";
        followUpData.demandId = 1L;
        followUpData.demandNum = "I-1";
        followUpData.demandSummary = "Summary Demand";
        followUpData.demandDescription = "Description Demand";
        followUpData.demandStatusPriority = 0;
        followUpData.demandPriorityOrder = 0l;
        followUpData.demandStartDateStepMillis = 0L;
        followUpData.demandAssignee = "assignee.demand.test";
        followUpData.demandDueDate = DateTimeUtils.get(DateTimeUtils.parseDateTime("2025-05-25"), ZoneId.of(TIMEZONE_ID));
        followUpData.demandCreated = DateTimeUtils.get(DateTimeUtils.parseDateTime("2012-01-01"), ZoneId.of(TIMEZONE_ID));
        followUpData.demandLabels = "";
        followUpData.demandComponents = "";
        followUpData.demandReporter = "reporter.demand.test";
        followUpData.demandCoAssignees = "";
        followUpData.demandClassOfService = "Standard";
        followUpData.demandUpdatedDate = DateTimeUtils.get(DateTimeUtils.parseDateTime("2012-02-01"), ZoneId.of(TIMEZONE_ID));
        followUpData.demandCycletime = 1.111111;
        followUpData.demandIsBlocked = false;
        followUpData.demandLastBlockReason = "Demand last block reason";
        followUpData.demandExtraFields = null;

        followUpData.taskType = "Feature";
        followUpData.taskStatus = subtaskStatus;
        followUpData.taskId = 2L;
        followUpData.taskNum = "I-2";
        followUpData.taskSummary = "Summary Feature";
        followUpData.taskDescription = "Description Feature";
        followUpData.taskFullDescription = "Full Description Feature";
        followUpData.taskAdditionalEstimatedHours = 80.0;
        followUpData.taskRelease = "Release";
        followUpData.taskStatusPriority = 0;
        followUpData.taskPriorityOrder = 0l;
        followUpData.taskStartDateStepMillis = 0L;
        followUpData.taskAssignee = "assignee.task.test";
        followUpData.taskDueDate = DateTimeUtils.get(DateTimeUtils.parseDateTime("2025-05-24"), ZoneId.of(TIMEZONE_ID));
        followUpData.taskCreated = DateTimeUtils.get(DateTimeUtils.parseDateTime("2012-01-02"), ZoneId.of(TIMEZONE_ID));
        followUpData.taskLabels = "";
        followUpData.taskComponents = "";
        followUpData.taskReporter = "reporter.demand.test";
        followUpData.taskCoAssignees = "";
        followUpData.taskClassOfService = "Standard";
        followUpData.taskUpdatedDate = DateTimeUtils.get(DateTimeUtils.parseDateTime("2012-02-02"), ZoneId.of(TIMEZONE_ID));
        followUpData.taskCycletime = 2.222222;
        followUpData.taskIsBlocked = false;
        followUpData.taskLastBlockReason = "Task last block reason";
        followUpData.taskExtraFields = null;

        followUpData.subtaskType = "Sub-task";
        followUpData.subtaskStatus = subtaskStatus;
        followUpData.subtaskId = 3L;
        followUpData.subtaskNum = "I-3";
        followUpData.subtaskSummary = "Summary Sub-task";
        followUpData.subtaskDescription = "Description Sub-task";
        followUpData.subtaskFullDescription = "Full Description Sub-task";
        followUpData.subtaskStatusPriority = 0;
        followUpData.subtaskPriorityOrder = 0l;
        followUpData.subtaskStartDateStepMillis = 0L;
        followUpData.subtaskAssignee = "assignee.subtask.test";
        followUpData.subtaskDueDate = DateTimeUtils.get(DateTimeUtils.parseDateTime("2025-05-23"), ZoneId.of(TIMEZONE_ID));
        followUpData.subtaskCreated = DateTimeUtils.get(DateTimeUtils.parseDateTime("2012-01-03"), ZoneId.of(TIMEZONE_ID));
        followUpData.subtaskLabels = "";
        followUpData.subtaskComponents = "";
        followUpData.subtaskReporter = "reporter.subtask.test";
        followUpData.subtaskCoAssignees = "";
        followUpData.subtaskClassOfService = "Standard";
        followUpData.subtaskUpdatedDate = DateTimeUtils.get(DateTimeUtils.parseDateTime("2012-02-03"), ZoneId.of(TIMEZONE_ID));
        followUpData.subtaskCycletime = 3.333333;
        followUpData.subtaskIsBlocked = false;
        followUpData.subtaskLastBlockReason = "Subtask last block reason";
        followUpData.subtaskExtraFields = null;

        followUpData.tshirtSize = tshirtSize;
        followUpData.worklog = 1D;
        followUpData.wrongWorklog = 1D;
        followUpData.demandBallpark = 1D;
        followUpData.taskBallpark = taskballpark;
        followUpData.queryType = queryPlan;

        return followUpData;
    }

    public static FollowUpData getDefaultFollowupData() {
        return new FollowUpData(new FromJiraDataSet(Constants.FROMJIRA_HEADERS, getDefaultFromJiraDataRowList()),
                getDefaultAnalyticsTransitionsDataSet(), getDefaultSyntheticTransitionsDataSet());
    }


    public static FollowUpData getDefaultFollowupDataWithExtraFields() {
        Map<String, Set<String>> extraFieldsHeaders = new LinkedHashMap<>();
        extraFieldsHeaders.put(TYPE_DEMAND, emptySet());
        extraFieldsHeaders.put(TYPE_FEATURES, singleton(COST_CENTER_FIELD_ID));
        extraFieldsHeaders.put(TYPE_SUBTASKS, emptySet());

        FromJiraDataRow row = getDefaultFromJiraDataRow();
        row.taskExtraFields = singletonMap(COST_CENTER_FIELD_ID, "Taskboard");
        return new FollowUpData(new FromJiraDataSet(Constants.FROMJIRA_HEADERS, extraFieldsHeaders, singletonList(row)),
                getDefaultAnalyticsTransitionsDataSet(), getDefaultSyntheticTransitionsDataSet());
    }

    public static FollowUpData getEmptyFollowupData() {
        return new FollowUpData(new FromJiraDataSet(Constants.FROMJIRA_HEADERS, emptyList()), emptyList(), emptyList());
    }

    public static FollowUpData getFollowupData(FromJiraDataRow... rows) {
        return new FollowUpData(new FromJiraDataSet(Constants.FROMJIRA_HEADERS, asList(rows)), emptyList(), emptyList());
    }

    public static List<FromJiraDataRow> getDefaultFromJiraDataRowList() {
        return singletonList(getDefaultFromJiraDataRow());
    }

    public static FollowUpData getFromFile() {
        Gson gson = new GsonBuilder().registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter()).create();

        try(InputStream file = FollowUpHelper.class.getResourceAsStream("jiradata.json")) {
            return gson.fromJson(new InputStreamReader(file), FollowUpData.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void toJsonFile(FollowUpData data, File file) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeAdapter())
                .setPrettyPrinting()
                .create();

        try(OutputStream stream = new FileOutputStream(file)) {
            OutputStreamWriter writer = new OutputStreamWriter(stream);
            gson.toJson(data, writer);
            writer.flush();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static List<WipDataSet> getDefaultWipDataSet(){
        
        List<WipRow> demandRows = asList(
                                    makeDefaultWipRow("2017-09-25", "Demand", 0l),
                                    makeDefaultWipRow("2017-09-25", "OS", 0l),
                                    makeDefaultWipRow("2017-09-26", "Demand", 1l),
                                    makeDefaultWipRow("2017-09-26", "OS", 1l),
                                    makeDefaultWipRow("2017-09-27", "Demand", 0l),
                                    makeDefaultWipRow("2017-09-27", "OS", 1l)
                                    );
        List<WipRow> featureRows = asList(
                                    makeDefaultWipRow("2017-09-25", "Feature", 0l),
                                    makeDefaultWipRow("2017-09-26", "Feature", 1l)
                                    );
        List<WipRow> subtaskRows = asList(
                                    makeDefaultWipRow("2017-09-25", "Sub-task", 0l)
                                    );
        
        return asList(new WipDataSet(TYPE_DEMAND, demandRows),new WipDataSet(TYPE_FEATURES,featureRows),new WipDataSet(TYPE_SUBTASKS, subtaskRows));
    }
    
    private static WipRow makeDefaultWipRow(String date, String type, Long count) {
        return new WipRow(DateTimeUtils.parseDateTime(date),type,"Doing",count);
    }
    
    public static List<ThroughputDataSet> getDefaultThroughputDataSet(){
        List<ThroughputRow> demandRows = asList(
                makeDefaultThroughputRow("2017-09-25", "Demand", 0l),
                makeDefaultThroughputRow("2017-09-25", "OS", 0l),
                makeDefaultThroughputRow("2017-09-26", "Demand", 0l),
                makeDefaultThroughputRow("2017-09-26", "OS", 0l),
                makeDefaultThroughputRow("2017-09-27", "Demand", 1l),
                makeDefaultThroughputRow("2017-09-27", "OS", 0l)
                );
        List<ThroughputRow> featureRows = asList(
                makeDefaultThroughputRow("2017-09-25", "Feature", 0l),
                makeDefaultThroughputRow("2017-09-26", "Feature", 0l)
                );
        List<ThroughputRow> subtaskRows = asList(
                makeDefaultThroughputRow("2017-09-25", "Sub-task", 0l)
                );
        
        return asList(new ThroughputDataSet(TYPE_DEMAND, demandRows),new ThroughputDataSet(TYPE_FEATURES,featureRows),new ThroughputDataSet(TYPE_SUBTASKS, subtaskRows));
    }
    
    private static ThroughputRow makeDefaultThroughputRow(String date, String type, Long count) {
        return new ThroughputRow(DateTimeUtils.parseDateTime(date),type,count);
    }
    
    public static List<AnalyticsTransitionsDataSet> getDefaultAnalyticsTransitionsDataSet() {
        List<String> headers = new LinkedList<>();
        headers.add("PKEY");
        headers.add("ISSUE_TYPE");
        headers.add("Done");
        headers.add("Doing");
        headers.add("To Do");

        AnalyticsTransitionsDataRow rowDemand = new AnalyticsTransitionsDataRow("I-1", "Demand"
                , asList(
                        DateTimeUtils.parseDateTime("2017-09-27")
                        , DateTimeUtils.parseDateTime("2017-09-26")
                        , DateTimeUtils.parseDateTime("2017-09-25")));

        AnalyticsTransitionsDataRow rowOs = new AnalyticsTransitionsDataRow("I-4", "OS"
                , asList(
                        null
                        , DateTimeUtils.parseDateTime("2017-09-26")
                        , DateTimeUtils.parseDateTime("2017-09-25")));

        AnalyticsTransitionsDataRow rowFeature = new AnalyticsTransitionsDataRow("I-2", "Feature"
                , asList(
                        null
                        , DateTimeUtils.parseDateTime("2017-09-26")
                        , DateTimeUtils.parseDateTime("2017-09-25")));

        AnalyticsTransitionsDataRow rowSubtask = new AnalyticsTransitionsDataRow("I-3", "Sub-task"
                , asList(
                        null
                        , null
                        , DateTimeUtils.parseDateTime("2017-09-25")));

        return asList(new AnalyticsTransitionsDataSet(TYPE_DEMAND, headers, asList(rowDemand, rowOs)),
                new AnalyticsTransitionsDataSet(TYPE_FEATURES, headers, asList(rowFeature)),
                new AnalyticsTransitionsDataSet(TYPE_SUBTASKS, headers, asList(rowSubtask)));
    }

    public static List<AnalyticsTransitionsDataSet> getBiggerAnalyticsTransitionsDataSet() {
        List<String> demandHeaders = new LinkedList<>();
        demandHeaders.add("PKEY");
        demandHeaders.add("ISSUE_TYPE");
        demandHeaders.add("Done");
        demandHeaders.add("Doing");
        demandHeaders.add("To Do");

        List<String> featureHeaders = new LinkedList<>();
        featureHeaders.add("PKEY");
        featureHeaders.add("ISSUE_TYPE");
        featureHeaders.add("Done");
        featureHeaders.add("QA");
        featureHeaders.add("Doing");
        featureHeaders.add("To Do");

        List<String> subTaskHeaders = new LinkedList<>();
        subTaskHeaders.add("PKEY");
        subTaskHeaders.add("ISSUE_TYPE");
        subTaskHeaders.add("Done");
        subTaskHeaders.add("UAT");
        subTaskHeaders.add("Reviewing");
        subTaskHeaders.add("Doing");
        subTaskHeaders.add("To Do");

        AnalyticsTransitionsDataRow[] demands = new AnalyticsTransitionsDataRow[6];
        AnalyticsTransitionsDataRow[] features = new AnalyticsTransitionsDataRow[5];
        AnalyticsTransitionsDataRow[] subTasks = new AnalyticsTransitionsDataRow[12];
        demands[0] = new AnalyticsTransitionsDataRow("TASKB-1", "Demand"
                , parseDateList("2000-01-05", "2000-01-03", "2000-01-01"));
        demands[1] = new AnalyticsTransitionsDataRow("TASKB-2", "Demand"
                , parseDateList("2000-01-06", "2000-01-04", "2000-01-02"));
        demands[2] = new AnalyticsTransitionsDataRow("TASKB-3", "Demand"
                , parseDateList(null, "2000-01-04", "2000-01-01"));
        demands[3] = new AnalyticsTransitionsDataRow("TASKB-4", "OS"
                , parseDateList("2000-01-07", "2000-01-05", "2000-01-03"));
        demands[4] = new AnalyticsTransitionsDataRow("TASKB-5", "OS"
                , parseDateList("2000-01-08", "2000-01-06", "2000-01-04"));
        demands[5] = new AnalyticsTransitionsDataRow("TASKB-6", "OS"
                , parseDateList(null, "2000-01-06", "2000-01-03"));

        features[0] = new AnalyticsTransitionsDataRow("TASKB-7", "Feature"
                , parseDateList(null, "2000-01-04", "2000-01-03", "2000-01-02"));
        features[1] = new AnalyticsTransitionsDataRow("TASKB-8", "Feature"
                , parseDateList(null, null, "2000-01-04", "2000-01-03"));
        features[2] = new AnalyticsTransitionsDataRow("TASKB-9", "Feature"
                , parseDateList("2000-01-07", "2000-01-06", "2000-01-05", "2000-01-04"));
        features[3] = new AnalyticsTransitionsDataRow("TASKB-10", "Feature"
                , parseDateList(null, "2000-01-06", "2000-01-05", "2000-01-04"));
        features[4] = new AnalyticsTransitionsDataRow("TASKB-11", "Feature"
                , parseDateList("2000-01-08", "2000-01-07", "2000-01-06", "2000-01-05"));

        subTasks[0] = new AnalyticsTransitionsDataRow("TASKB-12", "Development"
                , parseDateList("2000-01-09", "2000-01-08", "2000-01-07", "2000-01-06", "2000-01-05"));
        subTasks[1] = new AnalyticsTransitionsDataRow("TASKB-13", "Development"
                , parseDateList("2000-01-11", "2000-01-10", "2000-01-09", "2000-01-07", "2000-01-05"));
        subTasks[2] = new AnalyticsTransitionsDataRow("TASKB-14", "Development"
                , parseDateList(null, null, "2000-01-08", "2000-01-08", "2000-01-06"));
        subTasks[3] = new AnalyticsTransitionsDataRow("TASKB-15", "Development"
                , parseDateList(null, null, null, null, "2000-01-06"));
        subTasks[4] = new AnalyticsTransitionsDataRow("TASKB-16", "Review"
                , parseDateList(null, null, null, "2000-01-09", "2000-01-07"));
        subTasks[5] = new AnalyticsTransitionsDataRow("TASKB-17", "Review"
                , parseDateList("2000-01-07", null, null, "2000-01-07", "2000-01-07"));
        subTasks[6] = new AnalyticsTransitionsDataRow("TASKB-18", "Review"
                , parseDateList("2000-01-10", null, null, "2000-01-09", "2000-01-08"));
        subTasks[7] = new AnalyticsTransitionsDataRow("TASKB-19", "Review"
                , parseDateList("2000-01-11", null, null, "2000-01-08", "2000-01-08"));
        subTasks[8] = new AnalyticsTransitionsDataRow("TASKB-20", "Sub-Task"
                , parseDateList("2000-01-08", null, "2000-01-07", "2000-01-06", "2000-01-04"));
        subTasks[9] = new AnalyticsTransitionsDataRow("TASKB-21", "Sub-Task"
                , parseDateList("2000-01-07", null, "2000-01-07", "2000-01-07", "2000-01-06"));
        subTasks[10] = new AnalyticsTransitionsDataRow("TASKB-22", "Sub-Task"
                , parseDateList(null, null, null, "2000-01-09", "2000-01-08"));
        subTasks[11] = new AnalyticsTransitionsDataRow("TASKB-23", "Sub-Task"
                , parseDateList("2000-01-12", null, "2000-01-11", "2000-01-11", "2000-01-10"));

        return asList(new AnalyticsTransitionsDataSet(TYPE_DEMAND, demandHeaders, asList(demands))
                , new AnalyticsTransitionsDataSet(TYPE_FEATURES, featureHeaders, asList(features))
                , new AnalyticsTransitionsDataSet(TYPE_SUBTASKS, subTaskHeaders, asList(subTasks)));
    }

    public static List<AnalyticsTransitionsDataSet> getAnalyticsTransitionsDataSetWitNoRow() {
        List<String> headers = new LinkedList<>();
        headers.add("PKEY");
        headers.add("ISSUE_TYPE");
        headers.add("Done");
        headers.add("Doing");
        headers.add("To Do");

        AnalyticsTransitionsDataRow rowSubtask = new AnalyticsTransitionsDataRow("I-4", "Sub-task"
                , asList(
                        null
                        , null
                        , DateTimeUtils.parseDateTime("2017-09-25")));

        return asList(new AnalyticsTransitionsDataSet(TYPE_DEMAND, headers, null),
                new AnalyticsTransitionsDataSet(TYPE_FEATURES, headers, emptyList()),
                new AnalyticsTransitionsDataSet(TYPE_SUBTASKS, headers, asList(rowSubtask)));
    }

    public static List<AnalyticsTransitionsDataSet> getEmptyAnalyticsTransitionsDataSet() {
        return singletonList(new AnalyticsTransitionsDataSet(TYPE_DEMAND, emptyList(), emptyList()));
    }

    public static List<SyntheticTransitionsDataSet> getDefaultSyntheticTransitionsDataSet() {
        List<String> headers = new LinkedList<>();
        headers.add("Date");
        headers.add("Type");
        headers.add("Done");
        headers.add("Doing");
        headers.add("To Do");

        List<SyntheticTransitionsDataRow> rowsDemand = new LinkedList<>();
        rowsDemand.add(new SyntheticTransitionsDataRow(DateTimeUtils.parseDateTime("2017-09-25"), "Demand", Ints.asList(0, 0, 1)));
        rowsDemand.add(new SyntheticTransitionsDataRow(DateTimeUtils.parseDateTime("2017-09-25"), "OS", Ints.asList(0, 0, 1)));
        rowsDemand.add(new SyntheticTransitionsDataRow(DateTimeUtils.parseDateTime("2017-09-26"), "Demand", Ints.asList(0, 1, 0)));
        rowsDemand.add(new SyntheticTransitionsDataRow(DateTimeUtils.parseDateTime("2017-09-26"), "OS", Ints.asList(0, 1, 0)));
        rowsDemand.add(new SyntheticTransitionsDataRow(DateTimeUtils.parseDateTime("2017-09-27"), "Demand", Ints.asList(1, 0, 0)));
        rowsDemand.add(new SyntheticTransitionsDataRow(DateTimeUtils.parseDateTime("2017-09-27"), "OS", Ints.asList(0, 1, 0)));

        List<SyntheticTransitionsDataRow> rowsFeature = new LinkedList<>();
        rowsFeature.add(new SyntheticTransitionsDataRow(DateTimeUtils.parseDateTime("2017-09-25"), "Feature", Ints.asList(0, 0, 1)));
        rowsFeature.add(new SyntheticTransitionsDataRow(DateTimeUtils.parseDateTime("2017-09-26"), "Feature", Ints.asList(0, 1, 0)));

        List<SyntheticTransitionsDataRow> rowsSubtask = new LinkedList<>();
        rowsSubtask.add(new SyntheticTransitionsDataRow(DateTimeUtils.parseDateTime("2017-09-25"), "Sub-task", Ints.asList(0, 0, 1)));

        return asList(new SyntheticTransitionsDataSet(TYPE_DEMAND, headers, rowsDemand),
                new SyntheticTransitionsDataSet(TYPE_FEATURES, headers, rowsFeature),
                new SyntheticTransitionsDataSet(TYPE_SUBTASKS, headers, rowsSubtask));
    }

    public static List<SyntheticTransitionsDataSet> getSyntheticTransitionsDataSetWithNoRow() {
        List<String> headers = new LinkedList<>();
        headers.add("Date");
        headers.add("Type");
        headers.add("Done");
        headers.add("Doing");
        headers.add("To Do");

        List<SyntheticTransitionsDataRow> rowsSubtask = new LinkedList<>();
        rowsSubtask.add(new SyntheticTransitionsDataRow(DateTimeUtils.parseDateTime("2017-09-25"), "Sub-task", Ints.asList(0, 0, 1)));

        return asList(new SyntheticTransitionsDataSet(TYPE_DEMAND, headers, null),
                new SyntheticTransitionsDataSet(TYPE_FEATURES, headers, emptyList()),
                new SyntheticTransitionsDataSet(TYPE_SUBTASKS, headers, rowsSubtask));
    }

    public static List<SyntheticTransitionsDataSet> getEmptySyntheticTransitionsDataSet() {
        return singletonList(new SyntheticTransitionsDataSet(TYPE_DEMAND, emptyList(), emptyList()));
    }

    private static List<ZonedDateTime> parseDateList(String... yyyymmdd) {
        return Arrays.stream(yyyymmdd)
                .map(DateTimeUtils::parseDateTime)
                .collect(Collectors.toList());
    }
}
