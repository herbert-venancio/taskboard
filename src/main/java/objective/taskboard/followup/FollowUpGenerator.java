package objective.taskboard.followup;

/*-
 * [LICENSE]
 * Taskboard
 * ---
 * Copyright (C) 2015 - 2017 Objective Solutions
 * ---
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

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FollowUpGenerator {

    private static final String TAG_T_IN_SHARED_STRINGS = "t";
    private static final String PATH_SHARED_STRINGS_TEMPLATE = "followup-template/sharedStrings-template.xml";

    private Map<String, Long> sharedStrings;

    @Autowired
    private FollowupDataProvider provider;

    public ByteArrayResource generate() {
        sharedStrings = getSharedStringsTemplate(PATH_SHARED_STRINGS_TEMPLATE);
        generateJiraDataSheet();
        // write (generateJiraDataSheet)
        // write (generateSharedStrings)
        // zip
        // read bytes

        return null;
    }

    Map<String, Long> getSharedStringsTemplate(String path) {
        Map<String, Long> sharedStrings = new HashMap<String, Long>();
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream(path);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(inputStream);
            doc.getDocumentElement().normalize();
            NodeList nodes = doc.getElementsByTagName(TAG_T_IN_SHARED_STRINGS);

            for (Long index = 0L; index < nodes.getLength(); index++) {
                Node node = nodes.item(index.intValue());
                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                sharedStrings.put(node.getTextContent(), index);
            }
            return sharedStrings;
        } catch (Exception e) {
            log.error(e.getMessage());
            return new HashMap<String, Long>();
        }
    }

    String generateJiraDataSheet() {
        //* pre popula o sharedStrings
        // pega o template da linha (row-sheet-template)
        // itera pelos dados do provider
        //     insere no sharedStrings se não existir
        //     concatena a linha com os valores
        // insere as linhas geradas no meio do template do sheet (sheet7-template)
        // retorna a string inteira do sheet7

//        try {
//            InputStream inputStream = getClass().getClassLoader()
//                    .getResourceAsStream("followup-template/row-sheet-template.xml");
//            String rowTemplate = IOUtils.toString(inputStream, "UTF-8");
//            String rows = "";
//            int rowNumber = 2;
//            for (FollowUpData followUpData : provider.getJiraData()) {
//                Map<String, Object> valuesMap = new HashMap<String, Object>();
//                valuesMap.put("rowNumber", rowNumber);
//                Long indexInSharedStrings = sharedStrings.get(followUpData.project);
//                if (indexInSharedStrings == null) {
//                    indexInSharedStrings = Long.valueOf(sharedStrings.size());
//                    sharedStrings.put(followUpData.project, indexInSharedStrings);
//                }
//            }
//            
//        } catch (Exception e) {
//            log.error(e.getMessage());
//        }

        return null;
    }

    String generateSharedStrings() {
        throw new IllegalStateException("NOT IMPLEMENTED");
    }
}
