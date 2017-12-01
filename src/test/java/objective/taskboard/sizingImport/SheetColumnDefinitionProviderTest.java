package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProvider.EXTRA_FIELD_ID_TAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;

import objective.taskboard.sizingImport.SizingImportConfig.SheetMap.DefaultColumn;
import objective.taskboard.sizingImport.SizingImportConfig.SheetMap.ExtraField;

public class SheetColumnDefinitionProviderTest {
        
    private SheetColumnDefinitionProvider subject;

    @Before
    public void setUp() {
        DefaultColumn defaultColumnCf2 = new DefaultColumn();
        defaultColumnCf2.setFieldId("cf_2");
        defaultColumnCf2.setColumn("W");

        SizingImportConfig importConfig = new SizingImportConfig();

        importConfig.getSheetMap().setIssuePhase("A");
        importConfig.getSheetMap().setIssueDemand("B");
        importConfig.getSheetMap().setIssueFeature("C");
        importConfig.getSheetMap().setIssueKey("D");
        importConfig.getSheetMap().setInclude("E");
        
        importConfig.getSheetMap().getExtraFields().add(new ExtraField("f8", "Acceptance Criteria", "Z"));
        importConfig.getSheetMap().getExtraFields().add(new ExtraField("f9", "Assumptions", "T"));
        
        importConfig.getSheetMap().setDefaultColumns(asList(defaultColumnCf2));
        
        subject = new SheetColumnDefinitionProvider(importConfig);
    }
    
    @Test
    public void getStaticColumns() {
        List<StaticMappingDefinition> mappingDefinitions = subject.getStaticMappings();

        assertEquals(7, mappingDefinitions.size());

        StaticMappingDefinition mappingDefinition = mappingDefinitions.get(0);
        assertEquals("Phase", mappingDefinition.getColumnDefinition().getName());

        mappingDefinition = mappingDefinitions.get(5);
        assertEquals("Assumptions", mappingDefinition.getColumnDefinition().getName());
        assertEquals("T", mappingDefinition.getColumnLetter());
        assertEquals("f9", mappingDefinition.getColumnDefinition().getTagValue(EXTRA_FIELD_ID_TAG));
        
        mappingDefinition = mappingDefinitions.get(6);
        assertEquals("Acceptance Criteria", mappingDefinition.getColumnDefinition().getName());
        assertEquals("Z", mappingDefinition.getColumnLetter());
        assertEquals("f8", mappingDefinition.getColumnDefinition().getTagValue(EXTRA_FIELD_ID_TAG));
    }
    
    @Test
    public void getDynamicColumns() {
        List<CimFieldInfo> sizingFields = Arrays.asList(
                new CimFieldInfo("cf_2", true,  "Feature TSize", null, null, null, null),
                new CimFieldInfo("cf_3", false, "UX TSize",      null, null, null, null));

        List<DynamicMappingDefinition> mappingDefinitions = subject.getDynamicMappings(sizingFields);

        assertEquals(2, mappingDefinitions.size());
        
        DynamicMappingDefinition mappingDefinition = mappingDefinitions.get(0);
        assertEquals("sizing:cf_2", mappingDefinition.getColumnId());
        assertEquals("Feature TSize", mappingDefinition.getColumnDefinition().getName());
        assertTrue(mappingDefinition.getDefaultColumnLetter().isPresent());
        assertEquals("W", mappingDefinition.getDefaultColumnLetter().get());
        
        mappingDefinition = mappingDefinitions.get(1);
        assertEquals("sizing:cf_3", mappingDefinition.getColumnId());
        assertEquals("UX TSize", mappingDefinition.getColumnDefinition().getName());
        assertFalse(mappingDefinition.getDefaultColumnLetter().isPresent());
    }
}
