package objective.taskboard.spreadsheet;

import static objective.taskboard.utils.IOUtilities.resourceToString;
import static objective.taskboard.utils.XmlUtils.normalizeXml;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import objective.taskboard.followup.FollowUpHelper;
import objective.taskboard.followup.FollowUpTemplate;
import objective.taskboard.followup.FromJiraDataRow;
import objective.taskboard.spreadsheet.SimpleSpreadsheetEditor.Sheet;
import objective.taskboard.spreadsheet.SimpleSpreadsheetEditor.SheetRow;
import objective.taskboard.utils.IOUtilities;

public class SimpleSpreadsheetEditorTest { 
    private static final String PATH_FOLLOWUP_TEMPLATE = "followup/Followup-template.xlsm";

    private static final String MSG_ASSERT_SHARED_STRINGS_SIZE = "Shared strings size";
    

    @Test
    public void getSharedStringsInitialTest() throws ParserConfigurationException, SAXException, IOException {
        try (SimpleSpreadsheetEditor subject = new SimpleSpreadsheetEditor(getBasicTemplate())) {
            subject.open();
            
            Map<String, Long> sharedStrings = subject.getSharedStrings();
            assertEquals(MSG_ASSERT_SHARED_STRINGS_SIZE, 204, sharedStrings.size());
            assertEquals("First shared string", 0, sharedStrings.get("project").longValue());
            assertEquals("Some special character shared string", 44, sharedStrings.get("Demand Status > Demand > Task Status > Task > Subtask").longValue());
            assertEquals("Any shared string", 126, sharedStrings.get("Group %").longValue());
            assertEquals("Last shared string", 203, sharedStrings.get("BALLPARK").longValue());
        }
    }

    @Test
    public void addNewSharedStringsInTheEndTest() throws ParserConfigurationException, SAXException, IOException {
        FollowUpTemplate template = getBasicTemplate();
        try (SimpleSpreadsheetEditor subject = new SimpleSpreadsheetEditor(template)) {
            subject.open();
            
            Map<String, Long> sharedStrings = subject.getSharedStrings();
            assertEquals(MSG_ASSERT_SHARED_STRINGS_SIZE, 204, sharedStrings.size());
            assertEquals("First shared string", 0, sharedStrings.get("project").longValue());
            assertEquals("Last shared string", 203, sharedStrings.get("BALLPARK").longValue());
    
            Sheet sheet = subject.getSheet("From Jira");
            for (FromJiraDataRow followUpData : FollowUpHelper.getFollowUpDataDefaultList()) 
                addRow(sheet, followUpData);
            sheet.save();
    
            assertEquals(MSG_ASSERT_SHARED_STRINGS_SIZE, 218, sharedStrings.size());
            assertEquals("First new shared string", 204, sharedStrings.get("PROJECT TEST").longValue());
            assertEquals("Any new shared string", 210, sharedStrings.get("Summary Feature").longValue());
            assertEquals("Last new shared string", 217, sharedStrings.get("Full Description Sub-task").longValue());
        }
    }
    
    @Test
    public void addOneRowToSpreadSheet() {
        FollowUpTemplate template = getBasicTemplate();
        try (SimpleSpreadsheetEditor subject = new SimpleSpreadsheetEditor(template)) {
            subject.open();
            
            Sheet sheet = subject.getSheet("From Jira");
            sheet.truncate(1);
            addRow(sheet, FollowUpHelper.getDefaultFromJiraDataRow());
            sheet.save();
    
            String jiraDataSheet = subject.getSheet("From Jira").stringValue();
            String jiraDataSheetExpected = normalizeXml(resourceToString("followup/fromJiraWithOneRow.xml"));
            assertEquals("Jira data sheet", jiraDataSheetExpected, jiraDataSheet);        
        }
    }
    
    @Test
    public void generateSharedStringsInOrderTest() throws ParserConfigurationException, SAXException, IOException {
        try (SimpleSpreadsheetEditor subject = new SimpleSpreadsheetEditor(getBasicTemplate())) {
            subject.open();
            
            String sharedStringsGenerated = subject.generateSharedStrings();
    
            String sharedStringsExpected = resourceToString("followup/generateSharedStringsInOrderTest.xml");
            assertEquals("Shared strings", sharedStringsExpected, sharedStringsGenerated);
        }
    }

    @Test
    public void generateSharedStringsInOrderAfterAddNewSharedStringTest() throws ParserConfigurationException, SAXException, IOException {
        FollowUpTemplate template = getBasicTemplate();
        try (SimpleSpreadsheetEditor subject = new SimpleSpreadsheetEditor(template)) {
            subject.open();
            
            Map<String, Long> sharedStrings = subject.getSharedStrings();
    
            assertEquals(MSG_ASSERT_SHARED_STRINGS_SIZE, 204, sharedStrings.size());
            
            Sheet sheet = subject.getSheet("From Jira");
            for (FromJiraDataRow followUpData : FollowUpHelper.getFollowUpDataDefaultList()) 
                addRow(sheet, followUpData);
            
            sheet.save();
            
            assertEquals(MSG_ASSERT_SHARED_STRINGS_SIZE, 218, sharedStrings.size());
    
            String sharedStringsGenerated = subject.generateSharedStrings();
    
            String sharedStringsExpected = resourceToString("followup/generateSharedStringsInOrderAfterAddNewSharedStringTest.xml");
            assertEquals("Shared strings", sharedStringsExpected, sharedStringsGenerated);
        }
    }
    
    @Test
    public void truncateFromJira() throws ParserConfigurationException, SAXException, IOException {
        FollowUpTemplate template = getBasicTemplate();
        try (SimpleSpreadsheetEditor subject = new SimpleSpreadsheetEditor(template)) {
            subject.open();
            
            Sheet sheet = subject.getSheet("From Jira");
            sheet.truncate(1);
            String jiraDataSheet = sheet.stringValue();
    
            String jiraDataSheetExpected = normalizeXml(resourceToString("followup/emptyFromJiraOnlyWithHeaders.xml"));
            
            assertEquals("Jira data sheet", jiraDataSheetExpected, jiraDataSheet);
        }
    }
    
    @Test
    public void getSheetByName_shouldFindTheSheetPart() {
        try(SimpleSpreadsheetEditor subject = new SimpleSpreadsheetEditor(getBasicTemplate())) {
            subject.open();
            Sheet sheet = subject.getSheet("T-shirt Size");
            String path = sheet.getSheetPath();
            
            assertEquals("xl/worksheets/sheet6.xml", path);
        }
    }
    
    private FollowUpTemplate getBasicTemplate() {
        return new FollowUpTemplate(resolve(PATH_FOLLOWUP_TEMPLATE));
    }

    private static Resource resolve(String resourceName) {
        return IOUtilities.asResource(SimpleSpreadsheetEditorTest.class.getClassLoader().getResource(resourceName));
    }

    private void addRow(Sheet sheet, FromJiraDataRow followUpData) {
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
        row.addFormula("SUMIFS(Clusters[Effort],Clusters[Cluster Name],AllIssues[[#This Row],[SUBTASK_TYPE]],Clusters[T-Shirt Size],AllIssues[tshirt_size])");
        row.addFormula("SUMIFS(Clusters[Cycle],Clusters[Cluster Name],AllIssues[[#This Row],[SUBTASK_TYPE]],Clusters[T-Shirt Size],AllIssues[tshirt_size])");
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
}