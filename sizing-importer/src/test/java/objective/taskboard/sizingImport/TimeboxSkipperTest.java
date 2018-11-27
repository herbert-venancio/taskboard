package objective.taskboard.sizingImport;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetsManager;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SIZING_METADATA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TimeboxSkipperTest {

    private static final String SPREADSHEET_ID = "100";

    private final SizingImportConfig importConfig = new SizingImportConfig();
    private final GoogleApiService googleApiService = mock(GoogleApiService.class);
    private final SizingVersionProvider versionProvider = mock(SizingVersionProvider.class);
    private final SpreadsheetsManager spreadsheetsManager = mock(SpreadsheetsManager.class);
    private List<List<Object>> sheetSizingMetaData;

    private SizingSkipper subject = new TimeboxSkipper(importConfig, versionProvider);

    @Before
    public void before() {
        when(googleApiService.buildSpreadsheetsManager()).thenReturn(spreadsheetsManager);

        sheetSizingMetaData = asList(
                asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""),
                asList("templateVersion", importConfig.getMinimalVersionForTimebox()));

        when(spreadsheetsManager.getSheetsTitles(SPREADSHEET_ID))
            .thenReturn(asList(SHEET_SIZING_METADATA));

        when(spreadsheetsManager.readRange(SPREADSHEET_ID, format("'%s'", SHEET_SIZING_METADATA)))
            .thenAnswer((i) -> sheetSizingMetaData);

        when(versionProvider.get(SPREADSHEET_ID))
            .thenReturn(4.0);
    }

    @Test
    public void shouldReturnTrue_whenSizingMetaDataSheetNotFound() {
        when(spreadsheetsManager.getSheetsTitles(SPREADSHEET_ID))
                .thenReturn(asList());

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));
    }

    @Test
    public void shouldReturnTrue_whenSpreadsheetVersionIsNotValid() {
        sheetSizingMetaData = asList(
            asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", "")
        );

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));

        sheetSizingMetaData = asList(
            asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""),
            asList("templateVersion")
        );

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));

        sheetSizingMetaData = asList(
            asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""),
            asList("templateVersion", null)
        );

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));

        sheetSizingMetaData = asList(
            asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""),
            asList("templateVersion", "invalid_version")
        );

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));
    }

    @Test
    public void shouldReturnTrue_whenVersionIsLower() {
        when(versionProvider.get(SPREADSHEET_ID))
                .thenReturn(4.1);

        when(spreadsheetsManager.getSheetsTitles(SPREADSHEET_ID))
                .thenReturn(asList());

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));
    }

    @Test
    public void shouldReturnFalse_whenVersionIsGraterThanMinimalVersion() {
        when(versionProvider.get(SPREADSHEET_ID))
            .thenReturn(4.2);

        when(spreadsheetsManager.getSheetsTitles(SPREADSHEET_ID))
            .thenReturn(asList());

        assertFalse(subject.shouldSkip(SPREADSHEET_ID));
    }
}
