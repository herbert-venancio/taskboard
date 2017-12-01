package objective.taskboard.sizingImport;

class SheetColumn {
    private final SheetColumnDefinition definition;
    private final String letter;

    public SheetColumn(SheetColumnDefinition definition, String letter) {
        this.definition = definition;
        this.letter = letter;
    }
    
    public SheetColumnDefinition getDefinition() {
        return definition;
    }
    
    public String getLetter() {
        return letter;
    }

    @Override
    public int hashCode() {
        return letter.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj == null || getClass() != obj.getClass())
            return false;
        
        SheetColumn other = (SheetColumn) obj;
        return letter.equals(other.letter);
    }
}
