package objective.taskboard.sizingImport.cost;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_COST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.sizingImport.SheetColumn;
import objective.taskboard.sizingImport.SizingImportLine.ImportValue;

@Component
public class CostSheetParser {

    private final GoogleApiService googleApiService;
    private final CostColumnMappingDefinitionProvider costColumnProvider;
    private final CostColumnMappingDefinitionRowProvider costRowProvider;

    @Autowired
    public CostSheetParser(GoogleApiService googleApiService, CostColumnMappingDefinitionProvider costColumnProvider, CostColumnMappingDefinitionRowProvider costRowProvider) {
        this.googleApiService = googleApiService;
        this.costColumnProvider = costColumnProvider;
        this.costRowProvider = costRowProvider;
    }

    public List<SizingImportLineCost> parse(String spreadsheetId) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        List<List<Object>> rows = spreadsheetsManager.readRange(spreadsheetId, format("'%s'", SHEET_COST));

        if (rows == null || rows.isEmpty())
            return Collections.emptyList();

        List<SizingImportLineCost> lines = new ArrayList<>();
        int startingRowIndex = costRowProvider.getDataStartingRowIndex(spreadsheetId);
        int endingRowIndex = costRowProvider.getDataEndingRowIndex(spreadsheetId);
        List<SheetColumn> headerColumns = getHeaderColumns();

        for (int i = startingRowIndex; i <= endingRowIndex; i++) {
            List<Object> row = rows.get(i);

            if (row.isEmpty())
                continue;

            lines.add(parseRow(row, i, headerColumns));
        }

        return lines;
    }

    private List<SheetColumn> getHeaderColumns() {
        return costColumnProvider.getHeaderMappings().stream()
                .map(md -> new SheetColumn(md.getColumnDefinition(), md.getColumnLetter()))
                .collect(toList());
    }

    private SizingImportLineCost parseRow(List<Object> row, int rowIndex, List<SheetColumn> headerColumns) {
        List<ImportValue> values = headerColumns.stream()
                .map(column -> {
                    String value = getValue(row, column.getLetter());
                    if (value == null)
                        return null;

                    return new ImportValue(column, value);
                })
                .filter(Objects::nonNull)
                .collect(toList());

        return new SizingImportLineCost(rowIndex, values);
    }

    private String getValue(List<Object> row, String columnLetter) {
        int index = SpreadsheetUtils.columnLetterToIndex(columnLetter);
        if (index >= row.size())
            return null;

        String value = StringUtils.trim((String) row.get(index));

        if (StringUtils.isBlank(value))
            return null;

        return value;
    }

}
