package objective.taskboard.sizingImport;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.List;

class SizingImportLineScope extends SizingImportLine {

    public SizingImportLineScope(int rowIndex, List<ImportValue> values) {
        super(rowIndex, values);
    }

    public boolean isImported() {
        return isNotBlank(getJiraKey());
    }

    public boolean isNotImported() {
        return !isImported();
    }

    public String getJiraKey() {
        return getValue(SheetColumnDefinitionProviderScope.KEY);
    }

    public boolean isInclude() {
        return "true".equalsIgnoreCase(getValue(SheetColumnDefinitionProviderScope.INCLUDE));
    }

    public String getPhase() {
        return getValue(SheetColumnDefinitionProviderScope.PHASE);
    }

    public String getDemand() {
        return getValue(SheetColumnDefinitionProviderScope.DEMAND);
    }

    public String getFeature() {
        return getValue(SheetColumnDefinitionProviderScope.FEATURE);
    }

    public String getType() {
        return getValue(SheetColumnDefinitionProviderScope.TYPE);
    }

}
