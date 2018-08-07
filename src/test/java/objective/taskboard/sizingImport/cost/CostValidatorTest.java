package objective.taskboard.sizingImport.cost;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_COST;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_SCOPE;
import static objective.taskboard.sizingImport.cost.CostColumnMappingDefinitionProvider.EFFORT;
import static objective.taskboard.sizingImport.cost.CostColumnMappingDefinitionProvider.INDIRECT_COSTS;
import static objective.taskboard.sizingImport.cost.CostColumnMappingDefinitionProvider.INDIRECT_COSTS_KEY;
import static objective.taskboard.sizingImport.cost.CostColumnMappingDefinitionProvider.TOTAL_INDIRECT_COSTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.sizingImport.JiraFacade;
import objective.taskboard.sizingImport.SheetColumnDefinition;
import objective.taskboard.sizingImport.SizingImportConfig;
import objective.taskboard.sizingImport.SizingImportConfig.IndirectCosts;
import objective.taskboard.sizingImport.SizingImportValidator.ValidationContext;
import objective.taskboard.sizingImport.SizingImportValidator.ValidationContextWithSpreadsheet;
import objective.taskboard.sizingImport.SizingImportValidator.ValidationResult;

public class CostValidatorTest {

    private static final String SPREADSHEET_ID = "100";
    private static final Long PARENT_TYPE_ID = 1L;

    private final SizingImportConfig config = new SizingImportConfig();
    private final JiraFacade jiraFacade = mock(JiraFacade.class);
    private final SpreadsheetsManager spreadsheetsManager = mock(SpreadsheetsManager.class);
    private final CostColumnMappingDefinitionProvider costColumnProvider = mock(CostColumnMappingDefinitionProvider.class);
    private final CostSheetSkipper costSheetSkipper = mock(CostSheetSkipper.class);
    private final ValidationContext context = new ValidationContext(SPREADSHEET_ID, spreadsheetsManager);
    private List<List<Object>> sheetCostData;
    private ValidationContextWithSpreadsheet contextWithSpreadsheet;

    private final CostValidator subject = new CostValidator(config, jiraFacade, costColumnProvider, costSheetSkipper);

    @Before
    public void setup() {
        IndirectCosts indirectCosts = new IndirectCosts(); 
        indirectCosts.setParentTypeId(PARENT_TYPE_ID); 
        config.setIndirectCosts(indirectCosts); 

        sheetCostData = asList(
                asList("Indirect Costs",       "Key", "Role Level", "Role",      "Hour($)", "Weekly Hours", "% of Project", "Project Days", "Effort"),
                asList("Dev Support",          "",    "Tech Lead",  "Tech lead", "$225",    "5.0",          "",             "46",           "1,446"),
                asList("Coach",                "",    "Coach",      "Coach",     "$225",    "",             "25%",          "46",           "723"),
                asList("Total Indirect Costs", "",    "",           "",          "",        "",             "",             "46",           "2,169"));

        when(spreadsheetsManager.readRange(SPREADSHEET_ID, format("'%s'", SHEET_COST))).thenAnswer((i) -> sheetCostData);

        contextWithSpreadsheet = new ValidationContextWithSpreadsheet(context, asList(SHEET_SCOPE, "Timeline", SHEET_COST));

        when(costColumnProvider.getHeaderMappings()).thenReturn(asList(
                new ColumnMappingDefinition(INDIRECT_COSTS,     "A"),
                new ColumnMappingDefinition(INDIRECT_COSTS_KEY, "B"),
                new ColumnMappingDefinition(EFFORT,             "I")));

        when(costColumnProvider.getFooterMappings()).thenReturn(asList(
                new ColumnMappingDefinition(TOTAL_INDIRECT_COSTS, "A")));

        when(costSheetSkipper.shouldSkip(SPREADSHEET_ID)).thenReturn(false);
    }

    @Test
    public void shouldReturnSuccessWhenEverythingIsCorrect() {
        ValidationResult result = subject.validate(contextWithSpreadsheet);
        assertTrue(result.success);
    }

