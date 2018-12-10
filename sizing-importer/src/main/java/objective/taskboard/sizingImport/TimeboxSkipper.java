package objective.taskboard.sizingImport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TimeboxSkipper implements SizingSkipper {

    private final SizingImportConfig importConfig;
    private final SizingVersionProvider versionProvider;

    @Autowired
    public TimeboxSkipper(
        final SizingImportConfig importConfig,
        final SizingVersionProvider versionProvider
    ) {
        this.importConfig = importConfig;
        this.versionProvider = versionProvider;
    }

    @Override
    public boolean shouldSkip(final String spreadsheetId) {
        Double version = this.versionProvider.get(spreadsheetId);
        Double minimalVersion = Double.valueOf(this.importConfig.getMinimalVersionForTimebox());
        return version < minimalVersion;
    }
}
