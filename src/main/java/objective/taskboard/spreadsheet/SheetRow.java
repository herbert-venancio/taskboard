package objective.taskboard.spreadsheet;

import java.time.ZonedDateTime;

import org.w3c.dom.Node;

public interface SheetRow {

    Node buildNode();

    void addColumn(String value);

    void addColumn(Number value);

    void addColumn(ZonedDateTime value);

    void addFormula(String formula);

    int getColumnIndex();

    void save();

}