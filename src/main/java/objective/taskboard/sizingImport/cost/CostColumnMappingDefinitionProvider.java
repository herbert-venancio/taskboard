package objective.taskboard.sizingImport.cost;

import static java.util.Collections.sort;
import static java.util.Comparator.comparing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.google.SpreadsheetUtils;
import objective.taskboard.sizingImport.SheetColumnDefinition;
import objective.taskboard.sizingImport.SheetColumnDefinition.PreviewBehavior;
import objective.taskboard.sizingImport.SizingImportConfig;

@Component
class CostColumnMappingDefinitionProvider {

    public static final SheetColumnDefinition INDIRECT_COSTS       = new SheetColumnDefinition("Indirect Costs");
    public static final SheetColumnDefinition INDIRECT_COSTS_KEY   = new SheetColumnDefinition("Key", PreviewBehavior.HIDE);
    public static final SheetColumnDefinition EFFORT               = new SheetColumnDefinition("Effort");
    public static final SheetColumnDefinition TOTAL_INDIRECT_COSTS = new SheetColumnDefinition("Total Indirect Costs", PreviewBehavior.HIDE);

    private final SizingImportConfig importConfig;
    private final List<ColumnMappingDefinition> headerMappings = new ArrayList<>();
    private final List<ColumnMappingDefinition> footerMappings = new ArrayList<>();

    @Autowired
    public CostColumnMappingDefinitionProvider(SizingImportConfig importConfig) {
        this.importConfig = importConfig;
    }

    public List<ColumnMappingDefinition> getHeaderMappings() {
        if (!headerMappings.isEmpty())
            return headerMappings;

        headerMappings.addAll(getIndirectCostsHeaderMappings());
        sort(headerMappings, comparing(c -> c.getColumnLetter(), SpreadsheetUtils.COLUMN_LETTER_COMPARATOR));

        return headerMappings;
    }

    public List<ColumnMappingDefinition> getFooterMappings() {
        if (!footerMappings.isEmpty())
            return footerMappings;

        footerMappings.addAll(getIndirectCostsFooterMappings());
        return footerMappings;
    }

    private List<ColumnMappingDefinition> getIndirectCostsHeaderMappings() {
        return Arrays.asList(
                new ColumnMappingDefinition(INDIRECT_COSTS,     importConfig.getIndirectCosts().getIndirectCostsColumn()),
                new ColumnMappingDefinition(INDIRECT_COSTS_KEY, importConfig.getIndirectCosts().getIssueKeyColumn()),
                new ColumnMappingDefinition(EFFORT,             importConfig.getIndirectCosts().getEffortColumn()));
    }

    private List<ColumnMappingDefinition> getIndirectCostsFooterMappings() {
        return Arrays.asList(
                new ColumnMappingDefinition(TOTAL_INDIRECT_COSTS, importConfig.getIndirectCosts().getTotalIndirectCostsColumn()));
    }

}
