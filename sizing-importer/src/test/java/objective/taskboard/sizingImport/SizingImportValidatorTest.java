package objective.taskboard.sizingImport;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.*;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_COST;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SCOPE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.google.SpreadsheetsManager.SpreadsheeNotFoundException;
import objective.taskboard.sizingImport.SizingImportValidator.ValidationResult;
import objective.taskboard.sizingImport.cost.CostValidator;

public class SizingImportValidatorTest {
    
    private static final String SPREADSHEET_ID = "100";

    private SizingImportConfig config = new SizingImportConfig();
    private GoogleApiService googleApiService= mock(GoogleApiService.class);
    private SpreadsheetsManager spreadsheetsManager = mock(SpreadsheetsManager.class);
    private TimeboxSkipper timeboxSkipper = mock(TimeboxSkipper.class);
    private SheetColumnDefinitionProviderScope scopeColumnProvider = mock(SheetColumnDefinitionProviderScope.class);
    private CostValidator costValidator = mock(CostValidator.class);
    private List<List<Object>> sheetScopeData;

    private SizingImportValidator subject = new SizingImportValidator(config, googleApiService, scopeColumnProvider, costValidator, timeboxSkipper);
    
    @Before
    public void setup() {
        config.setTabHeadersRowNumber(1);
        config.setDataStartingRowNumber(2);

        when(timeboxSkipper.shouldSkip(any())).thenReturn(false);
        when(googleApiService.buildSpreadsheetsManager()).thenReturn(spreadsheetsManager);

        sheetScopeData = asList(
                asList("Phase", "Demand", "Feature",  "Include", "Dev", "UX"),
                asList("P1",    "MVP",    "Login",           "true",    "M",   "L"),
                asList("P1",    "MVP",    "Home",            "true",    "S",   "L"));

        when(spreadsheetsManager.getSheetsTitles(SPREADSHEET_ID)).thenReturn(asList(SHEET_SCOPE, "Timeline", SHEET_COST));
        when(spreadsheetsManager.readRange(SPREADSHEET_ID, format("'%s'", SHEET_SCOPE))).thenAnswer((i) -> sheetScopeData);

        when(scopeColumnProvider.getStaticMappings())
            .thenReturn(asList(
                new StaticMappingDefinition(PHASE,   "A"),
                new StaticMappingDefinition(DEMAND,  "B"),
                new StaticMappingDefinition(FEATURE, "C"),
                new StaticMappingDefinition(INCLUDE, "D"))
            );

        when(costValidator.validate(any())).thenReturn(ValidationResult.success());
    }

    @Test
    public void shouldReturnSuccessWhenEverythingIsCorrect() {
        ValidationResult result = subject.validate(SPREADSHEET_ID);
        assertTrue(result.success);
    }

    @Test
    public void shouldFailWhenSpreadsheetNotExists() {
        when(spreadsheetsManager.getSheetsTitles(SPREADSHEET_ID)).thenThrow(new SpreadsheeNotFoundException(SPREADSHEET_ID));

        ValidationResult result = subject.validate(SPREADSHEET_ID);

        assertTrue(result.failed());
        assertEquals("Spreadsheet canot be found. Please check the URL.", result.errorMessage);
    }

    @Test
    public void shouldFailWhenSpreadsheetsHasNoSheetEntitledAsScope() {
        when(spreadsheetsManager.getSheetsTitles(SPREADSHEET_ID)).thenReturn(asList("Timeline", SHEET_COST));
        
        ValidationResult result = subject.validate(SPREADSHEET_ID);
        
        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Specified URL should contain a sheet with title “Scope”.", result.errorMessage);
        assertEquals("Found sheets: Timeline, Cost.", result.errorDetail);
    }
    
    @Test
    public void shouldFailWhenDataStartAtIncorrectRow() {
        sheetScopeData = asList(
                asList("Sizing", "",       "",        "X",       "X",   "X"),
                asList("Phase", "Demand", "Feature", "Include", "Dev", "UX"),
                asList("P1",    "MVP",    "Login",   "true",    "M",   "L"));
        
        ValidationResult result = subject.validate(SPREADSHEET_ID);

        assertInvalidSheetScopeFormat(result);
    }

    @Test
    public void shouldFailWhenNoDataIsPresent() {
        sheetScopeData = asList(
                asList("Phase", "Demand", "Feature", "Include", "Dev", "UX"));
        
        ValidationResult result = subject.validate(SPREADSHEET_ID);

        assertInvalidSheetScopeFormat(result);
    }
    
    @Test
    public void shouldFailWhenNoHeaderIsPresent() {
        sheetScopeData = asList(
                null,
                asList("P1",    "MVP",    "Login",   "true",    "M",   "L"));
        
        ValidationResult result = subject.validate(SPREADSHEET_ID);

        assertInvalidSheetScopeFormat(result);
    }

    @Test
    public void shouldFailWhenEmptyHeaderIsPresent() {
        sheetScopeData = asList(
                asList(),
                asList("P1",    "MVP",    "Login",   "true",    "M",   "L"));

        ValidationResult result = subject.validate(SPREADSHEET_ID);

        assertInvalidSheetScopeFormat(result);
    }

    @Test
    public void shouldFailWhenThereAreNoRows() {
        sheetScopeData = emptyList();
        
        ValidationResult result = subject.validate(SPREADSHEET_ID);

        assertInvalidSheetScopeFormat(result);
    }

