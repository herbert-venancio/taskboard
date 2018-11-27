package objective.taskboard.sizingImport;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.google.SpreadsheetsManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static java.lang.String.format;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SIZING_METADATA;

@Component
public class SizingVersionProvider {

    private final SizingImportConfig importConfig;
    private final GoogleApiService googleApiService;

    @Autowired
    public SizingVersionProvider(
        final SizingImportConfig importConfig,
        final GoogleApiService googleApiService
    ) {
        this.importConfig = importConfig;
        this.googleApiService = googleApiService;
    }

    public Double get(final String spreadsheetId) {
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
