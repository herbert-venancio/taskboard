package objective.taskboard.followup;

import objective.taskboard.utils.XmlUtils;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;
import java.nio.file.Path;

public class FollowUpTemplateValidator {

    public void validate(Path path) {
        Path sheetXml = searchFromJiraSheet(path);
        if(!isEmpty(XmlUtils.xpath(sheetXml.toFile(), "//sheetData/row[@r>1]/c/v/text()"))) {
            throw new InvalidTemplateException();
        }
    }

    // ---

    private static boolean isEmpty(NodeList nodeList) {
        return nodeList.getLength() == 0;
    }

    private static Path searchFromJiraSheet(Path decompressed) {
        try {
            Path wbXml = decompressed.resolve("xl/workbook.xml");
            String relId = XmlUtils.asString(XmlUtils.xpath(wbXml.toFile(), "//sheet[@name='From Jira']/@id"));
            Path wbRelXml = decompressed.resolve("xl/_rels/workbook.xml.rels");
            String sheetId = XmlUtils.asString(XmlUtils.xpath(wbRelXml.toFile(), "//Relationship[@Id='" + relId + "']/@Target"));
            return decompressed.resolve("xl/" + sheetId);
        } catch (TransformerException e) {
            throw new InvalidTemplateException(e);
        }
    }

    public static class InvalidTemplateException extends RuntimeException {
        private static final String MESSAGE = "Invalid Template";
        public InvalidTemplateException() {
            super(MESSAGE);
        }
        public InvalidTemplateException(Exception e) {
            super(MESSAGE, e);
        }
    }
}
