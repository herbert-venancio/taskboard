package objective.taskboard.spreadsheet;

import static objective.taskboard.utils.IOUtilities.write;
import static objective.taskboard.utils.XmlUtils.asString;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import objective.taskboard.spreadsheet.SpreadsheetTable.TableColumn;
import objective.taskboard.spreadsheet.SpreadsheetTable.TableType;
import objective.taskboard.utils.XmlUtils;

public class SimpleTableEditor implements TableEditor {

    private SpreadsheetTable tableData = new SpreadsheetTable();
    private final SimpleSpreadsheetEditor spreadsheetEditor;

    private boolean modified = false;

    public SimpleTableEditor(SimpleSpreadsheetEditor spreadsheetEditor, String pathTable) {
        this.spreadsheetEditor = spreadsheetEditor;
        this.tableData.setPath(pathTable);
        doLoad();
    }

    @Override
    public CellRange getRange() {
        return tableData.getRef();
    }

    @Override
    public void recreate(CellRange range) {
        modified = true;
        if(tableData.getType() == TableType.queryTable) {
            FileUtils.deleteQuietly(new File(spreadsheetEditor.getExtractedSheetDirectory(), tableData.getRelPath()));
            FileUtils.deleteQuietly(new File(spreadsheetEditor.getExtractedSheetDirectory(), tableData.getQueryTablePath()));
            FileUtils.deleteQuietly(new File(spreadsheetEditor.getExtractedSheetDirectory(), "xl/connections.xml"));
            tableData.setType(null);
        }
        tableData.setRef(range);
        createTableColumns(tableData.getRef());
    }

