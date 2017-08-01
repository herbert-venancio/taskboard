package objective.taskboard.sizingImport;

import java.util.List;

class SheetDefinition {
    private final String lastColumnLetter;
    private final List<SheetStaticColumn> staticColumns;
    private final List<SheetColumnDefinition> dynamicColumns;
    
    public SheetDefinition(String lastColumnLetter, List<SheetStaticColumn> staticColumns, List<SheetColumnDefinition> dynamicColumns) {
        this.lastColumnLetter = lastColumnLetter;
        this.staticColumns = staticColumns;
        this.dynamicColumns = dynamicColumns;
    }
    
    public List<SheetStaticColumn> getStaticColumns() {
        return staticColumns;
    }
    
    public List<SheetColumnDefinition> getDynamicColumns() {
        return dynamicColumns;
    }

    public String getLastColumnLetter() {
        return lastColumnLetter;
    }
    
    static class SheetStaticColumn {
        private final String name;
        private final String columnLetter;
        
        public SheetStaticColumn(String name, String columnLetter) {
            this.name = name;
            this.columnLetter = columnLetter;
        }
        
        public String getName() {
            return name;
        }
        
        public String getColumnLetter() {
            return columnLetter;
        }
    }

    static class SheetColumnDefinition {
        private final String fieldId;
        private final String name;
        private final String defaultColumnLetter;
        private final boolean required;

        public SheetColumnDefinition(String fieldId, String name, boolean required, String defaultColumnLetter) {
            this.fieldId = fieldId;
            this.name = name;
            this.required = required;
            this.defaultColumnLetter = defaultColumnLetter;
        }
        
        public SheetColumnDefinition(String fieldId, String name, boolean required) {
            this(fieldId, name, required, null);
        }

        public String getFieldId() {
            return fieldId;
        }

        public String getName() {
            return name;
        }

        public String getDefaultColumnLetter() {
            return defaultColumnLetter;
        }
        
        public boolean isRequired() {
            return required;
        }
    }
}