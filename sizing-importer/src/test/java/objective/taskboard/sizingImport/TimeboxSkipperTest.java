package objective.taskboard.sizingImport;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimeboxSkipperTest {

    private static final String SPREADSHEET_ID = "100";

    private final SizingImportConfig importConfig = new SizingImportConfig();
    private final SizingVersionProvider versionProvider = mock(SizingVersionProvider.class);

    private SizingSkipper subject = new TimeboxSkipper(importConfig, versionProvider);

    @Test
    public void whenVersionIsLower_shouldSkip() {
        when(versionProvider.get(SPREADSHEET_ID))
                .thenReturn(4.1);

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));
    }

    @Test
    public void whenVersionIsGraterThanMinimalVersion_dontShouldSkip() {
        when(versionProvider.get(SPREADSHEET_ID))
            .thenReturn(4.2);

        assertFalse(subject.shouldSkip(SPREADSHEET_ID));
    }

    @Test
    public void whenVersionIsGrater_dontShouldSkip() {
        when(versionProvider.get(SPREADSHEET_ID))
                .thenReturn(4.3);

        assertFalse(subject.shouldSkip(SPREADSHEET_ID));
    }
}
