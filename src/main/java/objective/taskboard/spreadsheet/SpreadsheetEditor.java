package objective.taskboard.spreadsheet;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public interface SpreadsheetEditor extends Closeable {

    SpreadsheetStylesEditor SpreadsheetStylesEditor = null;

    void open();

    Sheet getSheet(String sheetName);

    Sheet createSheet(String sheetName);

    Sheet getOrCreateSheet(String sheetName);

    byte[] toBytes();

    void save() throws IOException;

    Map<String, Long> getSharedStrings();

    String generateSharedStrings() throws IOException;

    File getExtractedSheetDirectory();

    SpreadsheetStylesEditor getStylesEditor();

}