package objective.taskboard.followup;

/*-
 * [LICENSE]
 * Taskboard
 * - - -
 * Copyright (C) 2015 - 2016 Objective Solutions
 * - - -
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * [/LICENSE]
 */

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

    	private static final long serialVersionUID = 1L;
		
    	private static final String MESSAGE = "Invalid Template";
        public InvalidTemplateException() {
            super(MESSAGE);
        }
        public InvalidTemplateException(Exception e) {
            super(MESSAGE, e);
        }
    }
}
