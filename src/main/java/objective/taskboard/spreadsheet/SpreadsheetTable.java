package objective.taskboard.spreadsheet;

import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

class SpreadsheetTable {

    public static final String TAG_TABLE_COLUMN = "tableColumn";

    private String path;
    private TableType type;
    private String relPath;
    private String queryTablePath;
    private String worksheetSource;
    private CellRange ref;
    private Map<Integer, TableColumn> columns;
    private int initialColumnsCount;

    enum TableType {
        worksheet,
        xml,
        queryTable
    }

    static class TableColumn {
        public String id;
        public String uniqueName;
        public String name;
        public String queryTableFieldId;

        public TableColumn(String id, String uniqueName, String name, String queryTableFieldId) {
            this.id = id;
            this.uniqueName = uniqueName;
            this.name = name;
            this.queryTableFieldId = queryTableFieldId;
        }

        public static TableColumn fromXmlNode(Element node) {
            String id = node.getAttribute("id");
            String uniqueName = node.getAttribute("uniqueName");
            String name = node.getAttribute("name");
            String queryTableFieldId = node.getAttribute("queryTableFieldId");
            return new TableColumn(id, uniqueName, name, queryTableFieldId);
        }

        public Node asXmlNode(Document doc) {
            Element element = doc.createElement(TAG_TABLE_COLUMN);
            element.setAttribute("id", id);
            if(uniqueName != null)
                element.setAttribute("uniqueName", uniqueName);
            if(name != null)
                element.setAttribute("name", name);
            if(queryTableFieldId != null)
                element.setAttribute("queryTableFieldId", queryTableFieldId);
            return element;
        }
    }

    public String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    public TableType getType() {
        return type;
    }

    void setType(TableType type) {
        this.type = type;
    }

    public String getRelPath() {
        return relPath;
    }

    void setRelPath(String relPath) {
        this.relPath = relPath;
    }

    public String getQueryTablePath() {
        return queryTablePath;
    }

    void setQueryTablePath(String queryTablePath) {
        this.queryTablePath = queryTablePath;
    }

    public String getWorksheetSource() {
        return worksheetSource;
    }

    void setWorksheetSource(String worksheetSource) {
        this.worksheetSource = worksheetSource;
    }

    public CellRange getRef() {
        return ref;
    }

    void setRef(CellRange ref) {
        this.ref = ref;
    }

    public Map<Integer, TableColumn> getColumns() {
        return columns;
    }

    void setColumns(Map<Integer, TableColumn> columns) {
        this.columns = columns;
    }

    public int getInitialColumnsCount() {
        return initialColumnsCount;
    }

    void setInitialColumnsCount(int initialColumnsCount) {
        this.initialColumnsCount = initialColumnsCount;
    }

}
