package objective.taskboard.sizingImport;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static objective.taskboard.google.SpreadsheetUtils.columnLetterToIndex;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_TITLE;

import java.util.List;

import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.RepeatCellRequest;
import com.google.api.services.sheets.v4.model.Request;

import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.jira.JiraProperties;
import objective.taskboard.sizingImport.SizingImporter.SizingImporterListener;

class SizingImporterSheetUpdater implements SizingImporterListener {

    private final String spreadsheetId;
    private final SpreadsheetsManager spreadsheetsManager;
    private final JiraProperties jiraProperties;
    
    private final int issueKeyIndex;
    private final int dataStartRowIndex;
    private final int sheetId;

    public SizingImporterSheetUpdater(
            String spreadsheetId,
            SpreadsheetsManager spreadsheetsManager, 
            SizingImportConfig importConfig, 
            JiraProperties jiraProperties) {
        
        this.spreadsheetId = spreadsheetId;
        this.spreadsheetsManager = spreadsheetsManager;
        this.jiraProperties = jiraProperties;
        
        this.issueKeyIndex = columnLetterToIndex(importConfig.getSheetMap().getIssueKey());
        this.dataStartRowIndex = importConfig.getDataStartingRowIndex();
        this.sheetId = spreadsheetsManager.getSheetId(spreadsheetId, SHEET_TITLE);
    }

    @Override
    public void onImportStarted(int totalLinesCount, int linesToImportCount) {
        clearAllErrors();
    }

    @Override
    public void onLineImportStarted(SizingImportLine line) {
        highlightKeyCell(line.getRowIndex());
    }
    
    @Override
    public void onLineImportFinished(SizingImportLine line) {
        String keyValue = format("=HYPERLINK(\"%s/browse/%s\",\"%s\")", jiraProperties.getUrl(), line.getJiraKey(), line.getJiraKey());
        clearHighlightKeyCellAndSetValue(line.getRowIndex(), keyValue);
        
    }

    @Override
    public void onLineError(SizingImportLine line, List<String> errorMessages) {
        String errorMessage = "- " + errorMessages.stream().collect(joining("\n- "));
        addError(line, errorMessage);
    }

    @Override
    public void onImportFinished() {
    }

    private void highlightKeyCell(int rowIndex) {
        Color backgroundColor = new Color().setRed(1f).setGreen(0.875f).setBlue(0f);

        GridRange range = new GridRange()
                .setSheetId(sheetId)
                .setStartRowIndex(rowIndex)
                .setEndRowIndex(rowIndex + 1)
                .setStartColumnIndex(issueKeyIndex)
                .setEndColumnIndex(issueKeyIndex + 1);

        CellData cell = new CellData()
                .setUserEnteredFormat(new CellFormat().setBackgroundColor(backgroundColor));
        
        RepeatCellRequest repeatCell = new RepeatCellRequest()
                .setRange(range)
                .setCell(cell)
                .setFields("userEnteredFormat(backgroundColor)");
        
        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest()
                .setRequests(asList(new Request().setRepeatCell(repeatCell)));

        spreadsheetsManager.batchUpdate(spreadsheetId, content);
    }
    
    private void clearHighlightKeyCellAndSetValue(int rowIndex, String formulaValue) {
        GridRange range = new GridRange()
                .setSheetId(sheetId)
                .setStartRowIndex(rowIndex)
                .setEndRowIndex(rowIndex + 1)
                .setStartColumnIndex(issueKeyIndex)
                .setEndColumnIndex(issueKeyIndex + 1);

        CellData cell = new CellData()
                .setUserEnteredValue(new ExtendedValue().setFormulaValue(formulaValue))
                .setUserEnteredFormat(new CellFormat().setBackgroundColor(null));
        
        RepeatCellRequest repeatCell = new RepeatCellRequest()
                .setRange(range)
                .setCell(cell)
                .setFields("userEnteredValue(formulaValue),userEnteredFormat(backgroundColor)");
        
        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest()
                .setRequests(asList(new Request().setRepeatCell(repeatCell)));

        spreadsheetsManager.batchUpdate(spreadsheetId, content);
    }

    private void addError(SizingImportLine line, String errorMessage) {
        Color backgroundColor = new Color().setRed(1f).setGreen(0f).setBlue(0f);

        GridRange range = new GridRange()
                .setSheetId(sheetId)
                .setStartRowIndex(line.getRowIndex())
                .setEndRowIndex(line.getRowIndex() + 1)
                .setStartColumnIndex(issueKeyIndex)
                .setEndColumnIndex(issueKeyIndex + 1);

        CellData cell = new CellData()
                .setNote(errorMessage)
                .setUserEnteredFormat(new CellFormat().setBackgroundColor(backgroundColor));
        
        RepeatCellRequest repeatCell = new RepeatCellRequest()
                .setRange(range)
                .setCell(cell)
                .setFields("note,userEnteredFormat(backgroundColor)");
        
        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest()
                .setRequests(asList(new Request().setRepeatCell(repeatCell)));

        spreadsheetsManager.batchUpdate(spreadsheetId, content);
    }
    
    private void clearAllErrors() {
        GridRange range = new GridRange()
                .setSheetId(sheetId)
                .setStartRowIndex(dataStartRowIndex)
                .setStartColumnIndex(issueKeyIndex)
                .setEndColumnIndex(issueKeyIndex + 1);
        
        CellData cell = new CellData()
                .setNote(null)
                .setUserEnteredFormat(null);
        
        RepeatCellRequest repeatCell = new RepeatCellRequest()
                .setRange(range)
                .setCell(cell)
                .setFields("note,userEnteredFormat(backgroundColor)");
        
        BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest()
                .setRequests(asList(new Request().setRepeatCell(repeatCell)));

        spreadsheetsManager.batchUpdate(spreadsheetId, content);
    }
}
