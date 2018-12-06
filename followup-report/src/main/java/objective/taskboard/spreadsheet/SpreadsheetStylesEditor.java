package objective.taskboard.spreadsheet;

interface SpreadsheetStylesEditor {

    int getOrCreateNumberFormat(String format);

    void save();

    String getPathStyles();

}