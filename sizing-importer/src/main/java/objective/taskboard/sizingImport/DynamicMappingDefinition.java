package objective.taskboard.sizingImport;

import java.util.Optional;

class DynamicMappingDefinition extends MappingDefinition {
    private final String columnId;
    private final Optional<String> defaultColumnLetter;
    private final boolean mappingRequired;
    
    public DynamicMappingDefinition(SheetColumnDefinition columnDefinition, String columnId, boolean mappingRequired, Optional<String> defaultColumnLetter) {
        super(columnDefinition);
        this.columnId = columnId;
        this.mappingRequired = mappingRequired;
        this.defaultColumnLetter = defaultColumnLetter;
    }
    
    public DynamicMappingDefinition(SheetColumnDefinition columnDefinition, String columnId) {
        this(columnDefinition, columnId, false, Optional.empty());
    }
    
    public String getColumnId() {
        return columnId;
    }
    
    public Optional<String> getDefaultColumnLetter() {
        return defaultColumnLetter;
    }

    public boolean isMappingRequired() {
        return mappingRequired;
    }
}