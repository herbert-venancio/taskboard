package objective.taskboard.spreadsheet;

import static java.util.stream.Collectors.toSet;
import static objective.taskboard.google.SpreadsheetUtils.columnIndexToLetter;
import static objective.taskboard.google.SpreadsheetUtils.columnLetterToIndex;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import objective.taskboard.google.SpreadsheetUtils.SpreadsheetA1Range;
import objective.taskboard.testUtils.OperationLoggers;
import objective.taskboard.testUtils.OperationLoggers.OperationLogger;

public class SimpleSpreadsheetEditorMock implements SpreadsheetEditor {

    private final OperationLogger logger = OperationLoggers.create();
    private final Map<String,String> sheetPathByName = new LinkedHashMap<>();
    private final Map<String, SheetTable> tables = new HashMap<>();

    public void open() {
        logger.append("Spreadsheet Open", "global");
    }

    public Sheet getSheet(String sheetName) {
        return new SheetMock(sheetName, tables, logger);
    }

    public String loggerString(String... sheetNames) {
        if (sheetNames.length == 0)
            return logger.getAllLog();
        
        Collection<String> markersToFilter = Stream.of(sheetNames).map(s -> "sheet:" + s).collect(toSet());
        markersToFilter.add("global");

        return logger.getLog(markersToFilter);
    }

    public void clearLogger() {
        logger.clear();
    }
    
    public void addTable(String sheetName, String tableName, SpreadsheetA1Range reference) {
        tables.put(sheetName + ":" + tableName, new SheetTableMock(tableName, reference));
    }

    @Override
    public Sheet createSheet(String sheetName) {
        Optional<Integer> max = sheetPathByName.values().stream()
                .filter(v->v.startsWith("xl/worksheets/sheet"))
                .map(v->Integer.parseInt(v.replaceAll("xl/worksheets/sheet([0-9]+).xml", "$1")))
                .sorted()
                .max(Integer::compareTo);
        int sheetFileNumber = max.orElse(0)+1;

        String sheetPath = "xl/worksheets/sheet"+sheetFileNumber+".xml";

        sheetPathByName.put(sheetName, sheetPath);

        return new SheetMock(sheetName, tables, logger);
    }

    @Override
    public Sheet getOrCreateSheet(String sheetName) {
        if(sheetPathByName.containsKey(sheetName)) {
            return getSheet(sheetName);
        } else {
            return createSheet(sheetName);
        }
    }

    @Override
    public byte[] toBytes() {
        return new byte[0];
    }

    @Override
    public void save() throws IOException {
        logger.append("Spreadsheet Save", "global");
    }

    @Override
    public void close() {
        logger.append("Spreadsheet Close", "global");
    }

    @Override
    public SpreadsheetStylesEditor getStylesEditor() {
        return null;
    }

    @Override
    public Map<String, Long> getSharedStrings() {
        return null;
    }

    @Override
    public String generateSharedStrings() throws IOException {
        return null;
    }

    @Override
    public File getExtractedSheetDirectory() {
        return null;
    }

    private static class SheetMock implements Sheet {
        private final String sheetName;
        private final OperationLogger logger;
        private final Map<String, SheetTable> tables;

        private int rowNumber = 0;

        public SheetMock(String sheetName, Map<String, SheetTable> tables, OperationLogger logger) {
            this.sheetName = sheetName;
            this.tables = tables;
            this.logger = OperationLoggers.wrap(logger, "sheet:" + sheetName);

            this.logger.append("Sheet Create: " + sheetName + "\n");
        }

        @Override
        public void save() {
            logger.append("Sheet \"" + sheetName + "\" Save\n");
        }

        @Override
        public SheetRowMock createRow() {
            rowNumber++;
            logger.append("Sheet \"" + sheetName + "\" Row Create: "+ rowNumber +"\n");
            return new SheetRowMock(sheetName, rowNumber, logger);
        }

        @Override
        public SheetRow getOrCreateRow(int rowNumber) {
            this.rowNumber = Math.max(this.rowNumber, rowNumber);
            logger.append("Sheet \"" + sheetName + "\" Row Get/Create: "+ rowNumber +"\n");
            return new SheetRowMock(sheetName, rowNumber, logger);
        }

