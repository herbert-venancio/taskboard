package objective.taskboard.sizingImport.cost;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.sizingImport.SheetColumnDefinition;

class ColumnMappingDefinition {
    private final SheetColumnDefinition columnDefinition;
    private final String columnLetter;

    public ColumnMappingDefinition(SheetColumnDefinition columnDefinition, String columnLetter) {
        this.columnDefinition = columnDefinition;
        this.columnLetter = columnLetter;
    }

    public SheetColumnDefinition getColumnDefinition() {
        return columnDefinition;
    }

    public String getColumnLetter() {
        return columnLetter;
    }

    public ColumnMappingDefinitionRow findRow(List<List<Object>> rows) {
        if (rows == null || rows.isEmpty())
            return null;

        for (int i = 0; i < rows.size(); i++) {
            List<Object> row = rows.get(i);
            if (row == null || row.isEmpty())
                continue;

            List<String> rowStrings = row.stream()
                    .map(c -> Objects.toString(c))
                    .map(StringUtils::trim)
                    .collect(toList());

            int columnIndex = SpreadsheetUtils.columnLetterToIndex(getColumnLetter());
            if (columnIndex >= rowStrings.size())
                continue;

            if (getColumnDefinition().getName().equalsIgnoreCase(rowStrings.get(columnIndex)))
                return new ColumnMappingDefinitionRow(i, rowStrings);
        }
        return null;
    }

    static class ColumnMappingDefinitionRow {
        private final Integer rowIndex;
        private final List<String> row;

        public ColumnMappingDefinitionRow(Integer rowIndex, List<String> row) {
            this.rowIndex = rowIndex;
            this.row = row;
        }

        public Integer getRowIndex() {
            return rowIndex;
        }

        public List<String> getRow() {
            return row;
        }
    }

}