    private void assertInvalidSheetScopeFormat(ValidationResult result) {
        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Row 1 of sheet “Scope” should contain the headers (e.g. Phase, Demand, Feature).", result.errorMessage);
        assertEquals("Activities to import should start at row 2.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenSomeStaticColumnsAreMissing() {
        sheetScopeData = asList(
                asList("Demand", "Feature", "Dev", "UX"),
                asList("MVP",    "Login",          "M",   "L"));
        
        ValidationResult result = subject.validate(SPREADSHEET_ID);

        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Missing required columns in “Scope“ sheet (row 1).", result.errorMessage);
        assertEquals("“Phase” column should be placed at position “A”, “Include” column should be placed at position “D”.", result.errorDetail);
    }
    
    @Test
    public void shouldFailWhenStaticColumnsAreDuplicated() {
        sheetScopeData = asList(
                asList("Phase", "Demand", "Include", "Feature", "Include", "Dev", "Dev"),
                asList("P1",    "MVP",    "true",    "Login",          "true",    "M",   "L"));
        
        ValidationResult result = subject.validate(SPREADSHEET_ID);

        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Duplicate columns found in “Scope“ sheet (row 1).", result.errorMessage);
        assertEquals("“Include” column is showing up in positions “C”/“E”.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenSomeStaticColumnsAreIncorrectlyPositioned() {
        when(scopeColumnProvider.getStaticMappings()).thenReturn(asList(
                new StaticMappingDefinition(PHASE,   "A"),
                new StaticMappingDefinition(DEMAND,  "B"),
                new StaticMappingDefinition(FEATURE, "C"),
                new StaticMappingDefinition(INCLUDE, "Z")));
        
        sheetScopeData = asList(
                asList("Phase", "Demand", "Include", "Feature", "Dev"),
                asList("P1",    "MVP",    "true",    "Login",          "L"));
        
        ValidationResult result = subject.validate(SPREADSHEET_ID);

        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Incorrectly positioned columns in “Scope“ sheet (row 1).", result.errorMessage);
        assertEquals("“Feature” column should be moved to position “C”, “Include” column should be moved to position “Z”.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenCostValidatorFail() {
        when(costValidator.validate(any())).thenReturn(ValidationResult.fail("Invalid Cost sheet"));

        ValidationResult result = subject.validate(SPREADSHEET_ID);

        assertTrue(result.failed());
        assertEquals("Invalid Cost sheet", result.errorMessage);
    }

    @Test public void shouldImportTimeboxLineWithSuccess(){

        when(scopeColumnProvider.getStaticMappings())
            .thenReturn(asList(
                new StaticMappingDefinition(PHASE,   "A"),
                new StaticMappingDefinition(DEMAND,  "B"),
                new StaticMappingDefinition(FEATURE, "C"),
                new StaticMappingDefinition(TYPE, "D"),
                new StaticMappingDefinition(INCLUDE, "E"),
                new StaticMappingDefinition(TIMEBOX, "H")
            )
        );

        sheetScopeData = asList(
            asList("Phase", "Demand", "Feature",         "Type",    "Include", "Dev", "UX", "Timebox"),
            asList("P1",    "MVP",    "Feature One",     "Feature", "true",    "M",   "L",  ""),
            asList("P1",    "MVP",    "Task One",        "Task",    "true",    "S",   "L",  ""),
            asList("P1",    "MVP",    "Timebox One",     "Timebox", "true",    "",    "",   "4"));

        ValidationResult result = subject.validate(SPREADSHEET_ID);

        assertTrue(result.success);
    }

    @Test public void importShouldFail_whenImportTimeboxHeaderIsNotFound(){
        when(scopeColumnProvider.getStaticMappings())
            .thenReturn(asList(
                new StaticMappingDefinition(PHASE,   "A"),
                new StaticMappingDefinition(DEMAND,  "B"),
                new StaticMappingDefinition(FEATURE, "C"),
                new StaticMappingDefinition(TYPE, "D"),
                new StaticMappingDefinition(INCLUDE, "E"),
                new StaticMappingDefinition(TIMEBOX, "H")
            )
        );

        sheetScopeData = asList(
            asList("Phase", "Demand", "Feature",         "Type",    "Include", "Dev", "UX", ""),
            asList("P1",    "MVP",    "Feature One",     "Feature", "true",    "M",   "L",  ""),
            asList("P1",    "MVP",    "Task One",        "Task",    "true",    "S",   "L",  ""),
            asList("P1",    "MVP",    "Timebox One",     "Timebox", "true",    "",    "",   "4")
        );

        ValidationResult result = subject.validate(SPREADSHEET_ID);

        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Missing required columns in “Scope“ sheet (row 1).", result.errorMessage);
        assertEquals("“Timebox” column should be placed at position “H”.", result.errorDetail);
    }

    @Test public void shouldValidateOk_skippingTimeboxHeader(){
        when(timeboxSkipper.shouldSkip(any())).thenReturn(true);

        when(scopeColumnProvider.getStaticMappings())
            .thenReturn(asList(
                new StaticMappingDefinition(PHASE,   "A"),
                new StaticMappingDefinition(DEMAND,  "B"),
                new StaticMappingDefinition(FEATURE, "C"),
                new StaticMappingDefinition(TYPE, "D"),
                new StaticMappingDefinition(INCLUDE, "E"),
                new StaticMappingDefinition(TIMEBOX, "H")
            )
        );
        sheetScopeData = asList(
            asList("Phase", "Demand", "Feature",         "Type",    "Include", "Dev", "UX"),
            asList("P1",    "MVP",    "Feature One",     "Feature", "true",    "M",   "L"),
            asList("P1",    "MVP",    "Task One",        "Task",    "true",    "S",   "L")
        );

        ValidationResult result = subject.validate(SPREADSHEET_ID);

        assertTrue(result.success);
    }
}
