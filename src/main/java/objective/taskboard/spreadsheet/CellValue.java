package objective.taskboard.spreadsheet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

interface CellValue {

    void writeValue(Element cell, Document sheetDoc);

}
