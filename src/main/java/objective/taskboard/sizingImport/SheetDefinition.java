package objective.taskboard.sizingImport;

import java.util.List;

class SheetDefinition {
    private final List<StaticMappingDefinition> staticMappings;
    private final List<DynamicMappingDefinition> dynamicMappings;
    
    public SheetDefinition(List<StaticMappingDefinition> staticMappings, List<DynamicMappingDefinition> dynamicMappings) {
        this.staticMappings = staticMappings;
        this.dynamicMappings = dynamicMappings;
    }
    
    public List<StaticMappingDefinition> getStaticColumns() {
        return staticMappings;
    }
    
    public List<DynamicMappingDefinition> getDynamicColumns() {
        return dynamicMappings;
    }
}