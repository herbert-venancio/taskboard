package objective.taskboard.spreadsheet;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;

class TableEditorCache {

    private final SimpleSpreadsheetEditor spreadsheetEditor;
    private Map<String, SimpleTableEditor> cache;

    public TableEditorCache(SimpleSpreadsheetEditor spreadsheetEditor) {
        this.spreadsheetEditor = spreadsheetEditor;
    }

    public SimpleTableEditor get(String tableName) {
        ensureLoaded();
        return cache.get(tableName);
    }

    private void ensureLoaded() {
        if(cache != null)
            return;

        cache = new HashMap<>();
        File tablesFolder = new File(spreadsheetEditor.getExtractedSheetDirectory(), "xl/tables");
        Pattern nameRegex = Pattern.compile("name=\"([^\"]+)?\"");
        for(File tableFile : tablesFolder.listFiles((FileFilter) FileFilterUtils.suffixFileFilter("xml", IOCase.INSENSITIVE))) {
            try (Scanner scanner = new Scanner(tableFile)) {
                String name = scanner.findWithinHorizon(nameRegex, 0);
                Matcher m = nameRegex.matcher(name);
                m.matches();
                name = m.group(1);
                cache.put(name, new SimpleTableEditor(spreadsheetEditor, "xl/tables/" + tableFile.getName()));
            } catch (FileNotFoundException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }
}
