package objective.taskboard.sizingImport.cost;

import static java.lang.String.format;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SIZING_METADATA;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.sizingImport.SizingImportConfig;

@Component
public class CostSheetSkipper {

    private final SizingImportConfig importConfig;
    private final GoogleApiService googleApiService;

    @Autowired
    public CostSheetSkipper(SizingImportConfig importConfig, GoogleApiService googleApiService) {
        this.importConfig = importConfig;
        this.googleApiService = googleApiService;
    }

    public boolean shouldSkip(String spreadsheetId) {
        if (importConfig.getIndirectCosts() == null)
            return true;

        Double spreadsheetVersion = getSpreadsheetVersion(spreadsheetId);
        Double minimalVersion = importConfig.getMinimalVersionForCostDouble();
        if (spreadsheetVersion < minimalVersion)
            return true;

        return false;
    }

    private Double getSpreadsheetVersion(String spreadsheetId) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        List<String> sheetsTitles = spreadsheetsManager.getSheetsTitles(spreadsheetId);
        if (!sheetsTitles.contains(SHEET_SIZING_METADATA))
            return 0D;

        List<List<Object>> rows = spreadsheetsManager.readRange(spreadsheetId, format("'%s'", SHEET_SIZING_METADATA));
        Integer versionRowIndex = importConfig.getVersionRowIndex();
        if (rows.size() <= versionRowIndex)
            return 0D;

        List<Object> versionRow = rows.get(versionRowIndex);

        int versionColumnIndex = SpreadsheetUtils.columnLetterToIndex(importConfig.getVersionColumnLetter());
        if (versionRow.size() <= versionColumnIndex)
            return 0D;

        String version = String.valueOf(versionRow.get(versionColumnIndex));
        try {
            return Double.parseDouble(version);
        } catch (NumberFormatException e) {
            return 0D;
        }
    }

}
