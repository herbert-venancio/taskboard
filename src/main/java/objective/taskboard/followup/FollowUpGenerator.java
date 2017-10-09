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

import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;

import org.springframework.core.io.Resource;

import objective.taskboard.spreadsheet.SimpleSpreadsheetEditor;
import objective.taskboard.spreadsheet.SimpleSpreadsheetEditor.Sheet;
import objective.taskboard.spreadsheet.SimpleSpreadsheetEditor.SheetRow;
import objective.taskboard.utils.IOUtilities;

public class FollowUpGenerator {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FollowUpGenerator.class);

    private final FollowupDataProvider provider;

	private SimpleSpreadsheetEditor editor;

    public FollowUpGenerator(FollowupDataProvider provider, SimpleSpreadsheetEditor editor) {
        this.provider = provider;
        this.editor = editor;
    }

    public Resource generate(String [] includedProjects, ZoneId timezone) {
        try {
            editor.open();

            FollowupData followupData = provider.getJiraData(includedProjects, timezone);

            generateFromJiraSheet(followupData);
            generateTransitionsSheets(followupData);

            return IOUtilities.asResource(editor.toBytes());
        } catch (Exception e) {
            log.error(e.getMessage() == null ? e.toString() : e.getMessage());
            throw e;
        } finally {
            editor.close();
        }
    }

    Sheet generateFromJiraSheet(FollowupData followupData) {
        Sheet sheet = editor.getSheet("From Jira");
        sheet.truncate(1);
        for (FromJiraDataRow fromJiraDataRow : followupData.fromJiraDs.rows) {
            SheetRow row = sheet.createRow();

            row.addColumn(fromJiraDataRow.project);
            row.addColumn(fromJiraDataRow.demandType);
            row.addColumn(fromJiraDataRow.demandStatus);
            row.addColumn(fromJiraDataRow.demandNum);
            row.addColumn(fromJiraDataRow.demandSummary);
            row.addColumn(fromJiraDataRow.demandDescription);
            row.addColumn(fromJiraDataRow.taskType);
            row.addColumn(fromJiraDataRow.taskStatus);
            row.addColumn(fromJiraDataRow.taskNum);
            row.addColumn(fromJiraDataRow.taskSummary);
            row.addColumn(fromJiraDataRow.taskDescription);
            row.addColumn(fromJiraDataRow.taskFullDescription);
            row.addColumn(fromJiraDataRow.subtaskType);
            row.addColumn(fromJiraDataRow.subtaskStatus);
            row.addColumn(fromJiraDataRow.subtaskNum);
            row.addColumn(fromJiraDataRow.subtaskSummary);
            row.addColumn(fromJiraDataRow.subtaskDescription);
            row.addColumn(fromJiraDataRow.subtaskFullDescription);
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
            row.addFormula("IF(AllIssues[[#This Row],[TASK_BALLPARK]]>0,AllIssues[[#This Row],[TASK_BALLPARK]],SUMIFS(Clusters[Effort],Clusters[Cluster Name],AllIssues[[#This Row],[SUBTASK_TYPE]],Clusters[T-Shirt Size],AllIssues[tshirt_size]))");
            row.addFormula("IF(AllIssues[[#This Row],[TASK_BALLPARK]]>0,AllIssues[[#This Row],[TASK_BALLPARK]]*1.3,SUMIFS(Clusters[Cycle],Clusters[Cluster Name],AllIssues[[#This Row],[SUBTASK_TYPE]],Clusters[T-Shirt Size],AllIssues[tshirt_size]))");
            row.addFormula("AllIssues[EffortEstimate]-AllIssues[EffortDone]");
            row.addFormula("AllIssues[CycleEstimate]-AllIssues[CycleDone]");
            row.addFormula("IF(AllIssues[[#This Row],[planning_type]]=\"Ballpark\",AllIssues[EffortEstimate],0)");
            row.addFormula("IF(AllIssues[[#This Row],[planning_type]]=\"Plan\",AllIssues[EffortEstimate],0)");
            row.addFormula("IF(OR(AllIssues[SUBTASK_STATUS]=\"Done\",AllIssues[SUBTASK_STATUS]=\"Cancelled\"),AllIssues[EffortEstimate],0)");
            row.addFormula("IF(OR(AllIssues[SUBTASK_STATUS]=\"Done\",AllIssues[SUBTASK_STATUS]=\"Cancelled\"),AllIssues[CycleEstimate],0)");
            row.addFormula("IF(OR(AllIssues[SUBTASK_STATUS]=\"Done\",AllIssues[SUBTASK_STATUS]=\"Cancelled\"),AllIssues[worklog],0)");
            row.addFormula("IF(OR(AllIssues[SUBTASK_STATUS]=\"Done\",AllIssues[SUBTASK_STATUS]=\"Cancelled\"),0, AllIssues[worklog])");
            row.addFormula("IF(COUNTIFS(AllIssues[TASK_ID],AllIssues[TASK_ID],AllIssues[TASK_ID],\">0\")=0,0,1/COUNTIFS(AllIssues[TASK_ID],AllIssues[TASK_ID],AllIssues[TASK_ID],\">0\"))");
            row.addFormula("IF(COUNTIFS(AllIssues[demand_description],AllIssues[demand_description])=0,0,1/COUNTIFS(AllIssues[demand_description],AllIssues[demand_description]))");
            row.addFormula("IF(AllIssues[planning_type]=\"Plan\",1,0)");
            row.addFormula("IF(AllIssues[[#This Row],[SUBTASK_STATUS]]=\"Done\", AllIssues[[#This Row],[EffortDone]],0)");
            row.addFormula("IF(AllIssues[TASK_TYPE]=\"Bug\",AllIssues[EffortEstimate], 0)");
            row.addFormula("IF(AllIssues[TASK_TYPE]=\"Bug\",AllIssues[worklog],0)");

            row.save();
        }
        sheet.save();

        return sheet;
    }

    List<Sheet> generateTransitionsSheets(FollowupData followupData) {
        List<Sheet> sheets = new LinkedList<>();
        sheets.addAll(generateTransitionsSheets("Analytic - ", followupData.analyticsTransitionsDsList));
        sheets.addAll(generateTransitionsSheets("Synthetic - ", followupData.syntheticsTransitionsDsList));
        return sheets;
    }

    private List<Sheet> generateTransitionsSheets(String prefixSheetName, List<? extends TransitionDataSet<? extends TransitionDataRow>> transitionDataSets) {
        List<Sheet> sheets = new LinkedList<>();
        for (TransitionDataSet<? extends TransitionDataRow> transitionDataSet : transitionDataSets) {

            if (transitionDataSet.rows.isEmpty())
                continue;

            Sheet sheet = editor.createSheet(prefixSheetName + transitionDataSet.issueType);
            SheetRow rowHeader = sheet.createRow();
            for (String header : transitionDataSet.headers)
                rowHeader.addColumn(header);
            rowHeader.save();

            for (TransitionDataRow transitionDataRow : transitionDataSet.rows) {
                SheetRow row = sheet.createRow();
                for (Object columnValue : transitionDataRow.getAsObjectList())
                    row.addColumn(columnValue);
                row.save();
            }

            sheet.save();
            sheets.add(sheet);
        }
        return sheets;
    }

	public SimpleSpreadsheetEditor getEditor() {
		return editor;
	}
}
