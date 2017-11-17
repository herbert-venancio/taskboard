package objective.taskboard.sizingImport;

class SheetColumnMapping {
        private final String columnId;
        private final String columnLetter;
        
        public SheetColumnMapping(String columnId, String columnLetter) {
            this.columnId = columnId;
            this.columnLetter = columnLetter.toUpperCase();
        }
        
        public String getColumnId() {
            return columnId;
        }
        
        public String getColumnLetter() {
            return columnLetter;
        }
    }