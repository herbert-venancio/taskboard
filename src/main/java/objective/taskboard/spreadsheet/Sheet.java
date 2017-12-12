package objective.taskboard.spreadsheet;

public interface Sheet {

    void save();

    SheetRow createRow();

    void truncate(int starting);

    String getSheetPath();

}