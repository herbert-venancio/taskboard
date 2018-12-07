package objective.taskboard.sizingImport;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetsManager;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import java.util.List;

import static java.lang.String.format;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SIZING_METADATA;
import static objective.taskboard.utils.NumberUtils.numberEquals;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SizingVersionProviderTest {

    private static final String SPREADSHEET_ID = "100";

    private final SizingImportConfig importConfig = new SizingImportConfig();
    private final GoogleApiService googleApiService = mock(GoogleApiService.class);
    private final SpreadsheetsManager spreadsheetsManager = mock(SpreadsheetsManager.class);
    private List<List<Object>> sheetSizingMetaData;

    private SizingVersionProvider subject = new SizingVersionProvider(importConfig, googleApiService);

    @Before
    public void before() {
        importConfig.setVersionRowIndex(1);

        sheetSizingMetaData = asList(
            asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""),
            asList("templateVersion", importConfig.getMinimalVersionForTimebox())
        );

        when(googleApiService.buildSpreadsheetsManager())
            .thenReturn(spreadsheetsManager);

        when(spreadsheetsManager.getSheetsTitles(SPREADSHEET_ID))
            .thenReturn(asList(SHEET_SIZING_METADATA));

        when(spreadsheetsManager.readRange(SPREADSHEET_ID, format("'%s'", SHEET_SIZING_METADATA)))
            .thenAnswer((i) -> sheetSizingMetaData);
    }

    @Test
    public void whenSizingMetaDataSheetNotFound_shouldReturnZeroValue() {
        when(spreadsheetsManager.getSheetsTitles(SPREADSHEET_ID))
            .thenReturn(asList());

        assertTrue(numberEquals(0D, subject.get(SPREADSHEET_ID)));
    }

    @Test
    public void whenSpreadsheetVersionIsNotValid_shouldReturnZeroValue() {
        sheetSizingMetaData = asList(
            asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", "")
        );

        assertTrue(numberEquals(0D, subject.get(SPREADSHEET_ID)));

        sheetSizingMetaData = asList(
            asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""),
            asList("templateVersion")
        );

        assertTrue(numberEquals(0D, subject.get(SPREADSHEET_ID)));

        sheetSizingMetaData = asList(
            asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""),
            asList("templateVersion", null)
        );

        assertTrue(numberEquals(0D, subject.get(SPREADSHEET_ID)));

        sheetSizingMetaData = asList(
            asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""),
            asList("templateVersion", "invalid_version")
        );

        assertTrue(numberEquals(0D, subject.get(SPREADSHEET_ID)));
    }

    @Test
    public void whenTheSheetIsFilledWithVersion_shouldReturnTheCorrectVersion() {
        sheetSizingMetaData = asList(
            asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""),
            asList("templateVersion", "4.2")
        );

        assertTrue(numberEquals(4.2, subject.get(SPREADSHEET_ID)));
    }
}
