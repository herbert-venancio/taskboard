package objective.taskboard.sizingImport;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.sizingImport.SizingImportLine.ImportValue;

@Component
class SizingSheetParser {

    private final SizingImportConfig properties;
    
    @Autowired
    public SizingSheetParser(SizingImportConfig properties) {
        this.properties = properties;
    }

    public List<SizingImportLine> parse(List<List<Object>> rows, SheetDefinition sheetDefinition, List<SheetColumnMapping> columnMappings) {
        if (rows == null || rows.isEmpty())
            return Collections.emptyList();

        List<SizingImportLine> lines = new ArrayList<>();
        int startingRowIndex = properties.getDataStartingRowIndex();
        List<SheetColumn> columns = getSheetColumns(sheetDefinition, columnMappings);
        
        for (int i = startingRowIndex; i < rows.size(); i++) {
            List<Object> row = rows.get(i);
            
            if (row.isEmpty())
                continue;

            lines.add(parseRow(row, i, columns));
        }

        List<SizingImportLine> includedLines = lines.stream()
                .filter(SizingImportLine::isInclude)
                .collect(toList());

        return includedLines;
    }

    private List<SheetColumn> getSheetColumns(SheetDefinition sheetDefinition, List<SheetColumnMapping> columnMappings) {
        
        List<SheetColumn> columns = new ArrayList<>();
        Map<String, SheetColumnDefinition> dynamicDefinitionsByColumnId = sheetDefinition.getDynamicColumns().stream()
                .collect(toMap(DynamicMappingDefinition::getColumnId, DynamicMappingDefinition::getColumnDefinition));

        columns.addAll(sheetDefinition.getStaticColumns().stream()
                .map(md -> new SheetColumn(md.getColumnDefinition(), md.getColumnLetter()))
                .collect(toList()));
        
        columns.addAll(columnMappings.stream()
                .map(m -> new SheetColumn(dynamicDefinitionsByColumnId.get(m.getColumnId()), m.getColumnLetter()))
                .collect(toList()));

        return columns;
    }

    private SizingImportLine parseRow(List<Object> row, int rowIndex, List<SheetColumn> columns) {
        List<ImportValue> values = columns.stream()
                .map(column -> {
                    String value = getValue(row, column.getLetter());
                    if (value == null)
                        return null;
                    
                    return new ImportValue(column, value);
                })
                .filter(Objects::nonNull)
                .collect(toList());
        
        
        return new SizingImportLine(rowIndex, values);
    }

    private String getValue(List<Object> row, int index) {
        if (index >= row.size())
            return null;
        
        String value = StringUtils.trim((String) row.get(index));

        if (StringUtils.isBlank(value) || properties.getValueToIgnore().equals(value)) {
            return null;
        }

        return value;
    }
    
    private String getValue(List<Object> row, String columnLetter) {
        int index = SpreadsheetUtils.columnLetterToIndex(columnLetter);
        return getValue(row, index);
    }

}