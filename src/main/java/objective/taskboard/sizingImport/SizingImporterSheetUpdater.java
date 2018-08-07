package objective.taskboard.sizingImport;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static objective.taskboard.google.SpreadsheetUtils.columnLetterToIndex;

import java.util.List;
import java.util.Optional;

import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.CellData;
import com.google.api.services.sheets.v4.model.CellFormat;
import com.google.api.services.sheets.v4.model.Color;
import com.google.api.services.sheets.v4.model.ExtendedValue;
import com.google.api.services.sheets.v4.model.GridRange;
import com.google.api.services.sheets.v4.model.RepeatCellRequest;
import com.google.api.services.sheets.v4.model.Request;

import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.sizingImport.SizingImporterNotifier.SizingImporterListener;

public class SizingImporterSheetUpdater implements SizingImporterListener {

    private final String spreadsheetId;
    private final SpreadsheetsManager spreadsheetsManager;
    private final int sheetId;
    private final String jiraUrl;
    
    private final int issueKeyIndex;
    private final Integer dataStartingRowIndex;
    private final Optional<Integer> dataEndingRowIndex;

    private SizingImporterSheetUpdater(
            String spreadsheetId,
            String sheetTitle,
            SpreadsheetsManager spreadsheetsManager,
            String jiraUrl,
            String issueKeyColumn,
            Integer dataStartingRowIndex,
            Optional<Integer> dataEndingRowIndex) {
        
        this.spreadsheetId = spreadsheetId;
        this.spreadsheetsManager = spreadsheetsManager;
        this.sheetId = spreadsheetsManager.getSheetId(spreadsheetId, sheetTitle);
        this.jiraUrl = jiraUrl;
        
        this.issueKeyIndex = columnLetterToIndex(issueKeyColumn);
        this.dataStartingRowIndex = dataStartingRowIndex;
        this.dataEndingRowIndex = dataEndingRowIndex;
    }

    public SizingImporterSheetUpdater(
            String spreadsheetId,
            String sheetTitle,
            SpreadsheetsManager spreadsheetsManager,
            String jiraUrl,
            String issueKeyColumn,
            Integer dataStartingRowIndex,
            Integer dataEndingRowIndex) {

        this(spreadsheetId, sheetTitle, spreadsheetsManager, jiraUrl, issueKeyColumn, dataStartingRowIndex, Optional.of(dataEndingRowIndex));
    }

    public SizingImporterSheetUpdater(
            String spreadsheetId,
            String sheetTitle,
            SpreadsheetsManager spreadsheetsManager,
            String jiraUrl,
            String issueKeyColumn,
            Integer dataStartingRowIndex) {

        this(spreadsheetId, sheetTitle, spreadsheetsManager, jiraUrl, issueKeyColumn, dataStartingRowIndex, Optional.empty());
    }

    @Override
    public void onSheetImportStarted(int totalLinesCount, int linesToImportCount) {
        clearAllErrors();
    }

    @Override
    public void onLineImportStarted(SizingImportLine line) {
    }
    
    @Override
    public void onLineImportFinished(SizingImportLine line, String issueKey) {
        String keyValue = format("=HYPERLINK(\"%s/browse/%s\",\"%s\")", jiraUrl, issueKey, issueKey);
        setValue(line.getRowIndex(), keyValue);
    }

    @Override
    public void onLineError(SizingImportLine line, List<String> errorMessages) {
        addError(line, "[Import Error]\n\n" + errorMessages.stream().collect(joining(";\n- ", "- ", ".")));
    }

    @Override
    public void onSheetImportFinished() {
    }

    private void setValue(int rowIndex, String formulaValue) {
        GridRange range = new GridRange()
                .setSheetId(sheetId)
                .setStartRowIndex(rowIndex)
                .setEndRowIndex(rowIndex + 1)
                .setStartColumnIndex(issueKeyIndex)
                .setEndColumnIndex(issueKeyIndex + 1);

        CellData cell = new CellData()
                .setUserEnteredValue(new ExtendedValue().setFormulaValue(formulaValue));
        
        RepeatCellRequest repeatCell = new RepeatCellRequest()
                .setRange(range)
                .setCell(cell)
                .setFields("userEnteredValue(formulaValue)");
        
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
                .setStartRowIndex(dataStartingRowIndex)
                .setEndRowIndex(dataEndingRowIndex.map(i -> i + 1).orElse(null))
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
