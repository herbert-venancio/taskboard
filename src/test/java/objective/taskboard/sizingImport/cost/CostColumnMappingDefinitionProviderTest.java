package objective.taskboard.sizingImport.cost;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import objective.taskboard.sizingImport.SizingImportConfig;
import objective.taskboard.sizingImport.SizingImportConfig.IndirectCosts;

public class CostColumnMappingDefinitionProviderTest {

    private CostColumnMappingDefinitionProvider subject;

    @Before
    public void setUp() {
        SizingImportConfig importConfig = new SizingImportConfig();
        IndirectCosts indirectCosts = new IndirectCosts();
        indirectCosts.setIndirectCostsColumn("A");
        indirectCosts.setIssueKeyColumn("B");
        indirectCosts.setEffortColumn("R");
        indirectCosts.setTotalIndirectCostsColumn("A");
        importConfig.setIndirectCosts(indirectCosts);

        subject = new CostColumnMappingDefinitionProvider(importConfig);
    }

    @Test
    public void getHeaderMappings() {
        List<ColumnMappingDefinition> headerMappings = subject.getHeaderMappings();

        assertEquals(3, headerMappings.size());

        ColumnMappingDefinition headerMapping = headerMappings.get(0);
        assertEquals("Indirect Costs", headerMapping.getColumnDefinition().getName());
        assertEquals("A", headerMapping.getColumnLetter());
        headerMapping = headerMappings.get(1);
        assertEquals("Key", headerMapping.getColumnDefinition().getName());
        assertEquals("B", headerMapping.getColumnLetter());
        headerMapping = headerMappings.get(2);
        assertEquals("Effort", headerMapping.getColumnDefinition().getName());
        assertEquals("R", headerMapping.getColumnLetter());
    }

    @Test
    public void getFooterMappings() {
        List<ColumnMappingDefinition> footerMappings = subject.getFooterMappings();

        assertEquals(1, footerMappings.size());

        ColumnMappingDefinition footerMapping = footerMappings.get(0);
        assertEquals("Total Indirect Costs", footerMapping.getColumnDefinition().getName());
        assertEquals("A", footerMapping.getColumnLetter());
    }

}
