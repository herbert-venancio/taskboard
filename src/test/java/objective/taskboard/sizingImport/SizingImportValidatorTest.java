package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProvider.DEMAND;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProvider.FEATURE;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProvider.INCLUDE;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProvider.PHASE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.sizingImport.SizingImportValidator.ValidationResult;

public class SizingImportValidatorTest {
    
    private SizingImportConfig config = new SizingImportConfig();
    private GoogleApiService googleApiService= mock(GoogleApiService.class);
    private SpreadsheetsManager spreadsheetsManager = mock(SpreadsheetsManager.class);
    private JiraFacade jiraFacade = mock(JiraFacade.class);
    private SheetColumnDefinitionProvider columnDefinitionProvider = mock(SheetColumnDefinitionProvider.class);
    private List<List<Object>> sheetData;

    private SizingImportValidator subject = new SizingImportValidator(config, googleApiService, jiraFacade, columnDefinitionProvider);
    
    @Before
    public void setup() {
        config.setDataStartingRowNumber(2);

        Map<String, JiraCreateIssue.FieldInfoMetadata> featureFields = new HashMap<>();
        featureFields.put("f5", new JiraCreateIssue.FieldInfoMetadata("f5", false, "Assumptions"));
        featureFields.put("f6", new JiraCreateIssue.FieldInfoMetadata("f6", false, "Acceptance Criteria"));
        
        when(jiraFacade.requestFeatureTypes("OBJ")).thenReturn(asList(
                new JiraCreateIssue.IssueTypeMetadata(55L, "Feature", false, featureFields),
                new JiraCreateIssue.IssueTypeMetadata(55L, "Task", false, emptyMap())));

        when(googleApiService.buildSpreadsheetsManager()).thenReturn(spreadsheetsManager);
        
        sheetData = asList(
                asList("Phase", "Demand", "Feature / Task",  "Include", "Dev", "UX"),
                asList("P1",    "MVP",    "Login",           "true",    "M",   "L"),
                asList("P1",    "MVP",    "Home",            "true",    "S",   "L"));

        when(spreadsheetsManager.getSheetsTitles("100")).thenReturn(asList("Scope", "Timeline", "Cost"));
        when(spreadsheetsManager.readRange("100", "'Scope'")).thenAnswer((i) -> sheetData);
        
        when(columnDefinitionProvider.getStaticMappings()).thenReturn(asList(
                new StaticMappingDefinition(PHASE,   "A"),
                new StaticMappingDefinition(DEMAND,  "B"),
                new StaticMappingDefinition(FEATURE, "C"),
                new StaticMappingDefinition(INCLUDE, "D")));
    }

    @Test
    public void shouldReturnSuccessWhenEverythingIsCorrect() {
        ValidationResult result = subject.validate("OBJ", "100");
        assertTrue(result.success);
    }

    @Test
    public void shouldFailWhenSpreadsheetsHasNoSheetEntitledAsScope() {
        when(spreadsheetsManager.getSheetsTitles("100")).thenReturn(asList("Timeline", "Cost"));
        
        ValidationResult result = subject.validate("OBJ", "100");
        
        assertFalse(result.success);
        assertEquals("Invalid spreadsheet format: Specified URL should contain a sheet with title “Scope”.", result.errorMessage);
        assertEquals("Found sheets: Timeline, Cost.", result.errorDetail);
    }
    
    @Test
    public void shouldFailWhenDataStartAtIncorrectRow() {
        sheetData = asList(
                asList("Sizing", "",       "",        "X",       "X",   "X"),
                asList("Phase", "Demand", "Feature", "Include", "Dev", "UX"),
                asList("P1",    "MVP",    "Login",   "true",    "M",   "L"));
        
        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Invalid spreadsheet format: Row 1 of sheet “Scope” should contain the headers (e.g. Phase, Demand, Feature / Task).", result.errorMessage);
        assertEquals("Activities to import should start at row 2.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenNoDataIsPresent() {
        sheetData = asList(
                asList("Phase", "Demand", "Feature", "Include", "Dev", "UX"));
        
        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Invalid spreadsheet format: Row 1 of sheet “Scope” should contain the headers (e.g. Phase, Demand, Feature / Task).", result.errorMessage);
        assertEquals("Activities to import should start at row 2.", result.errorDetail);
    }
    
    @Test
    public void shouldFailWhenNoHeaderIsPresent() {
        sheetData = asList(
                null,
                asList("P1",    "MVP",    "Login",   "true",    "M",   "L"));
        
        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Invalid spreadsheet format: Row 1 of sheet “Scope” should contain the headers (e.g. Phase, Demand, Feature / Task).", result.errorMessage);
        assertEquals("Activities to import should start at row 2.", result.errorDetail);
    }
    
    @Test
    public void shouldFailWhenThereAreNoRows() {
        sheetData = emptyList();
        
        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Invalid spreadsheet format: Row 1 of sheet “Scope” should contain the headers (e.g. Phase, Demand, Feature / Task).", result.errorMessage);
        assertEquals("Activities to import should start at row 2.", result.errorDetail);
    }
    
    @Test
    public void shouldFailWhenSomeStaticColumnsAreMissing() {
        sheetData = asList(
                asList("Demand", "Feature / Task", "Dev", "UX"),
                asList("MVP",    "Login",          "M",   "L"));
        
        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Invalid spreadsheet format: Missing required columns.", result.errorMessage);
        assertEquals("“Phase” column should be placed at position “A”, “Include” column should be placed at position “D”.", result.errorDetail);
    }
    
    @Test
    public void shouldFailWhenStaticColumnsAreDuplicated() {
        sheetData = asList(
                asList("Phase", "Demand", "Include", "Feature / Task", "Include", "Dev", "Dev"),
                asList("P1",    "MVP",    "true",    "Login",          "true",    "M",   "L"));
        
        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Invalid spreadsheet format: Duplicate columns found.", result.errorMessage);
        assertEquals("“Include” column is showing up in positions “C”/“E”.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenSomeStaticColumnsAreIncorrectlyPositioned() {
        when(columnDefinitionProvider.getStaticMappings()).thenReturn(asList(
                new StaticMappingDefinition(PHASE,   "A"),
                new StaticMappingDefinition(DEMAND,  "B"),
                new StaticMappingDefinition(FEATURE, "C"),
                new StaticMappingDefinition(INCLUDE, "Z")));
        
        sheetData = asList(
                asList("Phase", "Demand", "Include", "Feature / Task", "Dev"),
                asList("P1",    "MVP",    "true",    "Login",          "L"));
        
        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Invalid spreadsheet format: Incorrectly positioned columns.", result.errorMessage);
        assertEquals("“Feature / Task” column should be moved to position “C”, “Include” column should be moved to position “Z”.", result.errorDetail);
    }
}
