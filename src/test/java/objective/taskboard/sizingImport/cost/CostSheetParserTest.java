package objective.taskboard.sizingImport.cost;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static objective.taskboard.sizingImport.SizingImportConfig.SHEET_COST;
import static objective.taskboard.sizingImport.cost.CostColumnMappingDefinitionProvider.EFFORT;
import static objective.taskboard.sizingImport.cost.CostColumnMappingDefinitionProvider.INDIRECT_COSTS;
import static objective.taskboard.sizingImport.cost.CostColumnMappingDefinitionProvider.INDIRECT_COSTS_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import objective.taskboard.google.GoogleApiService;
import objective.taskboard.google.SpreadsheetsManager;

public class CostSheetParserTest {

    private static final String SPREADSHEET_ID = "100";

    private final GoogleApiService googleApiService = mock(GoogleApiService.class);
    private final SpreadsheetsManager spreadsheetsManager = mock(SpreadsheetsManager.class);
    private final CostColumnMappingDefinitionProvider costColumnProvider = mock(CostColumnMappingDefinitionProvider.class);
    private final CostColumnMappingDefinitionRowProvider costRowProvider = mock(CostColumnMappingDefinitionRowProvider.class);
    private List<List<Object>> costData;

    private final CostSheetParser subject = new CostSheetParser(googleApiService, costColumnProvider, costRowProvider);

    @Before
    public void before() {
        when(googleApiService.buildSpreadsheetsManager()).thenReturn(spreadsheetsManager);

        costData = asList(
                emptyList(),
                //     A                       B      C             D            E          F               G               H               I
                asList("Indirect Costs",       "Key", "Role Level", "Role",      "Hour($)", "Weekly Hours", "% of Project", "Project Days", "Effort"),
                asList("Dev Support",          "I-1", "Tech Lead",  "Tech lead", "$225",    "5.0",          "",             "46",           "1,446"),
                asList("   Coach   ",          "",    "Coach",      "Coach",     "$225",    "",             "25%",          "46",           "723"),
                emptyList(),
                asList("Archtecture Support",  null,  "Architect",  "Architect", "$225",    "1.0",          "",             "46"),
                asList("Total Indirect Costs", "",    "",           "",          "",        "",             "",             "46",           "2,169"),
                emptyList());

        when(spreadsheetsManager.readRange(SPREADSHEET_ID, format("'%s'", SHEET_COST))).thenAnswer(i -> costData);
    }

    @Test
    public void getSpreadsheetData() {
        when(costColumnProvider.getHeaderMappings()).thenReturn(asList(
                        new ColumnMappingDefinition(INDIRECT_COSTS,     "A"),
                        new ColumnMappingDefinition(INDIRECT_COSTS_KEY, "B"),
                        new ColumnMappingDefinition(EFFORT,             "I")));

        when(costRowProvider.getDataStartingRowIndex(SPREADSHEET_ID)).thenReturn(2);
        when(costRowProvider.getDataEndingRowIndex(SPREADSHEET_ID)).thenReturn(5);

        List<SizingImportLineCost> result = subject.parse(SPREADSHEET_ID);
        Assert.assertEquals(3, result.size());

        SizingImportLineCost line = result.get(0);
        assertEquals("Dev Support", line.getIndirectCosts());
        assertEquals("I-1", line.getJiraKey());
        assertEquals("1,446", line.getEffort());

        line = result.get(1);
        assertEquals("Coach", line.getIndirectCosts());
        assertNull(line.getJiraKey());
        assertEquals("723", line.getEffort());

        line = result.get(2);
        assertEquals("Archtecture Support", line.getIndirectCosts());
        assertNull(line.getJiraKey());
        assertNull(line.getEffort());
    }

    @Test
    public void getSpreedsheetData_Empty() {
        costData = emptyList();

        List<SizingImportLineCost> result = subject.parse(SPREADSHEET_ID);
        assertEquals(0, result.size());
    }

    @Test
    public void getSpreedsheetData_Null() {
        costData = null;

        List<SizingImportLineCost> result = subject.parse(SPREADSHEET_ID);
        assertEquals(0, result.size());
    }
}
