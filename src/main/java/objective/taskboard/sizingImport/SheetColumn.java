package objective.taskboard.sizingImport;

import objective.taskboard.utils.ObjectUtils;

public class SheetColumn {
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
        return ObjectUtils.equals(this, obj, other -> letter.equals(other.letter));
    }
}
