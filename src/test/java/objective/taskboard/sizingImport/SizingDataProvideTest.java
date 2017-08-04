package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Test;

import objective.taskboard.google.SpreadsheetsManager;
import objective.taskboard.sizingImport.SizingSheetParser.SheetColumnMapping;

public class SizingDataProvideTest {
    
    private SpreadsheetsManager spreadsheetsManager = mock(SpreadsheetsManager.class);
    private SizingSheetParser parser = mock(SizingSheetParser.class);

    @Test
    public void shouldReadMappedRange_multiLetterColumns() {
        SizingImportConfig importConfig = defaultConfig();
        SizingDataProvider subject = new SizingDataProvider(new SheetStaticColumns(importConfig), parser);
        
        List<SheetColumnMapping> dynamicColumnsMapping = asList(
                new SheetColumnMapping("cf_1", "AB"),
                new SheetColumnMapping("cf_2", "T"));

        subject.getData(spreadsheetsManager, "123", dynamicColumnsMapping);
        
        verify(spreadsheetsManager).readRange("123", "A:AB");
    }
    
    @Test
    public void shouldReadMappedRange_staticColumnsAtRight() {
        SizingImportConfig importConfig = defaultConfig();
        importConfig.getSheetMap().setInclude("Z");

        SizingDataProvider subject = new SizingDataProvider(new SheetStaticColumns(importConfig), parser);
        
        List<SheetColumnMapping> dynamicColumnsMapping = asList(
                new SheetColumnMapping("cf_1", "D"),
                new SheetColumnMapping("cf_2", "E"));

        subject.getData(spreadsheetsManager, "123", dynamicColumnsMapping);
        
        verify(spreadsheetsManager).readRange("123", "A:Z");
    }

    private static SizingImportConfig defaultConfig() {
        SizingImportConfig importConfig = new SizingImportConfig();
        importConfig.getSheetMap().setIssuePhase("A");
        importConfig.getSheetMap().setIssueDemand("B");
        importConfig.getSheetMap().setIssueFeature("C");
        importConfig.getSheetMap().setIssueKey("D");
        importConfig.getSheetMap().setInclude("E");

        return importConfig;
    }
}