    private void doLoad() {
        File tableFile = new File(spreadsheetEditor.getExtractedSheetDirectory(), tableData.getPath());
        try(InputStream inputStream = new FileInputStream(tableFile)) {
            Document doc = XmlUtils.asDocument(inputStream);
            doc.getDocumentElement().normalize();

            tableData.setType(getTableType(doc));
            if(tableData.getType() == TableType.queryTable) {
                tableData.setRelPath(getTableRelPath(tableData.getPath()));
                tableData.setQueryTablePath(getQueryTablePath(spreadsheetEditor, tableData.getRelPath()));
                tableData.setWorksheetSource(getWorksheetSource(spreadsheetEditor, tableData.getPath()));
            }
            tableData.setRef(getRef(doc));
            tableData.setColumns(initializeTableColumns(doc));
            tableData.setInitialColumnsCount(tableData.getColumns().size());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void doSave() {
        File tableFile = new File(spreadsheetEditor.getExtractedSheetDirectory(), tableData.getPath());
        write(tableFile, generateTable());
    }

    @Override
    public void save() {
        if (modified) {
            doSave();
            modified = false;
        }
    }

    private static TableType getTableType(Document doc) {
        Element table = (Element) doc.getFirstChild();
        String tableTypeName = table.getAttribute("tableType");
        if(StringUtils.isEmpty(tableTypeName))
            return null;

        return TableType.valueOf(tableTypeName);
    }

    private static String getTableRelPath(String pathTable) {
        String tableName = pathTable.substring(
                pathTable.lastIndexOf('/') + 1
        );
        return "xl/tables/_rels/" + tableName + ".rels";
    }

    private static String getQueryTablePath(SimpleSpreadsheetEditor spreadsheetEditor, String tableRelPath) throws IOException {
        File tableRelFile = new File(spreadsheetEditor.getExtractedSheetDirectory(), tableRelPath);
        try(InputStream inputStream = new FileInputStream(tableRelFile)) {
            Document doc = XmlUtils.asDocument(inputStream);
            NodeList nodes = XmlUtils.xpath(doc, "//Relationship/@Target");
            if(nodes.getLength() > 0) {
                return nodes.item(0).getTextContent().replaceFirst("\\.\\.", "xl");
            }
        }
        return null;
    }

    private static String getWorksheetSource(SimpleSpreadsheetEditor spreadsheetEditor, String pathTable) throws IOException {
        File sheetRelFolder = new File(spreadsheetEditor.getExtractedSheetDirectory(), "xl/worksheets/_rels");
        String pathTableUpdated = pathTable.replaceFirst("xl", "..");
        for(File relFile : sheetRelFolder.listFiles((FileFilter) FileFilterUtils.suffixFileFilter("xml.rels", IOCase.INSENSITIVE))) {
            try(InputStream inputStream = new FileInputStream(relFile)) {
                Document doc = XmlUtils.asDocument(inputStream);
                NodeList nodes = XmlUtils.xpath(doc, "//Relationship[@Target='" + pathTableUpdated + "']");
                if(nodes.getLength() > 0) {
                    String sheetName = relFile.getName();
                    sheetName = sheetName.substring(0, sheetName.length() - ".rels".length());
                    return "xl/worksheets/" + sheetName;
                }
            }
        }
        return null;
    }

    private static CellRange getRef(Document doc) {
        return CellRange.fromString(((Element) doc.getFirstChild()).getAttribute("ref"));
    }

    private static Map<Integer, TableColumn> initializeTableColumns(Document doc) {
        Map<Integer, TableColumn> tableColumns = new LinkedHashMap<>();
        NodeList nodes = doc.getElementsByTagName(SpreadsheetTable.TAG_TABLE_COLUMN);

        for(Node node : XmlUtils.iterable(nodes)) {
            TableColumn tableColumn = TableColumn.fromXmlNode((Element) node);
            tableColumns.put(Integer.valueOf(tableColumn.id), tableColumn);
        }

        return tableColumns;
    }

    private void createTableColumns(CellRange range) {
        tableData.getColumns().clear();
        tableData.setInitialColumnsCount(0);
        String[] headerNames = getHeaderNames(range);
        for(int i = 0; i < headerNames.length; ++i) {
            int nextId = nextId();
            String id = Integer.toString(nextId);
            String name = headerNames[i];
            TableColumn newColumn = new TableColumn(id, null, name, null);
            tableData.getColumns().put(nextId, newColumn);
        }
    }

    private int nextId() {
        int i = 1;
        while(tableData.getColumns().containsKey(i))
            i++;
        return i;
    }

    private String[] getHeaderNames(CellRange range) {
        SimpleSpreadsheetEditor.SimpleSheet sheet = spreadsheetEditor.new SimpleSheet(tableData.getWorksheetSource());
        SimpleSpreadsheetEditor.SheetRowEditor header = sheet.editRow(range.minRowIndex);

        Set<String> allNames = new HashSet<>();

        int min = range.minColumnIndex;
        int max = range.maxColumnIndex + 1;
        String[] headerNames = new String[max - min];
        int newColumnId = 0;
        for(int i = 0; i < headerNames.length; ++i){
            String value = header.getCellValue(i + min);
            if(value == null) {
                do {
                    value = "Column" + ++newColumnId;
                } while(allNames.contains(value));
                header.setCellValue(i + min, value);
            }
            allNames.add(value);
            headerNames[i] = value;
        }

        header.save();

        return headerNames;
    }

    private String generateTable() {
        try(InputStream inputStream = new FileInputStream(new File(spreadsheetEditor.getExtractedSheetDirectory(), tableData.getPath()))) {
            Document doc = XmlUtils.asDocument(inputStream);
            Element table = (Element) doc.getFirstChild();

            table.setAttribute("ref", tableData.getRef().toString());

            for(Node item : XmlUtils.iterable(table.getElementsByTagName("autoFilter"))) {
                Element autoFilter = (Element) item;
                autoFilter.setAttribute("ref", tableData.getRef().toString());
            }

            Element tableColumns = (Element) table.getElementsByTagName("tableColumns").item(0);

            XmlUtils.removeChildrenAfter(tableColumns, tableData.getInitialColumnsCount());

            List<TableColumn> newTableColumns = new ArrayList<>(tableData.getColumns().values())
                    .subList(tableData.getInitialColumnsCount(), tableData.getColumns().size());
            for(TableColumn newTableColumn : newTableColumns) {
                tableColumns.appendChild(newTableColumn.asXmlNode(doc));
            }
            tableColumns.setAttribute("count", Integer.toString(tableData.getRef().columnCount()));

            if(tableData.getType() == null)
                table.removeAttribute("tableType");
            else
                table.setAttribute("tableType", tableData.getType().name());

            return asString(doc);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
