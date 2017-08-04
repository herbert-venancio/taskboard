package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import objective.taskboard.sizingImport.SizingSheetParser.SheetColumnMapping;

public class SizingSheetParserTest {

    private SizingImportConfig importConfig = new SizingImportConfig();
    private SizingSheetParser subject = new SizingSheetParser(importConfig);

    @Before
    public void setUp() {
        importConfig.setDataStartingRowNumber(2);
        importConfig.setValueToIgnore("<NA>");
        
        importConfig.getSheetMap().setIssuePhase("A");
        importConfig.getSheetMap().setIssueDemand("B");
        importConfig.getSheetMap().setIssueFeature("C");
        importConfig.getSheetMap().setIssueKey("D");
        importConfig.getSheetMap().setInclude("E");
    }

    @Test
    public void getSpreedsheetData() throws IOException {
        List<List<Object>> rows = asList(
                //     A                B                 C                  D              E           F           G 
                asList("Phase",         "Demand",         "Feature",         "Key",         "Include",  "V1",       "V2"),
                asList("VALUE_A_PHASE", "VALUE_A_DEMAND", "VALUE_A_FEATURE", "",            "TRUE",     "VALUE_A1", "VALUE_A2"),
                asList("VALUE_B_PHASE", "VALUE_B_DEMAND", "VALUE_B_FEATURE", "VALUE_B_KEY", "TRUE",     "VALUE_B1", "VALUE_B2"),
                asList("VALUE_E_PHASE", "VALUE_E_DEMAND", "VALUE_E_FEATURE", "",            "FALSE",    "VALUE_E1", "VALUE_E2"),
                asList("VALUE_F_PHASE", "VALUE_F_DEMAND", "VALUE_F_FEATURE", "",            "TRUE",     "VALUE_F1", "VALUE_F2"),
                emptyList());

        List<SheetColumnMapping> dynamicColumnsMapping = asList(
                new SheetColumnMapping("FIELD_1", "F"), 
                new SheetColumnMapping("FIELD_2", "G"));

        List<SizingImportLine> result = subject.getSpreedsheetData(rows, dynamicColumnsMapping);
        Assert.assertEquals(3, result.size());

        SizingImportLine line = result.get(0);
        assertEquals("VALUE_A_PHASE", line.getPhase());
        assertEquals("VALUE_A_DEMAND", line.getDemand());
        assertEquals("VALUE_A_FEATURE", line.getFeature());
        assertEquals("", line.getJiraKey());
        assertEquals(2, line.getFields().size());
        assertEquals("FIELD_1", line.getFields().get(0).getId());
        assertEquals("VALUE_A1", line.getFields().get(0).getValue());
        assertEquals("FIELD_2", line.getFields().get(1).getId());
        assertEquals("VALUE_A2", line.getFields().get(1).getValue());

        line = result.get(1);
        assertEquals("VALUE_B_PHASE", line.getPhase());
        assertEquals("VALUE_B_DEMAND", line.getDemand());
        assertEquals("VALUE_B_FEATURE", line.getFeature());
        assertEquals("VALUE_B_KEY", line.getJiraKey());
        assertEquals(2, line.getFields().size());
        assertEquals("FIELD_1", line.getFields().get(0).getId());
        assertEquals("VALUE_B1", line.getFields().get(0).getValue());
        assertEquals("FIELD_2", line.getFields().get(1).getId());
        assertEquals("VALUE_B2", line.getFields().get(1).getValue());
        
        line = result.get(2);
        assertEquals("VALUE_F_PHASE", line.getPhase());
        assertEquals("VALUE_F_DEMAND", line.getDemand());
        assertEquals("VALUE_F_FEATURE", line.getFeature());
        assertEquals("", line.getJiraKey());
        assertEquals(2, line.getFields().size());
        assertEquals("FIELD_1", line.getFields().get(0).getId());
        assertEquals("VALUE_F1", line.getFields().get(0).getValue());
        assertEquals("FIELD_2", line.getFields().get(1).getId());
        assertEquals("VALUE_F2", line.getFields().get(1).getValue());
    }

    @Test
    public void getSpreedsheetData_Empty() throws IOException {
        List<List<Object>> rows = emptyList();
        List<SheetColumnMapping> dynamicColumnsMapping = emptyList();

        List<SizingImportLine> result = subject.getSpreedsheetData(rows, dynamicColumnsMapping);
        assertEquals(0, result.size());
    }
}
