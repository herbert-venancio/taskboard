package objective.taskboard.sizingImport.cost;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import objective.taskboard.sizingImport.SizingVersionProvider;
import org.junit.Test;

import objective.taskboard.sizingImport.SizingImportConfig;
import objective.taskboard.sizingImport.SizingImportConfig.IndirectCosts;

public class CostSheetSkipperTest {

    private static final String SPREADSHEET_ID = "100";

    private final SizingImportConfig importConfig = new SizingImportConfig();
    private final SizingVersionProvider versionProvider = mock(SizingVersionProvider.class);
    private CostSheetSkipper subject = new CostSheetSkipper(importConfig, versionProvider);

    @Test
    public void shouldReturnFalseWhenIndirectCostsPropertiesAreConfiguredAndSpreadsheetVersionIsValid() {
        when(versionProvider.get(SPREADSHEET_ID)).thenReturn(4.1);
        importConfig.setIndirectCosts(new IndirectCosts());

        assertFalse(subject.shouldSkip(SPREADSHEET_ID));
    }

    @Test
    public void shouldReturnTrueWhenIndirectCostsPropertiesAreNotConfigured() {
        when(versionProvider.get(SPREADSHEET_ID)).thenReturn(4.0);
        importConfig.setIndirectCosts(null);

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));
    }

}
