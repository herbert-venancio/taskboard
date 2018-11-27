package objective.taskboard.sizingImport.cost;

import objective.taskboard.sizingImport.SizingImportConfig;
import objective.taskboard.sizingImport.SizingVersionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CostSheetSkipper {

    private final SizingImportConfig importConfig;
    private final SizingVersionProvider versionProvider;

    @Autowired
    public CostSheetSkipper(final SizingImportConfig importConfig, final SizingVersionProvider versionProvider) {
        this.importConfig = importConfig;
        this.versionProvider = versionProvider;
    }

    public boolean shouldSkip(String spreadsheetId) {
        if (importConfig.getIndirectCosts() == null)
            return true;

        Double spreadsheetVersion = this.versionProvider.get(spreadsheetId);
        Double minimalVersion = importConfig.getMinimalVersionForCostDouble();
        if (spreadsheetVersion < minimalVersion)
            return true;

        return false;
    }
}
