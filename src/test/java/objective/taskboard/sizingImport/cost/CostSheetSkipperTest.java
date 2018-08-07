package objective.taskboard.sizingImport.cost;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SIZING_METADATA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.sizingImport.SizingImportConfig;
import objective.taskboard.sizingImport.SizingImportConfig.IndirectCosts;
import objective.taskboard.sizingImport.cost.CostSheetSkipper;

public class CostSheetSkipperTest {

    private static final String SPREADSHEET_ID = "100";

    private final SizingImportConfig importConfig = new SizingImportConfig();
    private final GoogleApiService googleApiService = mock(GoogleApiService.class);
    private final SpreadsheetsManager spreadsheetsManager = mock(SpreadsheetsManager.class);
    private List<List<Object>> sheetSizingMetaData;

    private CostSheetSkipper subject = new CostSheetSkipper(importConfig, googleApiService);

    @Before
    public void before() {
        IndirectCosts indirectCosts = new IndirectCosts();
        importConfig.setIndirectCosts(indirectCosts);

        when(googleApiService.buildSpreadsheetsManager()).thenReturn(spreadsheetsManager);

        sheetSizingMetaData = asList(
                asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""),
                asList("templateVersion",                              importConfig.getMinimalVersionForCost()));

        when(spreadsheetsManager.getSheetsTitles(SPREADSHEET_ID)).thenReturn(asList(SHEET_SIZING_METADATA));
        when(spreadsheetsManager.readRange(SPREADSHEET_ID, format("'%s'", SHEET_SIZING_METADATA))).thenAnswer((i) -> sheetSizingMetaData);
    }

    @Test
    public void shouldReturnFalseWhenIndirectCostsPropertiesAreConfiguredAndSpreadsheetVersionIsValid() {
        assertFalse(subject.shouldSkip(SPREADSHEET_ID));
    }

    @Test
    public void shouldReturnTrueWhenIndirectCostsPropertiesAreNotConfigured() {
        importConfig.setIndirectCosts(null);

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));
    }

    @Test
    public void shouldReturnTrueWhenSizingMetaDataSheetNotFound() {
        when(spreadsheetsManager.getSheetsTitles(SPREADSHEET_ID)).thenReturn(asList());

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));
    }

    @Test
    public void shouldReturnTrueWhenSpreadsheetVersionIsNotValid() {
        sheetSizingMetaData = asList(
                asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""));

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));

        sheetSizingMetaData = asList(
                asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""),
                asList("templateVersion"));

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));

        sheetSizingMetaData = asList(
                asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""),
                asList("templateVersion",                              null));

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));

        sheetSizingMetaData = asList(
                asList("WARNING: DON'T CHANGE ANY DATA IN THIS SHEET", ""),
                asList("templateVersion",                              "invalid_version"));

        assertTrue(subject.shouldSkip(SPREADSHEET_ID));
    }

}
