package objective.taskboard.sizingImport.cost;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_COST;
import static objective.taskboard.sizingImport.cost.CostColumnMappingDefinitionProvider.EFFORT;
import static objective.taskboard.sizingImport.cost.CostColumnMappingDefinitionProvider.INDIRECT_COSTS;
import static objective.taskboard.sizingImport.cost.CostColumnMappingDefinitionProvider.INDIRECT_COSTS_KEY;
import static objective.taskboard.sizingImport.cost.CostColumnMappingDefinitionProvider.TOTAL_INDIRECT_COSTS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetsManager;

public class CostColumnMappingDefinitionRowProviderTest {

    private static final String SPREADSHEET_ID = "100";

    private final GoogleApiService googleApiService = mock(GoogleApiService.class);
    private final SpreadsheetsManager spreadsheetsManager = mock(SpreadsheetsManager.class);
    private final CostColumnMappingDefinitionProvider costColumnProvider = mock(CostColumnMappingDefinitionProvider.class);
    private List<List<Object>> costData;

    private final CostColumnMappingDefinitionRowProvider subject = new CostColumnMappingDefinitionRowProvider(googleApiService, costColumnProvider);

    @Before
    public void before() {
        when(googleApiService.buildSpreadsheetsManager()).thenReturn(spreadsheetsManager);

        costData = asList(
                emptyList(),
                asList("Indirect Costs",       "Key", "Role Level", "Role",      "Hour($)", "Weekly Hours", "% of Project", "Project Days", "Effort"),
                asList("Dev Support",          "I-1", "Tech Lead",  "Tech lead", "$225",    "5.0",          "",             "46",           "1,446"),
                asList("   Coach   ",          "",    "Coach",      "Coach",     "$225",    "",             "25%",          "46",           "723"),
                emptyList(),
                asList("Archtecture Support",  null,  "Architect",  "Architect", "$225",    "1.0",          "",             "46"),
                asList("Total Indirect Costs", "",    "",           "",          "",        "",             "",             "46",           "2,169"),
                emptyList());

        when(spreadsheetsManager.readRange(SPREADSHEET_ID, format("'%s'", SHEET_COST))).thenAnswer(i -> costData);

        when(costColumnProvider.getHeaderMappings()).thenReturn(asList(
                new ColumnMappingDefinition(INDIRECT_COSTS,     "A"),
                new ColumnMappingDefinition(INDIRECT_COSTS_KEY, "B"),
                new ColumnMappingDefinition(EFFORT,             "I")));

        when(costColumnProvider.getFooterMappings()).thenReturn(asList(
                new ColumnMappingDefinition(TOTAL_INDIRECT_COSTS, "A")));
    }

    @Test
    public void whenGetStartingAndEndingRowIndexFromProperlyFormattedRows_thenShouldReturnTheCorrectRowIndexes() {
        Assert.assertEquals(2, subject.getDataStartingRowIndex(SPREADSHEET_ID).intValue());
        assertEquals(5, subject.getDataEndingRowIndex(SPREADSHEET_ID).intValue());
    }

    @Test
    public void whenGetStartingAndEndingRowIndexFromIncorrectlyFormattedRows_thenShouldReturnZero() {
        costData = asList(
                emptyList(),
                asList("",                    "Key", "Role Level", "Role",      "Hour($)", "Weekly Hours", "% of Project", "Project Days", "Effort"),
                asList("Dev Support",         "I-1", "Tech Lead",  "Tech lead", "$225",    "5.0",          "",             "46",           "1,446"),
                asList("   Coach   ",         "",    "Coach",      "Coach",     "$225",    "",             "25%",          "46",           "723"),
                emptyList(),
                asList("Archtecture Support", null,  "Architect",  "Architect", "$225",    "1.0",          "",             "46"),
                asList("",                    "",    "",           "",          "",        "",             "",             "46",           "2,169"),
                emptyList());

        assertEquals(0, subject.getDataStartingRowIndex(SPREADSHEET_ID).intValue());
        assertEquals(0, subject.getDataEndingRowIndex(SPREADSHEET_ID).intValue());
    }

    @Test
    public void whenGetStartingAndEndingRowIndexWithNoHeaderAndFooterColumns_thenShouldReturnZero() {
        when(costColumnProvider.getHeaderMappings()).thenReturn(asList());
        when(costColumnProvider.getFooterMappings()).thenReturn(asList());

        assertEquals(0, subject.getDataStartingRowIndex(SPREADSHEET_ID).intValue());
        assertEquals(0, subject.getDataEndingRowIndex(SPREADSHEET_ID).intValue());
    }

}
