package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProvider.EXTRA_FIELD_ID_TAG;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CimIssueType;

import objective.taskboard.sizingImport.SizingImportConfig.SheetMap.DefaultColumn;
import objective.taskboard.sizingImport.SizingImportConfig.SheetMap.ExtraField;

public class SheetColumnDefinitionProviderTest {
        
    private SheetColumnDefinitionProvider subject;
    private JiraFacade jiraFacade = mock(JiraFacade.class);

    @Before
    public void setUp() {
        DefaultColumn defaultColumnCf2 = new DefaultColumn();
        defaultColumnCf2.setFieldId("cf_2");
        defaultColumnCf2.setColumn("W");

        SizingImportConfig importConfig = new SizingImportConfig();

        importConfig.getSheetMap().setIssuePhase("A");
        importConfig.getSheetMap().setIssueDemand("B");
        importConfig.getSheetMap().setIssueFeature("C");
        importConfig.getSheetMap().setType("D");
        importConfig.getSheetMap().setIssueKey("E");
        importConfig.getSheetMap().setInclude("F");
        
        importConfig.getSheetMap().getExtraFields().add(new ExtraField("f8", "Acceptance Criteria", "Z"));
        importConfig.getSheetMap().getExtraFields().add(new ExtraField("f9", "Assumptions", "T"));
        
        importConfig.getSheetMap().setDefaultColumns(asList(defaultColumnCf2));
        
        subject = new SheetColumnDefinitionProvider(importConfig, jiraFacade);
    }
    
    @Test
    public void getStaticColumns() {
        List<StaticMappingDefinition> mappingDefinitions = subject.getStaticMappings();

        assertEquals(8, mappingDefinitions.size());

        StaticMappingDefinition mappingDefinition = mappingDefinitions.get(0);
        assertEquals("Phase", mappingDefinition.getColumnDefinition().getName());

        mappingDefinition = mappingDefinitions.get(6);
        assertEquals("Assumptions", mappingDefinition.getColumnDefinition().getName());
        assertEquals("T", mappingDefinition.getColumnLetter());
        assertEquals("f9", mappingDefinition.getColumnDefinition().getTagValue(EXTRA_FIELD_ID_TAG));
        
        mappingDefinition = mappingDefinitions.get(7);
        assertEquals("Acceptance Criteria", mappingDefinition.getColumnDefinition().getName());
        assertEquals("Z", mappingDefinition.getColumnLetter());
        assertEquals("f8", mappingDefinition.getColumnDefinition().getTagValue(EXTRA_FIELD_ID_TAG));
    }
    
    @Test
    public void getDynamicColumns() {
        CimFieldInfo featureTSizeField  = new CimFieldInfo("cf_2", true,  "Feature TSize", null, null, null, null);
        CimFieldInfo uxTSizeField       = new CimFieldInfo("cf_3", false, "UX TSize",      null, null, null, null);
        CimFieldInfo useCasesField      = new CimFieldInfo("cf_4", false, "Use Cases",     null, null, null, null);
        CimFieldInfo taskTSizeField     = new CimFieldInfo("cf_5", false, "Task TSize",      null, null, null, null);
        
        when(jiraFacade.getSizingFieldIds()).thenReturn(asList(featureTSizeField.getId(), uxTSizeField.getId(), taskTSizeField.getId()));
        
        when(jiraFacade.requestFeatureTypes("PX")).thenReturn(asList(
                issueType(1L, "Feature", featureTSizeField, uxTSizeField, useCasesField),
                issueType(2L, "Task", taskTSizeField)));
        
        List<DynamicMappingDefinition> mappingDefinitions = subject.getDynamicMappings("PX");

        assertEquals(3, mappingDefinitions.size());
        
        DynamicMappingDefinition mappingDefinition = mappingDefinitions.get(0);
        assertEquals("sizing:cf_2", mappingDefinition.getColumnId());
        assertEquals("Feature TSize", mappingDefinition.getColumnDefinition().getName());
        assertTrue(mappingDefinition.getDefaultColumnLetter().isPresent());
        assertEquals("W", mappingDefinition.getDefaultColumnLetter().get());
        
        mappingDefinition = mappingDefinitions.get(1);
        assertEquals("sizing:cf_5", mappingDefinition.getColumnId());
        assertEquals("Task TSize", mappingDefinition.getColumnDefinition().getName());
        assertFalse(mappingDefinition.getDefaultColumnLetter().isPresent());
        
        mappingDefinition = mappingDefinitions.get(2);
        assertEquals("sizing:cf_3", mappingDefinition.getColumnId());
        assertEquals("UX TSize", mappingDefinition.getColumnDefinition().getName());
        assertFalse(mappingDefinition.getDefaultColumnLetter().isPresent());
    }
    
    private static CimIssueType issueType(Long id, String name, CimFieldInfo... fields) {
        Map<String, CimFieldInfo> fieldsMap = Stream.of(fields).collect(toMap(CimFieldInfo::getId, Function.identity()));
        return new CimIssueType(null, id, name, false, null, null, fieldsMap);
    }
}
