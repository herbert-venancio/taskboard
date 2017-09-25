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

    public Resource generate(String [] includedProjects) {
        try {
            editor.open();
            editor.resetCalcChain();
            FollowupData jiraData = provider.getJiraData(includedProjects);

            generateJiraDataSheet(editor, jiraData);

            return IOUtilities.asResource(editor.toBytes());
        } catch (Exception e) {
            log.error(e.getMessage() == null ? e.toString() : e.getMessage());
            throw e;
        } finally {
            editor.close();
        }
    }

    SimpleSpreadsheetEditor.Sheet generateJiraDataSheet(SimpleSpreadsheetEditor editor, FollowupData jiraData) {
        Sheet sheet = editor.getSheet("From Jira");
        sheet.truncate(1);
        for (FromJiraDataRow followUpData : jiraData.fromJiraDs.rows) {
        	SheetRow row = sheet.createRow();

        	row.addColumn(followUpData.project);
        	row.addColumn(followUpData.demandType);
        	row.addColumn(followUpData.demandStatus);
        	row.addColumn(followUpData.demandNum);
        	row.addColumn(followUpData.demandSummary);
        	row.addColumn(followUpData.demandDescription);
        	row.addColumn(followUpData.taskType);
        	row.addColumn(followUpData.taskStatus);
        	row.addColumn(followUpData.taskNum);
        	row.addColumn(followUpData.taskSummary);
        	row.addColumn(followUpData.taskDescription);
        	row.addColumn(followUpData.taskFullDescription);
        	row.addColumn(followUpData.subtaskType);
        	row.addColumn(followUpData.subtaskStatus);
        	row.addColumn(followUpData.subtaskNum);
        	row.addColumn(followUpData.subtaskSummary);
        	row.addColumn(followUpData.subtaskDescription);
        	row.addColumn(followUpData.subtaskFullDescription);
        	row.addColumn(followUpData.demandId);
        	row.addColumn(followUpData.taskId);
        	row.addColumn(followUpData.subtaskId);
        	row.addColumn(followUpData.planningType);
        	row.addColumn(followUpData.taskRelease);
        	row.addColumn(followUpData.worklog);
        	row.addColumn(followUpData.wrongWorklog);
        	row.addColumn(followUpData.demandBallpark);
        	row.addColumn(followUpData.taskBallpark);
        	row.addColumn(followUpData.tshirtSize);
        	row.addColumn(followUpData.queryType);
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

	public SimpleSpreadsheetEditor getEditor() {
		return editor;
	}
}
