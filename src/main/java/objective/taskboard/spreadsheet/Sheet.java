package objective.taskboard.spreadsheet;

public interface Sheet {

    void save();

    SheetRow createRow();
    
    SheetRow getOrCreateRow(int rowNumber);

    void truncate();

    String getSheetPath();

}