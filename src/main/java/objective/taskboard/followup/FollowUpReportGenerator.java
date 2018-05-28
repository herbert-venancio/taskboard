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

import static java.util.Optional.empty;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_DEMAND;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_FEATURES;
import static objective.taskboard.followup.FollowUpTransitionsDataProvider.TYPE_SUBTASKS;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.springframework.core.io.Resource;

import objective.taskboard.followup.FromJiraRowCalculator.FromJiraRowCalculation;
import objective.taskboard.followup.ReleaseHistoryProvider.ProjectRelease;
import objective.taskboard.followup.cluster.FollowUpClusterItem;
import objective.taskboard.followup.cluster.FollowupCluster;
import objective.taskboard.google.SpreadsheetUtils.SpreadsheetA1Range;
import objective.taskboard.project.ProjectProfileItem;
import objective.taskboard.spreadsheet.Sheet;
import objective.taskboard.spreadsheet.SheetRow;
import objective.taskboard.spreadsheet.SheetTable;
import objective.taskboard.spreadsheet.SpreadsheetEditor;
import objective.taskboard.utils.DateTimeUtils;
import objective.taskboard.utils.IOUtilities;

public class FollowUpReportGenerator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FollowUpReportGenerator.class);

    private final SpreadsheetEditor editor;

    public FollowUpReportGenerator(SpreadsheetEditor editor) {
        this.editor = editor;
    }

    public Resource generate(FollowUpSnapshot snapshot, ZoneId timezone) throws IOException {
        try {
            editor.open();

            FollowUpData followupData = snapshot.getData();

            updateTimelineDates(snapshot.getTimeline(), snapshot.getReleases());
            generateFromJiraSheet(snapshot);
            generateTransitionsSheets(followupData);
            generateEffortHistory(snapshot);
            generateTShirtSizeSheet(snapshot.getCluster());
            generateWorklogSheet(followupData, timezone);
            generateScopeBaselineSheet(snapshot);
            generateProjectProfileSheet(snapshot);

            return IOUtilities.asResource(editor.toBytes());
        } catch (Exception e) {
            log.error(e.getMessage() == null ? e.toString() : e.getMessage());
            throw e;
        } finally {
            editor.close();
        }
    }

    private void updateTimelineDates(FollowUpTimeline timeline, List<ProjectRelease> releases) {
        Sheet sheet = editor.getSheet("Timeline");
        if (sheet == null)
            return;

        sheet.getOrCreateRow(2).setValue("B", timeline.getStart().orElse(null));
        sheet.getOrCreateRow(5).setValue("B", timeline.getEnd().orElse(null));
        sheet.getOrCreateRow(6).setValue("B", timeline.getReference());
        sheet.getOrCreateRow(8).setValue("B", timeline.getRiskPercentage());
        sheet.getOrCreateRow(9).setValue("B", timeline.getBaselineDate().orElse(null));
        
        for (int i = 0; i < releases.size(); i++) {
            ProjectRelease release = releases.get(i);
            
            SheetRow row = sheet.getOrCreateRow(i + 2);
            row.setValue("L", release.getDate());
            row.setValue("N", release.getName());
        }

        sheet.save();
    }

    private void generateWorklogSheet(FollowUpData followupData, ZoneId timezone) {
        Sheet sheet = editor.getOrCreateSheet("Worklogs");
        sheet.truncate();
        SheetRow rowHeader = sheet.createRow();
        rowHeader.addColumn("AUTHOR");
        rowHeader.addColumn("ISSUE");
        rowHeader.addColumn("STARTED");
        rowHeader.addColumn("TIMESPENT");
        
        followupData.fromJiraDs.rows.stream().forEach(row -> {
            if (row.worklogs == null) return;
            String issueKey = row.subtaskNum;
            row.worklogs.forEach(worklog -> {
                SheetRow worklogRow = sheet.createRow();
                worklogRow.addColumn(worklog.author);
                worklogRow.addColumn(issueKey);
                worklogRow.addColumn(DateTimeUtils.get(worklog.started, timezone));
                worklogRow.addColumn(worklog.timeSpentSeconds/3600.0);
            });
        });
        sheet.save();
    }
    
    private void generateFromJiraSheet(FollowUpSnapshot snapshot) {
        generateFollowUpDataSheet("From Jira", snapshot.getData(), snapshot.getFromJiraRowCalculations());
    }
    
    private void generateScopeBaselineSheet(FollowUpSnapshot snapshot) {
        Optional<FollowUpData> scopeBaseline = snapshot.getScopeBaseline();
        if (!scopeBaseline.isPresent())
            return;
        
        generateFollowUpDataSheet("Scope Baseline", scopeBaseline.get(), snapshot.getScopeBaselineRowCalculations());
    }

    private void generateFollowUpDataSheet(String sheetName, FollowUpData followupData, List<FromJiraRowCalculation> rowCalculations) {
        Sheet sheet = editor.getOrCreateSheet(sheetName);
        sheet.truncate();

        SheetRow rowHeader = sheet.createRow();
        for (String header : followupData.fromJiraDs.headers)
            rowHeader.addColumn(header);

        addAnalyticsHeadersIfExist(followupData.analyticsTransitionsDsList, TYPE_DEMAND, rowHeader);
        addAnalyticsHeadersIfExist(followupData.analyticsTransitionsDsList, TYPE_FEATURES, rowHeader);
        addAnalyticsHeadersIfExist(followupData.analyticsTransitionsDsList, TYPE_SUBTASKS, rowHeader);

        for (FromJiraRowCalculation rowCalculation : rowCalculations) {
            FromJiraDataRow fromJiraDataRow = rowCalculation.getRow();
            SheetRow row = sheet.createRow();

            row.addColumn(fromJiraDataRow.project);
            row.addColumn(fromJiraDataRow.demandType);
            row.addColumn(fromJiraDataRow.demandStatus);
            row.addColumn(fromJiraDataRow.demandNum);
            row.addColumn(fromJiraDataRow.demandSummary);
            row.addColumn(fromJiraDataRow.demandDescription);
            row.addColumn(fromJiraDataRow.demandStartDateStepMillis);
            row.addColumn(fromJiraDataRow.demandAssignee);
            row.addColumn(fromJiraDataRow.demandDueDate);
            row.addColumn(fromJiraDataRow.demandCreated);
            row.addColumn(fromJiraDataRow.demandLabels);
            row.addColumn(fromJiraDataRow.demandComponents);
            row.addColumn(fromJiraDataRow.demandReporter);
            row.addColumn(fromJiraDataRow.demandCoAssignees);
            row.addColumn(fromJiraDataRow.demandClassOfService);
            row.addColumn(fromJiraDataRow.demandUpdatedDate);
            row.addColumn(fromJiraDataRow.demandCycletime);
            row.addColumn(fromJiraDataRow.demandIsBlocked);
            row.addColumn(fromJiraDataRow.demandLastBlockReason);

            row.addColumn(fromJiraDataRow.taskType);
            row.addColumn(fromJiraDataRow.taskStatus);
            row.addColumn(fromJiraDataRow.taskNum);
            row.addColumn(fromJiraDataRow.taskSummary);
            row.addColumn(fromJiraDataRow.taskDescription);
            row.addColumn(fromJiraDataRow.taskFullDescription);
            row.addColumn(fromJiraDataRow.taskAdditionalEstimatedHours);
            row.addColumn(fromJiraDataRow.taskStartDateStepMillis);
            row.addColumn(fromJiraDataRow.taskAssignee);
            row.addColumn(fromJiraDataRow.taskDueDate);
            row.addColumn(fromJiraDataRow.taskCreated);
            row.addColumn(fromJiraDataRow.taskLabels);
            row.addColumn(fromJiraDataRow.taskComponents);
            row.addColumn(fromJiraDataRow.taskReporter);
            row.addColumn(fromJiraDataRow.taskCoAssignees);
            row.addColumn(fromJiraDataRow.taskClassOfService);
            row.addColumn(fromJiraDataRow.taskUpdatedDate);
            row.addColumn(fromJiraDataRow.taskCycletime);
            row.addColumn(fromJiraDataRow.taskIsBlocked);
            row.addColumn(fromJiraDataRow.taskLastBlockReason);

            row.addColumn(fromJiraDataRow.subtaskType);
            row.addColumn(fromJiraDataRow.subtaskStatus);
            row.addColumn(fromJiraDataRow.subtaskNum);
            row.addColumn(fromJiraDataRow.subtaskSummary);
            row.addColumn(fromJiraDataRow.subtaskDescription);
            row.addColumn(fromJiraDataRow.subtaskFullDescription);
            row.addColumn(fromJiraDataRow.subtaskStartDateStepMillis);
            row.addColumn(fromJiraDataRow.subtaskAssignee);
            row.addColumn(fromJiraDataRow.subtaskDueDate);
            row.addColumn(fromJiraDataRow.subtaskCreated);
            row.addColumn(fromJiraDataRow.subtaskLabels);
            row.addColumn(fromJiraDataRow.subtaskComponents);
            row.addColumn(fromJiraDataRow.subtaskReporter);
            row.addColumn(fromJiraDataRow.subtaskCoAssignees);
            row.addColumn(fromJiraDataRow.subtaskClassOfService);
            row.addColumn(fromJiraDataRow.subtaskUpdatedDate);
            row.addColumn(fromJiraDataRow.subtaskCycletime);
            row.addColumn(fromJiraDataRow.subtaskIsBlocked);
            row.addColumn(fromJiraDataRow.subtaskLastBlockReason);

            row.addColumn(fromJiraDataRow.demandId);
            row.addColumn(fromJiraDataRow.taskId);
            row.addColumn(fromJiraDataRow.subtaskId);
            row.addColumn(fromJiraDataRow.planningType);
            row.addColumn(fromJiraDataRow.taskRelease);
            row.addColumn(fromJiraDataRow.worklog);
            row.addColumn(fromJiraDataRow.wrongWorklog);
            row.addColumn(fromJiraDataRow.demandBallpark);
            row.addColumn(fromJiraDataRow.taskBallpark);
            row.addColumn(fromJiraDataRow.tshirtSize);
            row.addColumn(fromJiraDataRow.queryType);

            row.addColumn(rowCalculation.getEffortEstimate());
            row.addColumn(rowCalculation.getCycleEstimate());
            row.addColumn(rowCalculation.getEffortOnBacklog());
            row.addColumn(rowCalculation.getCycleOnBacklog());
            row.addColumn(rowCalculation.getBallparkEffort());
            row.addColumn(rowCalculation.getPlannedEffort());
            row.addColumn(rowCalculation.getEffortDone());
            row.addColumn(rowCalculation.getCycleDone());
            row.addColumn(rowCalculation.getWorklogDone());
            row.addColumn(rowCalculation.getWorklogDoing());
            row.addColumn(rowCalculation.getCountTasks());
            row.addColumn(rowCalculation.getCountDemands());
            row.addColumn(rowCalculation.getCountSubtasks());
            row.addColumn(rowCalculation.getSubtaskEstimativeForEepCalculation());
            row.addColumn(rowCalculation.getPlannedEffortOnBug());
            row.addColumn(rowCalculation.getWorklogOnBug());

            addTransitionsDatesIfExist(followupData.analyticsTransitionsDsList, TYPE_DEMAND, fromJiraDataRow.demandNum, row);
            addTransitionsDatesIfExist(followupData.analyticsTransitionsDsList, TYPE_FEATURES, fromJiraDataRow.taskNum, row);
            addTransitionsDatesIfExist(followupData.analyticsTransitionsDsList, TYPE_SUBTASKS, fromJiraDataRow.subtaskNum, row);
        }
        sheet.save();
    }

    private void addAnalyticsHeadersIfExist(List<AnalyticsTransitionsDataSet> analyticsDataSets, String type, SheetRow rowHeader) {
        Optional<AnalyticsTransitionsDataSet> analyticDataSetOfType = getAnalyticDataSetWithRowByType(analyticsDataSets, type);

        if (!analyticDataSetOfType.isPresent())
            return;

        List<String> headers = analyticDataSetOfType.get().headers;
        for (int i = analyticDataSetOfType.get().getInitialIndexStatusHeaders(); i < headers.size(); i++)
            rowHeader.addColumn(analyticDataSetOfType.get().issueType + " - " + headers.get(i));
    }

    private void addTransitionsDatesIfExist(List<AnalyticsTransitionsDataSet> analyticsDataSets, String type, String issueKey, SheetRow row) {
        Optional<AnalyticsTransitionsDataSet> analyticDataSetOfType = getAnalyticDataSetWithRowByType(analyticsDataSets, type);

        if (!analyticDataSetOfType.isPresent())
            return;

        Optional<AnalyticsTransitionsDataRow> analyticRowOfIssue = analyticDataSetOfType.get().rows.stream()
            .filter(analyticRow -> analyticRow.issueKey.equals(issueKey))
            .findFirst();

        if (analyticRowOfIssue.isPresent()) {
            analyticRowOfIssue.get().transitionsDates.stream()
                .forEachOrdered(transitionDate -> row.addColumn(transitionDate));
        } else {
            ZonedDateTime dateNull = null;
            List<String> headers = analyticDataSetOfType.get().headers;
            for (int i = analyticDataSetOfType.get().getInitialIndexStatusHeaders(); i < headers.size(); i++)
                row.addColumn(dateNull);
        }
    }

    private Optional<AnalyticsTransitionsDataSet> getAnalyticDataSetWithRowByType(List<AnalyticsTransitionsDataSet> analyticsDataSets, String type) {
        if (isEmpty(analyticsDataSets))
            return empty();

        return analyticsDataSets.stream()
            .filter(dataSet -> type.equals(dataSet.issueType) && !isEmpty(dataSet.rows))
            .findFirst();
    }

    private List<Sheet> generateTransitionsSheets(FollowUpData followupData) {
        List<Sheet> sheets = new LinkedList<>();
        sheets.addAll(generateAnalyticTransitionsSheets(followupData.analyticsTransitionsDsList));
        sheets.addAll(generateSyntheticTransitionsSheets(followupData.syntheticsTransitionsDsList));
        return sheets;
    }

    private List<Sheet> generateAnalyticTransitionsSheets(List<AnalyticsTransitionsDataSet> analyticTransitionDataSets) {
        List<Sheet> sheets = new LinkedList<>();

        if (isEmpty(analyticTransitionDataSets))
            return sheets;

        for (AnalyticsTransitionsDataSet analyticTransitionDataSet : analyticTransitionDataSets) {
            if (isEmpty(analyticTransitionDataSet.rows))
                continue;

            Sheet sheet = createSheetWithHeader("Analytic - ", analyticTransitionDataSet);

            for (AnalyticsTransitionsDataRow analyticTransitionDataRow : analyticTransitionDataSet.rows) {
                SheetRow row = sheet.createRow();
                row.addColumn(analyticTransitionDataRow.issueKey);
                row.addColumn(analyticTransitionDataRow.issueType);
                for (ZonedDateTime transitionDate : analyticTransitionDataRow.transitionsDates)
                    row.addColumn(transitionDate);
            }

            sheet.save();
            sheets.add(sheet);
        }
        return sheets;
    }

    private List<Sheet> generateSyntheticTransitionsSheets(List<SyntheticTransitionsDataSet> syntheticTransitionDataSets) {
        List<Sheet> sheets = new LinkedList<>();

        if (isEmpty(syntheticTransitionDataSets))
            return sheets;

        for (SyntheticTransitionsDataSet syntheticTransitionDataSet : syntheticTransitionDataSets) {
            if (isEmpty(syntheticTransitionDataSet.rows))
                continue;

            Sheet sheet = createSheetWithHeader("Synthetic - ", syntheticTransitionDataSet);

            for (SyntheticTransitionsDataRow syntheticTransitionDataRow : syntheticTransitionDataSet.rows) {
                SheetRow row = sheet.createRow();
                row.addColumn(syntheticTransitionDataRow.date);
                row.addColumn(syntheticTransitionDataRow.issueType);
                for (Integer amountOfIssue : syntheticTransitionDataRow.amountOfIssueInStatus)
                    row.addColumn(amountOfIssue);
            }

            sheet.save();
            sheets.add(sheet);
        }
        return sheets;
    }

    private Sheet createSheetWithHeader(String prefixSheetName, TransitionDataSet<? extends TransitionDataRow> transitionDataSet) {
        Sheet sheet = editor.getOrCreateSheet(prefixSheetName + transitionDataSet.issueType);
        sheet.truncate();
        SheetRow rowHeader = sheet.createRow();
        for (String header : transitionDataSet.headers)
            rowHeader.addColumn(header);

        return sheet;
    }
    
    private void generateEffortHistory(FollowUpSnapshot snapshot) {
        Sheet sheet = editor.getOrCreateSheet("Effort History");
        sheet.truncate();
        
        SheetRow rowHeader = sheet.createRow();
        rowHeader.addColumn("Date");
        rowHeader.addColumn("SumEffortDone");
        rowHeader.addColumn("SumEffortBacklog");
        
        if (!snapshot.hasClusterConfiguration())
            return;

        for (EffortHistoryRow historyRow : snapshot.getEffortHistory()) {
            SheetRow row = sheet.createRow();
            row.addColumn(historyRow.date);
            row.addColumn(historyRow.sumEffortDone);
            row.addColumn(historyRow.sumEffortBacklog);
        }
        
        sheet.save();
    }

    private void generateTShirtSizeSheet(FollowupCluster cluster) {
        String sheetName = "T-shirt Size";
        String clustersTableName = "Clusters";
        
        Sheet sheet = editor.getOrCreateSheet(sheetName);
        sheet.truncate();
        
        Optional<SheetTable> clustersTable = sheet.getTable(clustersTableName);
        if (clustersTable.isPresent()) {
            SpreadsheetA1Range referenceRange = clustersTable.get().getReference();
            int clusterSize = cluster.getClusterItems().size() + 1;
            
            if (referenceRange.getEnd().getRowNumber() < clusterSize)
                throw new InvalidTableRangeException(sheetName, clustersTableName, clusterSize);
        }

        SheetRow rowHeader = sheet.createRow();
        rowHeader.addColumn("Cluster Name");
        rowHeader.addColumn("T-Shirt Size");
        rowHeader.addColumn("Type");
        rowHeader.addColumn("Effort");
        rowHeader.addColumn("Cycle");
        rowHeader.addColumn("Project");

        for (FollowUpClusterItem clusterItem : cluster.getClusterItems()) {
            SheetRow row = sheet.createRow();
            row.addColumn(clusterItem.getSubtaskTypeName());
            row.addColumn(clusterItem.getSizing());
            row.addColumn("Hours");
            row.addColumn(clusterItem.getEffort());
            row.addColumn(clusterItem.getCycle());
            row.addColumn(clusterItem.getProject().getProjectKey());
        }

        sheet.save();
    }

    private void generateProjectProfileSheet(FollowUpSnapshot snapshot) {
        Sheet sheet = editor.getOrCreateSheet("Project Profile");
        sheet.truncate();
        
        SheetRow header = sheet.createRow();
        header.addColumn("Role Name");
        header.addColumn("People Count");
        header.addColumn("Allocation Start");
        header.addColumn("Allocation End");
        
        for (ProjectProfileItem projectProfileItem : snapshot.getProjectProfile()) {
            SheetRow row = sheet.createRow();
            row.addColumn(projectProfileItem.getRoleName());
            row.addColumn(projectProfileItem.getPeopleCount());
            row.addColumn(projectProfileItem.getAllocationStart());
            row.addColumn(projectProfileItem.getAllocationEnd());
        }
        
        sheet.save();
    }

    public static class InvalidTableRangeException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        
        private final String sheetName;
        private final String tableName;
        private final int minRows;

        public InvalidTableRangeException(String sheetName, String tableName, int minRows) {
            this.sheetName = sheetName;
            this.tableName = tableName;
            this.minRows = minRows;
        }
        
        public String getSheetName() {
            return sheetName;
        }
        
        public String getTableName() {
            return tableName;
        }
        
        public int getMinRows() {
            return minRows;
        }
    }
}
