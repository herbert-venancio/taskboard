package objective.taskboard.sizingImport;

class StaticMappingDefinition extends MappingDefinition {
    private final String columnLetter;

    public StaticMappingDefinition(SheetColumnDefinition columnDefinition, String columnLetter) {
        super(columnDefinition);
        this.columnLetter = columnLetter;
    }
    
    public String getColumnLetter() {
        return columnLetter;
    }
}