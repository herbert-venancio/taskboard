package objective.taskboard.sizingImport;

abstract class MappingDefinition {
    private final SheetColumnDefinition columnDefinition;

    public MappingDefinition(SheetColumnDefinition columnDefinition) {
        this.columnDefinition = columnDefinition;
    }
    
    public SheetColumnDefinition getColumnDefinition() {
        return columnDefinition;
    }
}