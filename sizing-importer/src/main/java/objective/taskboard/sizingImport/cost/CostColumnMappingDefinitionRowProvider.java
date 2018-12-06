package objective.taskboard.sizingImport.cost;

import static java.lang.String.format;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_COST;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.sizingImport.cost.ColumnMappingDefinition.ColumnMappingDefinitionRow;

@Component
public class CostColumnMappingDefinitionRowProvider {

    private final GoogleApiService googleApiService;
    private final CostColumnMappingDefinitionProvider costColumnProvider;

    @Autowired
    public CostColumnMappingDefinitionRowProvider(GoogleApiService googleApiService, CostColumnMappingDefinitionProvider costColumnProvider) {
        this.googleApiService = googleApiService;
        this.costColumnProvider = costColumnProvider;
    }

    public Integer getDataStartingRowIndex(String spreadsheetId) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        List<List<Object>> rows = spreadsheetsManager.readRange(spreadsheetId, format("'%s'", SHEET_COST));

        ColumnMappingDefinition firstHeader = costColumnProvider.getHeaderMappings().stream()
            .findFirst().orElse(null);
        if (firstHeader == null)
            return 0;

        ColumnMappingDefinitionRow headerRow = firstHeader.findRow(rows);
        return headerRow == null ? 0 : headerRow.getRowIndex() + 1;
    }

    public Integer getDataEndingRowIndex(String spreadsheetId) {
        SpreadsheetsManager spreadsheetsManager = googleApiService.buildSpreadsheetsManager();
        List<List<Object>> rows = spreadsheetsManager.readRange(spreadsheetId, format("'%s'", SHEET_COST));

        ColumnMappingDefinition firstFooter = costColumnProvider.getFooterMappings().stream()
            .findFirst().orElse(null);
        if (firstFooter == null)
            return 0;

        ColumnMappingDefinitionRow footerRow = firstFooter.findRow(rows);
        return footerRow == null ? 0 : footerRow.getRowIndex() - 1;
    }

}
