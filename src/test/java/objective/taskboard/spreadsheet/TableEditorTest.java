package objective.taskboard.spreadsheet;

import static objective.taskboard.testUtils.Resources.resolve;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.SpreadsheetMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.SpreadsheetML.TablePart;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xlsx4j.sml.CTTableColumn;

import objective.taskboard.followup.FollowUpTemplate;
import objective.taskboard.utils.IOUtilities;
import objective.taskboard.utils.XmlUtils;

public class TableEditorTest {

    private static final String TABLE_ALLISSUES = "xl/tables/table7.xml";

    private final static String SAMPLE_FOLLOWUP_TEMPLATE_PATH = "followup/generic-followup-template.xlsm";

    private SimpleSpreadsheetEditor editor;

    @Before
    public void open() {
        FollowUpTemplate testTemplate = new FollowUpTemplate(resolve(SAMPLE_FOLLOWUP_TEMPLATE_PATH));
        editor = new SimpleSpreadsheetEditor(testTemplate);
        editor.open();
    }

    @After
    public void close() throws IOException {
        editor.close();
    }

    @Test
    public void whenRecreate_generatesXMLCorrectly() throws TransformerException, Docx4JException, IOException {
        Document doc = XmlUtils.asDocument(new File(editor.getExtractedSheetDirectory(), TABLE_ALLISSUES));
        NodeList set = XmlUtils.xpath(doc, "//tableColumn");
        Node expected0 = set.item(0);
        Node expected1 = set.item(1);
        Node expected2 = set.item(2);

        SimpleTableEditor allIssues = editor.getTableEditor("AllIssues");
        allIssues.recreate(CellRange.fromOneBased(1, 1, 3, 3));
        allIssues.save();

        List<CTTableColumn> tableColumn = getAllIssuesTableColumns(editor);
        assertAttributeValues((Element) expected0, tableColumn.get(0));
        assertAttributeValues((Element) expected1, tableColumn.get(1));
        assertAttributeValues((Element) expected2, tableColumn.get(2));
    }

    @Test
    public void givenAllIssuesTable_whenRecreateTable_updatedTable() throws IOException {
        // assert table7.xml
        Document doc = XmlUtils.asDocument(new File(editor.getExtractedSheetDirectory(), TABLE_ALLISSUES));
        Element table = (Element) doc.getFirstChild();
        assertThat(table.getAttribute("ref"), is("A1:AS10000"));
        Element tableColumns = (Element) doc.getElementsByTagName("tableColumns").item(0);
        assertThat(tableColumns.getAttribute("count"), is("45"));
        NodeList nodes = doc.getElementsByTagName("tableColumn");
        assertThat(nodes.getLength(), is(45));

        SimpleTableEditor allIssues = editor.getTableEditor("AllIssues");
        allIssues.recreate(CellRange.fromOneBased(1, 1, 100, 10000));
        allIssues.save();

        Document docUpdated = XmlUtils.asDocument(new File(editor.getExtractedSheetDirectory(), TABLE_ALLISSUES));
        Element tableUpdated = (Element) docUpdated.getFirstChild();
        assertThat(tableUpdated.getAttribute("ref"), is("A1:CV10000"));
        Element tableColumnsRecreated = (Element) docUpdated.getElementsByTagName("tableColumns").item(0);
        assertThat(tableColumnsRecreated.getAttribute("count"), is("100"));
        NodeList nodesRecreated = docUpdated.getElementsByTagName("tableColumn");
        assertThat(nodesRecreated.getLength(), is(100));
    }

    private List<CTTableColumn> getAllIssuesTableColumns(SimpleSpreadsheetEditor editor) throws Docx4JException, IOException {
        SpreadsheetMLPackage excelDoc = SpreadsheetMLPackage.load(IOUtilities.asResource(editor.toBytes()).getInputStream());
        TablePart table7 = (TablePart) excelDoc.getParts().get(new PartName("/xl/tables/table7.xml"));
        return table7.getContents().getTableColumns().getTableColumn();
    }

    private void assertAttributeValues(Element expected, CTTableColumn actual) {
        Assert.assertEquals(expected.getAttribute("name"), actual.getName());
        Assert.assertEquals(Long.parseLong(expected.getAttribute("id")), actual.getId());
    }
}