        @Override
        public void truncate() {
        }

        @Override
        public String getSheetPath() {
            return null;
        }

        @Override
        public Optional<SheetTable> getTable(String tableName) {
            return Optional.ofNullable(tables.get(sheetName + ":" + tableName));
        }
    }

    private static class SheetRowMock implements SheetRow {
        private final String sheetname;
        private final int rowNum;
        private final OperationLogger logger;

        private int columIndex = 0;

        public SheetRowMock(String sheetname, int rowNum, OperationLogger logger) {
            this.sheetname = sheetname;
            this.rowNum = rowNum;
            this.logger = logger;
        }

        @Override
        public void addColumn(String value) {
            logger.append("Sheet \"" + sheetname + "\" Row \""+ rowNum +"\" AddColumn \""+ columnIndexToLetter(columIndex) + rowNum +"\": " + value + "\n");
            columIndex++;
        }

        @Override
        public void addColumn(Number value) {
            logger.append("Sheet \"" + sheetname + "\" Row \""+ rowNum +"\" AddColumn \""+ columnIndexToLetter(columIndex) + rowNum +"\": " + value + "\n");
            columIndex++;
        }

        @Override
        public void addColumn(Boolean value) {
            logger.append("Sheet \"" + sheetname + "\" Row \""+ rowNum +"\" AddColumn \""+ columnIndexToLetter(columIndex) + rowNum +"\": " + String.valueOf(value) + "\n");
            columIndex++;
        }

        @Override
        public void addColumn(ZonedDateTime value) {
            logger.append("Sheet \"" + sheetname + "\" Row \""+ rowNum +"\" AddColumn \""+ columnIndexToLetter(columIndex) + rowNum +"\": " + value + "\n");
            columIndex++;
        }
        
        @Override
        public void addColumn(LocalDateTime value) {
            logger.append("Sheet \"" + sheetname + "\" Row \""+ rowNum +"\" AddColumn \""+ columnIndexToLetter(columIndex) + rowNum +"\": " + value + "\n");
            columIndex++;
        }

        @Override
        public void addColumn(LocalDate value) {
            logger.append("Sheet \"" + sheetname + "\" Row \""+ rowNum +"\" AddColumn \""+ columnIndexToLetter(columIndex) + rowNum +"\": " + value + "\n");
            columIndex++;
        }

        @Override
        public void addFormula(String formula) {
            logger.append("Sheet \"" + sheetname + "\" Row \""+ rowNum +"\" AddFormula \""+ columnIndexToLetter(columIndex) + rowNum +"\": " + formula + "\n");
            columIndex++;
        }
        
        @Override
        public void setValue(String columnLetter, String value) {
            internalSetValue(columnLetter, value, "string");
        }

        @Override
        public void setValue(String columnLetter, Number value) {
            internalSetValue(columnLetter, value, "number");
        }

        @Override
        public void setValue(String columnLetter, Boolean value) {
            internalSetValue(columnLetter, value, "boolean");
        }

        @Override
        public void setValue(String columnLetter, LocalDateTime value) {
            internalSetValue(columnLetter, value, "date-time");
        }

        @Override
        public void setValue(String columnLetter, LocalDate value) {
            internalSetValue(columnLetter, value, "date");
        }

        @Override
        public void setFormula(String columnLetter, String value) {
            internalSetValue(columnLetter, value, "formula");
        }

        private void internalSetValue(String columnLetter, Object value, String type) {
            logger.append(String.format("Sheet \"%s\" Row \"%s\" SetValue (%s) \"%s\": %s\n", sheetname, rowNum, type, columnLetter + rowNum, value));
            columIndex = Math.max(columIndex, columnLetterToIndex(columnLetter));
        }
    }

    private static class SheetTableMock implements SheetTable {
        private final String name;
        private final SpreadsheetA1Range reference;
        
        public SheetTableMock(String name, SpreadsheetA1Range reference) {
            this.name = name;
            this.reference = reference;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public SpreadsheetA1Range getReference() {
            return reference;
        }
    }
}
