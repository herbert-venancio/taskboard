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
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.sizingImport.SizingImportConfig.SheetMap.ExtraField;
import objective.taskboard.sizingImport.SizingImportValidator.ValidationResult;

public class SizingImportValidatorTest {
    
    private SizingImportConfig config = new SizingImportConfig();
    private GoogleApiService googleApiService= mock(GoogleApiService.class);
    private SpreadsheetsManager spreadsheetsManager = mock(SpreadsheetsManager.class);
    private JiraUtils jiraUtils = mock(JiraUtils.class);
    private SheetColumnDefinitionProvider columnDefinitionProvider = mock(SheetColumnDefinitionProvider.class);
    private List<List<Object>> sheetData;

    private SizingImportValidator subject = new SizingImportValidator(config, googleApiService, jiraUtils, columnDefinitionProvider);
    
    @Before
    public void setup() {
        config.setDataStartingRowNumber(2);
        config.getSheetMap().getExtraFields().add(new ExtraField("f5", "Assumptions", "T"));
        config.getSheetMap().getExtraFields().add(new ExtraField("f6", "Acceptance Criteria", "R"));
        
        when(jiraUtils.isAdminOfProject("OBJ")).thenReturn(true);
        
        Map<String, CimFieldInfo> featureFields = new HashMap<>();
        featureFields.put("f5", new CimFieldInfo("f5", false, "Assumptions", null, null, null, null));
        featureFields.put("f6", new CimFieldInfo("f6", false, "Acceptance Criteria", null, null, null, null));
        
        when(jiraUtils.requestFeatureCreateIssueMetadata("OBJ")).thenReturn(
                new CimIssueType(null, 55L, "Task", false, null, null, featureFields));
        
        when(jiraUtils.getSizingFields(any())).thenReturn(asList(
                new CimFieldInfo("cf_2", true, "Dev TSize", null, null, null, null),
                new CimFieldInfo("cf_3", false, "UX TSize", null, null, null, null)));

        when(googleApiService.buildSpreadsheetsManager()).thenReturn(spreadsheetsManager);
        
        sheetData = asList(
                asList("Phase", "Demand", "Feature", "Include", "Dev", "UX"),
                asList("P1",    "MVP",    "Login",   "true",    "M",   "L"),
                asList("P1",    "MVP",    "Home",    "true",    "S",   "L"));

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
    public void shouldFailWhenUserCantAdminTheProject() {
        when(jiraUtils.isAdminOfProject("OBJ")).thenReturn(false);
        
        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("You should have permission to admin this project in Jira.", result.errorMessage);
    }

    @Test
    public void shouldFailWhenIssueTypeFeatureHasNoTSizeFieldConfigured() {
        when(jiraUtils.getSizingFields(any())).thenReturn(emptyList());

        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Issue type “Task” should have at least one sizing field configured in it.", result.errorMessage);
        assertEquals("Please check the configuration of selected project in Jira.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenSomeExtraFieldIsNotConfiguredInIssueTypeFeature() {
        when(jiraUtils.requestFeatureCreateIssueMetadata("OBJ")).thenReturn(
                new CimIssueType(null, 55L, "Task", false, null, null, emptyMap()));

        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Issue type “Task” should have the following fields configured in it: Acceptance Criteria, Assumptions", result.errorMessage);
        assertEquals("Please check the configuration of selected project in Jira.", result.errorDetail);
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
        assertEquals("Invalid spreadsheet format: Row 1 of sheet “Scope” should contain the headers (e.g. Phase, Demand, Feature).", result.errorMessage);
        assertEquals("Activities to import should start at row 2.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenNoDataIsPresent() {
        sheetData = asList(
                asList("Phase", "Demand", "Feature", "Include", "Dev", "UX"));
        
        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Invalid spreadsheet format: Row 1 of sheet “Scope” should contain the headers (e.g. Phase, Demand, Feature).", result.errorMessage);
        assertEquals("Activities to import should start at row 2.", result.errorDetail);
    }
    
    @Test
    public void shouldFailWhenNoHeaderIsPresent() {
        sheetData = asList(
                null,
                asList("P1",    "MVP",    "Login",   "true",    "M",   "L"));
        
        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Invalid spreadsheet format: Row 1 of sheet “Scope” should contain the headers (e.g. Phase, Demand, Feature).", result.errorMessage);
        assertEquals("Activities to import should start at row 2.", result.errorDetail);
    }
    
    @Test
    public void shouldFailWhenThereAreNoRows() {
        sheetData = emptyList();
        
        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Invalid spreadsheet format: Row 1 of sheet “Scope” should contain the headers (e.g. Phase, Demand, Feature).", result.errorMessage);
        assertEquals("Activities to import should start at row 2.", result.errorDetail);
    }
    
    @Test
    public void shouldFailWhenSomeStaticColumnsAreMissing() {
        sheetData = asList(
                asList("Demand", "Feature", "Dev", "UX"),
                asList("MVP",    "Login",   "M",   "L"));
        
        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Invalid spreadsheet format: Missing required columns.", result.errorMessage);
        assertEquals("“Phase” column should be placed at position “A”, “Include” column should be placed at position “D”.", result.errorDetail);
    }
    
    @Test
    public void shouldFailWhenStaticColumnsAreDuplicated() {
        sheetData = asList(
                asList("Phase", "Demand", "Include", "Feature", "Include", "Dev", "Dev"),
                asList("P1",    "MVP",    "true",    "Login",   "true",    "M",   "L"));
        
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
                asList("Phase", "Demand", "Include", "Feature", "Dev"),
                asList("P1",    "MVP",    "true",    "Login",   "L"));
        
        ValidationResult result = subject.validate("OBJ", "100");

        assertFalse(result.success);
        assertEquals("Invalid spreadsheet format: Incorrectly positioned columns.", result.errorMessage);
        assertEquals("“Feature” column should be moved to position “C”, “Include” column should be moved to position “Z”.", result.errorDetail);
    }
}
