package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.DEMAND;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.FEATURE;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.INCLUDE;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.KEY;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.PHASE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ScopeSheetParserTest {

    private final SizingImportConfig importConfig = new SizingImportConfig();
    private final ScopeSheetParser subject = new ScopeSheetParser(importConfig);
    
    private static final SheetColumnDefinition FIELD_1 = new SheetColumnDefinition("Field 1");
    private static final SheetColumnDefinition FIELD_2 = new SheetColumnDefinition("Field 2");

    @Before
    public void setUp() {
        importConfig.setDataStartingRowNumber(2);
        importConfig.setValueToIgnore("<NA>");
    }

    @Test
    public void getSpreedsheetData() {
        List<List<Object>> rows = asList(
                //     A                B                 C                  D              E                       F           G           H
                asList("Phase",         "Demand",         "Feature",         "Key",         "Acceptance",           "Include",  "Field 1",  "Field 2"),
                asList("VALUE_A_PHASE", "VALUE_A_DEMAND", "VALUE_A_FEATURE", "",            "VALUE_A_ACCEPCRITER",  "TRUE",     "VALUE_A1", "<NA>"),
                asList("VALUE_B_PHASE", "VALUE_B_DEMAND", "VALUE_B_FEATURE", "VALUE_B_KEY", "VALUE_B_ACCEPCRITER",  "TRUE",     "VALUE_B1", "   VALUE_B2   "),
                asList("VALUE_E_PHASE", "VALUE_E_DEMAND", "VALUE_E_FEATURE", "",            "VALUE_E_ACCEPCRITER",  "FALSE",    "VALUE_E1", "VALUE_E2"),
                asList("VALUE_F_PHASE", "VALUE_F_DEMAND", "VALUE_F_FEATURE", null,          "VALUE_F_ACCEPCRITER",  "TRUE",     ""),
                emptyList());

        SheetDefinition sheetDefinition = new SheetDefinition(
                asList(
                        new StaticMappingDefinition(PHASE,               "A"),
                        new StaticMappingDefinition(DEMAND,              "B"),
                        new StaticMappingDefinition(FEATURE,             "C"),
                        new StaticMappingDefinition(KEY,                 "D"),
                        new StaticMappingDefinition(INCLUDE,             "F")),
                asList(
                        new DynamicMappingDefinition(FIELD_1, "f1"),
                        new DynamicMappingDefinition(FIELD_2, "f2")));

        List<SheetColumnMapping> columnsMapping = asList(
                new SheetColumnMapping("f1", "G"), 
                new SheetColumnMapping("f2", "H"));

        List<SizingImportLineScope> result = subject.parse(rows, sheetDefinition, columnsMapping);
        Assert.assertEquals(3, result.size());

        SizingImportLineScope line = result.get(0);
        assertEquals("VALUE_A_PHASE", line.getPhase());
        assertEquals("VALUE_A_DEMAND", line.getDemand());
        assertEquals("VALUE_A_FEATURE", line.getFeature());
        assertNull(line.getJiraKey());
        assertEquals("VALUE_A1", line.getValue(FIELD_1));
        assertNull(line.getValue(FIELD_2));

        line = result.get(1);
        assertEquals("VALUE_B_PHASE", line.getPhase());
        assertEquals("VALUE_B_DEMAND", line.getDemand());
        assertEquals("VALUE_B_FEATURE", line.getFeature());
        assertEquals("VALUE_B_KEY", line.getJiraKey());
        assertEquals("VALUE_B1", line.getValue(FIELD_1));
        assertEquals("VALUE_B2", line.getValue(FIELD_2));
        
        line = result.get(2);
        assertEquals("VALUE_F_PHASE", line.getPhase());
        assertEquals("VALUE_F_DEMAND", line.getDemand());
        assertEquals("VALUE_F_FEATURE", line.getFeature());
        assertNull(line.getJiraKey());
        assertNull(line.getValue(FIELD_1));
        assertNull(line.getValue(FIELD_2));
    }

    @Test
    public void getSpreedsheetData_Empty() {
        List<List<Object>> rows = emptyList();

        SheetDefinition sheetDefinition = new SheetDefinition(
                asList(
                        new StaticMappingDefinition(PHASE,  "A"),
                        new StaticMappingDefinition(DEMAND, "B")),
                asList(
                        new DynamicMappingDefinition(FIELD_1, "f1")));
        
        List<SheetColumnMapping> columnsMapping = emptyList();

        List<SizingImportLineScope> result = subject.parse(rows, sheetDefinition, columnsMapping);
        assertEquals(0, result.size());
    }

    @Test
    public void getSpreedsheetData_Null() {
        List<List<Object>> rows = null;

        SheetDefinition sheetDefinition = new SheetDefinition(
                asList(
                        new StaticMappingDefinition(PHASE,  "A"),
                        new StaticMappingDefinition(DEMAND, "B")),
                asList(
                        new DynamicMappingDefinition(FIELD_1, "f1")));

        List<SheetColumnMapping> columnsMapping = emptyList();

        List<SizingImportLineScope> result = subject.parse(rows, sheetDefinition, columnsMapping);
        assertEquals(0, result.size());
    }
}