    @Test
    public void shouldFailWhenSpreadsheetsHasNoSheetEntitledAsCost() {
        contextWithSpreadsheet = new ValidationContextWithSpreadsheet(context, asList(SHEET_SCOPE, "Timeline"));

        ValidationResult result = subject.validate(contextWithSpreadsheet);

        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Specified URL should contain a sheet with title “Cost”.", result.errorMessage);
        assertEquals("Found sheets: Scope, Timeline.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenIndirectCostsHasEmptyHeader() {
        sheetCostData = asList(
                asList(),
                asList("Dev Support",          "", "Tech Lead", "Tech lead", "$225", "5.0", "",    "46", "1,446"),
                asList("Coach",                "", "Coach",     "Coach",     "$225", "",    "25%", "46", "723"),
                asList("Total Indirect Costs", "", "",          "",          "",     "",    "",    "46", "2,169"));

        ValidationResult result = subject.validate(contextWithSpreadsheet);

        assertInvalidSheetCostFormat(result);
    }

    @Test
    public void shouldFailWhenIndirectCostsHasNoHeader() {
        sheetCostData = asList(
                null,
                asList("Dev Support",          "", "Tech Lead", "Tech lead", "$225", "5.0", "",    "46", "1,446"),
                asList("Coach",                "", "Coach",     "Coach",     "$225", "",    "25%", "46", "723"),
                asList("Total Indirect Costs", "", "",          "",          "",     "",    "",    "46", "2,169"));

        ValidationResult result = subject.validate(contextWithSpreadsheet);

        assertInvalidSheetCostFormat(result);
    }

    @Test
    public void shouldFailWhenThereAreNoRowsInCostSheet() {
        sheetCostData = emptyList();

        ValidationResult result = subject.validate(contextWithSpreadsheet);

        assertInvalidSheetCostFormat(result);
    }

    private void assertInvalidSheetCostFormat(ValidationResult result) {
        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Missing required header columns in “Cost“ sheet.", result.errorMessage);
        assertEquals("“Indirect Costs” column should be placed at position “A”, “Key” column should be placed at position “B”, "
                + "“Effort” column should be placed at position “I”. And all the columns should be placed at the same row.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenIndirectCostsHeaderColumnIsOutOfBound() {
        when(costColumnProvider.getHeaderMappings()).thenReturn(asList(
                new ColumnMappingDefinition(INDIRECT_COSTS,       "Z"),
                new ColumnMappingDefinition(INDIRECT_COSTS_KEY,   "B"),
                new ColumnMappingDefinition(EFFORT,               "I")));

        ValidationResult result = subject.validate(contextWithSpreadsheet);

        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Missing required header columns in “Cost“ sheet.", result.errorMessage);
        assertEquals("“Indirect Costs” column should be placed at position “Z”. And all the columns should be placed at the same row.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenSomeIndirectCostsHeaderColumnsAreMissing() {
        sheetCostData = asList(
                asList("Indirect Costs",       "", "Role Level", "Role",      "Hour($)", "Weekly Hours", "% of Project", "Project Days", ""),
                asList("Dev Support",          "", "Tech Lead",  "Tech lead", "$225",    "5.0",          "",             "46",           "1,446"),
                asList("Coach",                "", "Coach",      "Coach",     "$225",    "",             "25%",          "46",           "723"),
                asList("Total Indirect Costs", "", "",           "",          "",        "",             "",             "46",           "2,169"));

        ValidationResult result = subject.validate(contextWithSpreadsheet);

        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Missing required header columns in “Cost“ sheet.", result.errorMessage);
        assertEquals("“Key” column should be placed at position “B”, “Effort” column should be placed at position “I”. "
                + "And all the columns should be placed at the same row.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenSomeIndirectCostsHeaderColumnsAreInIncorrectRow() {
        sheetCostData = asList(
                asList("Progress",             "Key", "Grand Total", "",          "",        "XS",           "S",            "",             "Effort"),
                asList("Indirect Costs",       "",    "Role Level",  "Role",      "Hour($)", "Weekly Hours", "% of Project", "Project Days", ""),
                asList("Dev Support",          "",    "Tech Lead",   "Tech lead", "$225",    "5.0",          "",             "46",           "1,446"),
                asList("Coach",                "",    "Coach",       "Coach",     "$225",    "",             "25%",          "46",           "723"),
                asList("Total Indirect Costs", "",    "",            "",          "",        "",             "",             "46",           "2,169"));

        ValidationResult result = subject.validate(contextWithSpreadsheet);

        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Missing required columns in “Cost“ sheet (row 2).", result.errorMessage);
        assertEquals("“Key” column should be placed at position “B”, “Effort” column should be placed at position “I”.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenSomeIndirectCostsHeaderColumnsAreInIncorrectColumn() {
        sheetCostData = asList(
                asList("Progress",             "Key",        "Grand Total", "",          "",        "XS",           "S",            "",       "Effort"),
                asList("Indirect Costs",       "Role Level", "Key",         "Role",      "Hour($)", "Weekly Hours", "% of Project", "Effort", "Project Days"),
                asList("Dev Support",          "Tech Lead",  "",            "Tech lead", "$225",    "5.0",          "",             "1,446",  "46"),
                asList("Coach",                "Coach",      "",            "Coach",     "$225",    "",             "25%",          "723",    "46"),
                asList("Total Indirect Costs", "",           "",            "",          "",        "",             "",             "2,169",  "46"));

        ValidationResult result = subject.validate(contextWithSpreadsheet);

        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Incorrectly positioned columns in “Cost“ sheet (row 2).", result.errorMessage);
        assertEquals("“Key” column should be moved to position “B”, “Effort” column should be moved to position “I”.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenIndirectCostsHasNoFooter() {
        sheetCostData = asList(
                asList("Indirect Costs", "Key", "Role Level", "Role",      "Hour($)", "Weekly Hours", "% of Project", "Project Days", "Effort"),
                asList("Dev Support",    "",    "Tech Lead",  "Tech lead", "$225",    "5.0",          "",             "46",           "1,446"),
                asList("Coach",          "",    "Coach",      "Coach",     "$225",    "",             "25%",          "46",           "723"));

        ValidationResult result = subject.validate(contextWithSpreadsheet);

        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Missing required footer columns in “Cost“ sheet.", result.errorMessage);
        assertEquals("“Total Indirect Costs” column should be placed at position “A”. "
                + "And all the columns should be placed at the same row after the header row 1.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenSomeIndirectCostsFooterColumnsAreIncorrectlyPositioned() {
        when(costColumnProvider.getFooterMappings()).thenReturn(asList(
                new ColumnMappingDefinition(TOTAL_INDIRECT_COSTS,                "A"),
                new ColumnMappingDefinition(new SheetColumnDefinition("Phase"),  "C"),
                new ColumnMappingDefinition(new SheetColumnDefinition("Demand"), "D")));

        sheetCostData = asList(
                asList("Progress",             "87%", "Phase",      "Demand",    "",        "XS",           "S",            "",             ""),
                asList("Indirect Costs",       "Key", "Role Level", "Role",      "Hour($)", "Weekly Hours", "% of Project", "Project Days", "Effort"),
                asList("Dev Support",          "",    "Tech Lead",  "Tech lead", "$225",    "5.0",          "",             "46",           "1,446"),
                asList("Coach",                "",    "Coach",      "Coach",     "$225",    "",             "25%",          "46",           "723"),
                asList("Total Indirect Costs", "",    "",           "",          "",        "",             "",             "46",           "2,169"));

        ValidationResult result = subject.validate(contextWithSpreadsheet);

        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Missing required columns in “Cost“ sheet (row 5).", result.errorMessage);
        assertEquals("“Phase” column should be placed at position “C”, “Demand” column should be placed at position “D”.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenIndirectCostsFooterIsBeforeHeader() {
        sheetCostData = asList(
                asList("Total Indirect Costs", "",    "",           "",          "",        "",             "",             "46",           "2,169"),
                asList("Indirect Costs",       "Key", "Role Level", "Role",      "Hour($)", "Weekly Hours", "% of Project", "Project Days", "Effort"),
                asList("Dev Support",          "",    "Tech Lead",  "Tech lead", "$225",    "5.0",          "",             "46",           "1,446"),
                asList("Coach",                "",    "Coach",      "Coach",     "$225",    "",             "25%",          "46",           "723"));

        ValidationResult result = subject.validate(contextWithSpreadsheet);

        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Incorrect order for header and footer in “Cost“ sheet.", result.errorMessage);
        assertEquals("Footer row 1 should be moved to after the header row 2.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenIndirectCostsHasNoData() {
        sheetCostData = asList(
                asList("Indirect Costs",       "Key", "Role Level", "Role", "Hour($)", "Weekly Hours", "% of Project", "Project Days", "Effort"),
                asList("Total Indirect Costs", "",    "",           "",     "",        "",             "",             "46",           "2,169"));

        ValidationResult result = subject.validate(contextWithSpreadsheet);

        assertTrue(result.failed());
        assertEquals("Invalid spreadsheet format: Missing Indirect Costs in “Cost“ sheet.", result.errorMessage);
        assertEquals("Indirect Costs to import should start after the header row 1 and should end before the footer row 2.", result.errorDetail);
    }

    @Test
    public void shouldFailWhenIndirectCostsHasInvalidConfiguredIssueTypeId() {
        when(jiraFacade.getIssueTypeById(PARENT_TYPE_ID)).thenThrow(new IllegalArgumentException("There's no Issue Type with given ID: 1"));

        ValidationResult result = subject.validate(contextWithSpreadsheet);

        assertTrue(result.failed());
        assertEquals("Invalid configured issue type ids for “Cost“ sheet.", result.errorMessage);
        assertEquals("There's no Issue Type with given ID: 1", result.errorDetail);
    }

    @Test
    public void shouldReturnSuccessWhenHasNoCostSheetButSkipCostSheetValidation() {
        when(costSheetSkipper.shouldSkip(SPREADSHEET_ID)).thenReturn(true);

        contextWithSpreadsheet = new ValidationContextWithSpreadsheet(context, asList(SHEET_SCOPE, "Timeline"));

        ValidationResult result = subject.validate(contextWithSpreadsheet);
        assertTrue(result.success);
    }

}
