package objective.taskboard.spreadsheet;

import static java.util.stream.Collectors.toList;
import static objective.taskboard.google.SpreadsheetUtils.columnIndexToLetter;
import static objective.taskboard.utils.IOUtilities.write;
import static objective.taskboard.utils.ZipUtils.unzip;
import static objective.taskboard.utils.ZipUtils.zip;
import static org.apache.commons.lang.ObjectUtils.defaultIfNull;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import objective.taskboard.followup.FollowUpTemplate;
import objective.taskboard.utils.IOUtilities;
import objective.taskboard.utils.XmlUtils;

public class SimpleSpreadsheetEditor implements Closeable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SimpleSpreadsheetEditor.class);
    private static final String TAG_T_IN_SHARED_STRINGS = "t";
    private static final String PATH_SHARED_STRINGS = "xl/sharedStrings.xml";

    
    public Map<String, Long> sharedStrings;
    FollowUpTemplate template;
    private File directoryTempFollowup;
    private Map<String,String> sheetPathByName = new LinkedHashMap<>();
    private int initialSharedStringCount;

    public SimpleSpreadsheetEditor(FollowUpTemplate template) {
        this.template = template;
    }
    
    public void open() {
        directoryTempFollowup = decompressTemplate().toFile();
        sharedStrings = initializeSharedStrings();
        initializeWorkbookRelations();
    }

    private Map<String, Long> initializeSharedStrings() {
        InputStream inputStream = getSharedStringsInputStream();
        
        Map<String, Long> sharedStrings = new HashMap<>();
        Document doc = XmlUtils.asDocument(inputStream);
        doc.getDocumentElement().normalize();
        NodeList nodes = doc.getElementsByTagName(TAG_T_IN_SHARED_STRINGS);

        for (Long index = 0L; index < nodes.getLength(); index++) {
            Node node = nodes.item(index.intValue());
            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            sharedStrings.put(node.getTextContent(), index);
        }
        initialSharedStringCount = sharedStrings.size();
        return sharedStrings;
    }
    
    public Sheet getSheet(String sheetName) {
        return new Sheet(sheetPathByName.get(sheetName));
    }
    
    public byte[] toBytes() {
        Path pathFollowupXLSM = null;
        try {
            save();
            pathFollowupXLSM = compress(directoryTempFollowup.toPath());
            
            return Files.readAllBytes(pathFollowupXLSM);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        finally {
            if (pathFollowupXLSM != null && pathFollowupXLSM.toFile().exists())
                try {
                    Files.delete(pathFollowupXLSM);
                } catch (IOException e) {
                    log.warn("Could not remove " + pathFollowupXLSM.toString(), e);
                }
        }
    }
    

    @Override
    public void close() {
        if (directoryTempFollowup == null || !directoryTempFollowup.exists()) return;
        
        try {
            FileUtils.deleteDirectory(directoryTempFollowup);
        } catch (IOException e) {
            log.warn("Could not remove " + directoryTempFollowup.toString(), e);
        }
    }
    
    private InputStream getSharedStringsInputStream() {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(new File(directoryTempFollowup,"xl/sharedStrings.xml"));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return inputStream;
    }
    
    Map<String, Long> getSharedStrings() {
        return sharedStrings;
    }
    
    String generateSharedStrings() throws IOException {
        Document sharedStringsDoc = XmlUtils.asDocument(getSharedStringsInputStream());
        Node root = sharedStringsDoc.getElementsByTagName("sst").item(0);
        
        List<String> sharedStringsSorted = sharedStrings.keySet().stream()
            .sorted((s1, s2) -> sharedStrings.get(s1).compareTo(sharedStrings.get(s2)))
            .collect(toList());
        
        List<String> newStrings = sharedStringsSorted.subList(initialSharedStringCount, sharedStringsSorted.size());
        if (newStrings.size() > 0)
            root.appendChild(sharedStringsDoc.createTextNode("  "));

        for (String sharedString : newStrings) {
            Node si = sharedStringsDoc.createElement("si");
            Node t = sharedStringsDoc.createElement("t");
            si.appendChild(t);
            t.appendChild(sharedStringsDoc.createTextNode(sharedString));
            root.appendChild(si);
        }
        root.getAttributes().getNamedItem("uniqueCount").setNodeValue(Integer.toString(sharedStringsSorted.size()));

        return XmlUtils.asString(sharedStringsDoc);
    }

    private String getOrSetIndexInSharedStrings(String followUpDataAttrValue) {
        if (followUpDataAttrValue == null || followUpDataAttrValue.isEmpty())
            return "";

        Long index = sharedStrings.get(followUpDataAttrValue);
        if (index != null)
            return index+"";

        index = Long.valueOf(sharedStrings.size());
        sharedStrings.put(followUpDataAttrValue, index);
        return index+"";
    }
    
    private Path decompressTemplate() {
        Path pathFollowup;
        try {
            pathFollowup = Files.createTempDirectory("Followup");
            unzip(template.getPathFollowupTemplateXLSM().getInputStream(), pathFollowup);
            return pathFollowup;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void save() throws IOException {
        File fileSharedStrings = new File(directoryTempFollowup, PATH_SHARED_STRINGS);
        write(fileSharedStrings, generateSharedStrings());
    }
    
    private Path compress(Path directoryFollowup) throws IOException  {
        Path pathFollowupXLSM = Files.createTempFile("Followup", ".xlsm");
        zip(directoryFollowup, pathFollowupXLSM);
        return pathFollowupXLSM;
    }
    
    private void initializeWorkbookRelations() {
        final Document workbookRels = XmlUtils.asDocument(new File(directoryTempFollowup, "xl/_rels/workbook.xml.rels"));
        final NodeList relationShips = workbookRels.getElementsByTagName("Relationship");
        final Map<String,String> relations = new LinkedHashMap<>();
        for (int i = 0; i < relationShips.getLength(); i++) {
            Node sheetNode = relationShips.item(i);
            sheetNode.getAttributes().getNamedItem("Id");
            relations.put(sheetNode.getAttributes().getNamedItem("Id").getNodeValue(), 
                    sheetNode.getAttributes().getNamedItem("Target").getNodeValue());
        }
        
        final Document workbook = XmlUtils.asDocument(new File(directoryTempFollowup, "xl/workbook.xml"));
        
        final NodeList sheetList = workbook.getElementsByTagName("sheet");
        for (int i = 0; i < sheetList.getLength(); i++) {
            Node sheetNode = sheetList.item(i);
            String id = sheetNode.getAttributes().getNamedItem("r:id").getNodeValue();
            String name = sheetNode.getAttributes().getNamedItem("name").getNodeValue();
            sheetPathByName.put(name, "xl/"+relations.get(id));
        }
    }
    
    public class Sheet {
        List<SheetRow> rowsList = new LinkedList<>();
        private File sheetFile;
        int maxCol = 0;
        private Document sheetDoc;
        private Node sheetData;
        int rowCount;
        
        public Sheet(String sheetPath) {
            sheetFile = new File(directoryTempFollowup, sheetPath);
            sheetDoc = XmlUtils.asDocument(sheetFile);
            NodeList sheetDataTags = sheetDoc.getElementsByTagName("sheetData");
            if (sheetDataTags.getLength() == 0)
                throw new IllegalArgumentException("Malformed sheet part found. Missing sheetData");
            sheetData = sheetDataTags.item(0);
            rowCount = sheetDoc.getElementsByTagName("row").getLength();
        }
        
        public void save() {
            try {
                IOUtilities.write(sheetFile, XmlUtils.asString(sheetDoc));
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        
        public SheetRow createRow() {
            int nextRowNumber = getRowCount() + 1;
            return new SheetRow(this, nextRowNumber, sheetDoc);
        }

        private int getRowCount() {
            return rowCount;
        }
       
        public void truncate(int starting) {
            NodeList xmlRow = sheetDoc.getElementsByTagName("row");
            
            List<Node> rowsToKeep = new LinkedList<>();
            for (int i = 0; i < starting; i++) 
                rowsToKeep.add(xmlRow.item(i));
            
            while(sheetData.hasChildNodes()) 
                sheetData.removeChild(sheetData.getFirstChild());
            
            rowsToKeep.stream().forEach(node->sheetData.appendChild(node));
            rowCount = starting;
        }
        
        public String stringValue() {
            return XmlUtils.asString(sheetDoc);
        }
        
        public String getSheetPath() {
            return sheetFile.getPath().replace(directoryTempFollowup.getPath()+File.separator, "");
        }
        
        private void addRow(SheetRow r) {
            if (r.getColumnIndex() > maxCol)
                maxCol = r.getColumnIndex();
            sheetData.appendChild(r.buildNode());
            rowCount++;
        }
    }
    
    public class SheetRow {
        public StringBuilder rowString = new StringBuilder();
        private int rowNumber;
        private int columnIndex = 0;
        private Sheet sheet;
        private Document sheetDoc;
        private Element row;

        public SheetRow(Sheet sheet, int rowNumber, Document sheetDoc) {
            this.sheet = sheet;
            this.rowNumber = rowNumber;
            this.sheetDoc = sheetDoc;
            this.row = sheetDoc.createElement("row");
        }
        
        public Node buildNode() {
            row.setAttribute("r", Integer.toString(rowNumber));
            row.setAttribute("spans", "1:"+columnIndex);
            row.setAttribute("x14ac:dyDescent", "0.25");
            
            return row;
        }

        public void addColumn(String value) {
            Element column = addColumn(getOrSetIndexInSharedStrings(value), "v");
            column.setAttribute("t", "s");
        }

        public void addColumn(Long value) {
            addColumn(defaultIfNull(value, "").toString(), "v");
        }
        
        public void addColumn(Double value) {
            addColumn(defaultIfNull(value, "").toString(), "v");
        }
        
        public void addFormula(String formula) {
            Element column = addColumn(formula, "f");
            column.setAttribute("s", "4");
        }
        
        public void addColumn(Object value) {
            if (value instanceof Double)
                addColumn((Double)value);
            else
                addColumn(value.toString());
        }
        
        private Element addColumn(String colVal, String tagName) {
            Element column = sheetDoc.createElement("c");
            column.setAttribute("r", columnLabel());
            Element valueNode = sheetDoc.createElement(tagName);
            valueNode.appendChild(sheetDoc.createTextNode(colVal));
            column.appendChild(valueNode);
            row.appendChild(column);
            columnIndex++;
            return column;
        }
        
        private String columnLabel() {
            return columnIndexToLetter(columnIndex)+rowNumber;
        }
        
        public int getColumnIndex() {
            return columnIndex;
        }

        public void save() {
            sheet.addRow(this);
        }
    }
}
