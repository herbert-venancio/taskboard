package objective.taskboard.sizingImport;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;

import objective.taskboard.sizingImport.SheetDefinition.SheetColumnDefinition;
import objective.taskboard.sizingImport.SizingImportConfig.SheetMap.DefaultColumn;

public class DynamicColumnsDefinitionBuilderTest {
        
    private DynamicColumnsDefinitionBuilder subject;

    @Before
    public void setUp() {
        Collection<String> configuredTShirtSizeFieldsId = asList("cf_2", "cf_3");

        List<CimFieldInfo> featureIssueFields = Arrays.asList(
                new CimFieldInfo("cf_1", true, "User", null, null, null, null),
                new CimFieldInfo("cf_2", true, "Feature TSize", null, null, null, null),
                new CimFieldInfo("cf_3", false, "UX TSize", null, null, null, null));
        
        DefaultColumn defaultColumnCf2 = new DefaultColumn();
        defaultColumnCf2.setFieldId("cf_2");
        defaultColumnCf2.setColumn("W");

        Collection<DefaultColumn> defaultColumns = asList(defaultColumnCf2);

        subject = new DynamicColumnsDefinitionBuilder(configuredTShirtSizeFieldsId, featureIssueFields)
                .setDefaultColumns(defaultColumns);
    }
    
    @Test
    public void build() {
        List<SheetColumnDefinition> dynamicColumnsDefinition = subject.build();

        assertEquals(2, dynamicColumnsDefinition.size());
        
        assertEquals("cf_2", dynamicColumnsDefinition.get(0).getFieldId());
        assertEquals("W", dynamicColumnsDefinition.get(0).getDefaultColumnLetter());
        assertTrue(dynamicColumnsDefinition.get(0).isRequired());
        
        assertEquals("cf_3", dynamicColumnsDefinition.get(1).getFieldId());
        assertNull(dynamicColumnsDefinition.get(1).getDefaultColumnLetter());
        assertFalse(dynamicColumnsDefinition.get(1).isRequired());
    }
}
