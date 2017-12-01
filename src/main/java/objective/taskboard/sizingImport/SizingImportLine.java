package objective.taskboard.sizingImport;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

class SizingImportLine {

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
    
    public Optional<String> getValue(Predicate<SheetColumn> columnPredicate) {
        return values.stream()
                .filter(v -> columnPredicate.test(v.column))
                .findFirst()
                .map(ImportValue::getValue);
    }
    
    public String getValue(SheetColumnDefinition columnDefinition) {
        return getValue(columnDefinition, null);
    }

    public String getJiraKey() {
        return getValue(SheetColumnDefinitionProvider.KEY);
    }

    public boolean isImported() {
        return isNotBlank(getJiraKey());
    }
    
    public boolean isNotImported() {
        return !isImported();
    }
    
    public String getPhase() {
        return getValue(SheetColumnDefinitionProvider.PHASE);
    }

    public String getDemand() {
        return getValue(SheetColumnDefinitionProvider.DEMAND);
    }
    
    public String getFeature() {
        return getValue(SheetColumnDefinitionProvider.FEATURE);
    }

    public boolean isInclude() {
        return "true".equalsIgnoreCase(getValue(SheetColumnDefinitionProvider.INCLUDE));
    }

    static class ImportValue {
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
