package objective.taskboard.sizingImport.cost;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;

import objective.taskboard.sizingImport.SizingImportLine;

public class SizingImportLineCost extends SizingImportLine {

    public SizingImportLineCost(int rowIndex, List<ImportValue> values) {
        super(rowIndex, values);
    }

    public boolean isNotImported() {
        return isBlank(getJiraKey());
    }

    public String getJiraKey() {
        return getValue(CostColumnMappingDefinitionProvider.INDIRECT_COSTS_KEY);
    }

    public String getIndirectCosts() {
        return getValue(CostColumnMappingDefinitionProvider.INDIRECT_COSTS);
    }

    public String getEffort() {
        return getValue(CostColumnMappingDefinitionProvider.EFFORT);
    }

}
