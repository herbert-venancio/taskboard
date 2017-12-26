package objective.taskboard.spreadsheet;

public interface TableEditor {

    CellRange getRange();
    void recreate(CellRange range);
    void save();

}
