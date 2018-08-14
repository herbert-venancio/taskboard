package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static objective.taskboard.sizingImport.SheetColumnDefinitionProviderScope.EXTRA_FIELD_ID_TAG;
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

import objective.taskboard.jira.client.JiraCreateIssue;
import objective.taskboard.sizingImport.SizingImportConfig.SheetMap.DefaultColumn;
import objective.taskboard.sizingImport.SizingImportConfig.SheetMap.ExtraField;

public class SheetColumnDefinitionProviderScopeTest {
        
    private SheetColumnDefinitionProviderScope subject;
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
        
        subject = new SheetColumnDefinitionProviderScope(importConfig, jiraFacade);
    }
    
    @Test
    public void getStaticColumns() {
        List<StaticMappingDefinition> mappingDefinitions = subject.getStaticMappings();

        assertEquals(8, mappingDefinitions.size());

        StaticMappingDefinition mappingDefinition = mappingDefinitions.get(0);
        assertEquals("Phase", mappingDefinition.getColumnDefinition().getName());
        assertEquals("A", mappingDefinition.getColumnLetter());

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
        JiraCreateIssue.FieldInfoMetadata featureTSizeField  = new JiraCreateIssue.FieldInfoMetadata("cf_2", true,  "Feature TSize");
        JiraCreateIssue.FieldInfoMetadata uxTSizeField       = new JiraCreateIssue.FieldInfoMetadata("cf_3", false, "UX TSize");
        JiraCreateIssue.FieldInfoMetadata useCasesField      = new JiraCreateIssue.FieldInfoMetadata("cf_4", false, "Use Cases");
        JiraCreateIssue.FieldInfoMetadata taskTSizeField     = new JiraCreateIssue.FieldInfoMetadata("cf_5", false, "Task TSize");
        
        when(jiraFacade.getSizingFieldIds()).thenReturn(asList(featureTSizeField.id, uxTSizeField.id, taskTSizeField.id));
        
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
    
    private static JiraCreateIssue.IssueTypeMetadata issueType(Long id, String name, JiraCreateIssue.FieldInfoMetadata... fields) {
        Map<String, JiraCreateIssue.FieldInfoMetadata> fieldsMap = Stream.of(fields).collect(toMap(f -> f.id, Function.identity()));
        return new JiraCreateIssue.IssueTypeMetadata(id, name, false, fieldsMap);
    }
}
