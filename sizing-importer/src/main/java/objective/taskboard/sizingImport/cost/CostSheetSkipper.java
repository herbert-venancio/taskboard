package objective.taskboard.sizingImport.cost;

import objective.taskboard.sizingImport.SizingImportConfig;
import objective.taskboard.sizingImport.SizingVersionProvider;
import objective.taskboard.sizingImport.SizingSkipper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CostSheetSkipper implements SizingSkipper {

    private final SizingImportConfig importConfig;
    private final SizingVersionProvider versionProvider;

    @Autowired
    public CostSheetSkipper(
        final SizingImportConfig importConfig,
        final SizingVersionProvider versionProvider
    ) {
        this.importConfig = importConfig;
        this.versionProvider = versionProvider;
    }

    public boolean shouldSkip(final String spreadsheetId) {
        if (importConfig.getIndirectCosts() == null)
            return true;

        Double spreadsheetVersion = this.versionProvider.get(spreadsheetId);
        Double minimalVersion = importConfig.getMinimalVersionForCostDouble();
        return spreadsheetVersion < minimalVersion;
    }
}
