package objective.taskboard.sizingImport;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public abstract class SizingImportLine {

    private final int rowIndex;
    private final List<ImportValue> values;
    private final Map<SheetColumnDefinition, ImportValue> valuesByColumnDefinition;

    public SizingImportLine(int rowIndex, List<ImportValue> values) {
        this.rowIndex = rowIndex;
        this.values = values;
        this.valuesByColumnDefinition = values.stream().collect(toMap(v -> v.getColumnDefinition(), identity()));
    }

    public int getRowIndex() {
        return rowIndex;
    }

    public int getRowNumber() {
        return rowIndex + 1;
    }

    public List<ImportValue> getImportValues() {
        return values;
    }

    public String getValue(SheetColumnDefinition columnDefinition, String defaultValue) {
        ImportValue importValue = valuesByColumnDefinition.get(columnDefinition);
        return importValue == null ? defaultValue : importValue.getValue();
    }

    public String getValue(SheetColumnDefinition columnDefinition) {
        return getValue(columnDefinition, null);
    }

    public Optional<String> getValue(Predicate<SheetColumn> columnPredicate) {
        return values.stream()
                .filter(v -> columnPredicate.test(v.column))
                .findFirst()
                .map(ImportValue::getValue);
    }

    public static class ImportValue {
        private final SheetColumn column;
        private final String value;

        public ImportValue(SheetColumn column, String value) {
            this.column = column;
            this.value = value;
        }

        public SheetColumn getColumn() {
            return column;
        }
        
        public SheetColumnDefinition getColumnDefinition() {
            return column.getDefinition();
        }

        public String getValue() {
            return value;
        }
    }

}
