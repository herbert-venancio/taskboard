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

import static java.util.stream.Collectors.toList;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class FollowUpGenerator {

    private static final int BUFFER = 2048;

    private static final String PROPERTY_XML_SPACE_PRESERVE = " xml:space=\"preserve\"";
    private static final String TAG_T_IN_SHARED_STRINGS = "t";
    private static final String PATH_SHARED_STRINGS_INITIAL = "followup-template/sharedStrings-initial.xml";
    private static final String PATH_SHARED_STRINGS_TEMPLATE = "followup-template/sharedStrings-template.xml";
    private static final String PATH_SI_SHARED_STRINGS_TEMPLATE = "followup-template/sharedStrings-si-template.xml";
    private static final String PATH_SHEET_7_TEMPLATE = "followup-template/sheet7-template.xml";
    private static final String PATH_ROW_SHEET_TEMPLATE = "followup-template/sheet7-row-template.xml";
    private static final String PATH_FOLLOWUP_TEMPLATE_XLSM = "followup-template/Followup-template.xlsm";

    private static final String PATH_SHEET7 = "xl/worksheets/sheet7.xml";
    private static final String PATH_SHARED_STRINGS = "xl/sharedStrings.xml";

    @Autowired
    private FollowupDataProvider provider;

    public ByteArrayResource generate() throws Exception {
        try {
            File directoryTempFollowup = decompressTemplate();

            Map<String, Long> sharedStrings = getSharedStringsInitial();

            File fileSheet7 = new File(directoryTempFollowup, PATH_SHEET7);
            writeXML(fileSheet7, generateJiraDataSheet(sharedStrings));
            File fileSharedStrings = new File(directoryTempFollowup, PATH_SHARED_STRINGS);
            writeXML(fileSharedStrings, generateSharedStrings(sharedStrings));

            Path pathFollowupXLSM = compressXLSM(directoryTempFollowup);
            FileUtils.deleteDirectory(directoryTempFollowup);
            ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(pathFollowupXLSM));
            Files.delete(pathFollowupXLSM);
            return resource;
        } catch (Exception e) {
            log.error(e.getMessage() == null ? e.toString() : e.getMessage());
            throw e;
        }
    }

    private File decompressTemplate() throws Exception {
        Path pathFollowup = Files.createTempDirectory("Followup");

        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(PATH_FOLLOWUP_TEMPLATE_XLSM);
        ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(inputStream));

        BufferedOutputStream bufferedOutput = null;
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            Path entryPath = pathFollowup.resolve(entry.getName());
            if (entry.isDirectory()) {
                Files.createDirectories(entryPath);
                continue;
            }

            int count;
            byte data[] = new byte[BUFFER];
            FileOutputStream fos = new FileOutputStream(entryPath.toFile());
            bufferedOutput = new BufferedOutputStream(fos, BUFFER);
            while ((count = zipInputStream.read(data, 0, BUFFER)) != -1)
                bufferedOutput.write(data, 0, count);
            bufferedOutput.flush();
            bufferedOutput.close();
        }
        zipInputStream.close();

        return pathFollowup.toFile();
    }

    private void writeXML(File file, String xml) throws Exception {
        FileOutputStream output = new FileOutputStream(file);
        output.write(xml.getBytes());
        output.close();
    }

    private Path compressXLSM(File directoryFollowup) throws Exception {
        Path pathFollowupXLSM = Files.createTempFile("Followup", ".xlsm");
        FileOutputStream fileOutputStream = new FileOutputStream(pathFollowupXLSM.toFile());
        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
        for (File fileInFollowup : directoryFollowup.listFiles())
            compressFile(fileInFollowup, null, zipOutputStream);
        zipOutputStream.close();
        return pathFollowupXLSM;
    }

    private void compressFile(File file, String parentDirectory, ZipOutputStream zipOutputStream) throws Exception {
        if (file == null || !file.exists())
            return;

        String zipEntryName = parentDirectory == null ? file.getName() : parentDirectory + "/" + file.getName();

        if (file.isDirectory()) {
            for (File f : file.listFiles())
                compressFile(f, zipEntryName, zipOutputStream);
            return;
        }

        ZipEntry entry = new ZipEntry(zipEntryName);
        zipOutputStream.putNextEntry(entry);

        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bufferedInput = new BufferedInputStream(fis, BUFFER);
        int count;
        byte data[] = new byte[BUFFER];
        while((count = bufferedInput.read(data, 0, BUFFER)) != -1)
            zipOutputStream.write(data, 0, count);
        bufferedInput.close();
    }

    Map<String, Long> getSharedStringsInitial() throws ParserConfigurationException, SAXException, IOException {
        Map<String, Long> sharedStrings = new HashMap<String, Long>();
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(PATH_SHARED_STRINGS_INITIAL);
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
    }

    String generateJiraDataSheet(Map<String, Long> sharedStrings) throws IOException {
        String rowTemplate = getStringFromXML(PATH_ROW_SHEET_TEMPLATE);
        String rows = "";
        int rowNumber = 2;

        for (FollowUpData followUpData : provider.getJiraData()) {
            Map<String, Object> rowValues = new HashMap<String, Object>();
            rowValues.put("rowNumber", rowNumber);
            rowValues.put("project", getIndexInSharedStrings(sharedStrings, followUpData.project));
            rowValues.put("demandType", getIndexInSharedStrings(sharedStrings, followUpData.demandType));
            rowValues.put("demandStatus", getIndexInSharedStrings(sharedStrings, followUpData.demandStatus));
            rowValues.put("demandNum", getIndexInSharedStrings(sharedStrings, followUpData.demandNum));
            rowValues.put("demandSummary", getIndexInSharedStrings(sharedStrings, followUpData.demandSummary));
            rowValues.put("demandDescription", getIndexInSharedStrings(sharedStrings, followUpData.demandDescription));
            rowValues.put("taskType", getIndexInSharedStrings(sharedStrings, followUpData.taskType));
            rowValues.put("taskStatus", getIndexInSharedStrings(sharedStrings, followUpData.taskStatus));
            rowValues.put("taskNum", getIndexInSharedStrings(sharedStrings, followUpData.taskNum));
            rowValues.put("taskSummary", getIndexInSharedStrings(sharedStrings, followUpData.taskSummary));
            rowValues.put("taskDescription", getIndexInSharedStrings(sharedStrings, followUpData.taskDescription));
            rowValues.put("taskFullSescription", getIndexInSharedStrings(sharedStrings, followUpData.taskFullDescription));
            rowValues.put("subtaskType", getIndexInSharedStrings(sharedStrings, followUpData.subtaskType));
            rowValues.put("subtaskStatus", getIndexInSharedStrings(sharedStrings, followUpData.subtaskStatus));
            rowValues.put("subtaskNum", getIndexInSharedStrings(sharedStrings, followUpData.subtaskNum));
            rowValues.put("subtaskSummary", getIndexInSharedStrings(sharedStrings, followUpData.subtaskSummary));
            rowValues.put("subtaskDescription", getIndexInSharedStrings(sharedStrings, followUpData.subtaskDescription));
            rowValues.put("subtaskFullDescription", getIndexInSharedStrings(sharedStrings, followUpData.subtaskFullDescription));
            rowValues.put("demandId", String.valueOf(followUpData.demandId));
            rowValues.put("taskId", String.valueOf(followUpData.taskId));
            rowValues.put("subtaskId", String.valueOf(followUpData.subtaskId));
            rowValues.put("planningType", getIndexInSharedStrings(sharedStrings, followUpData.planningType));
            rowValues.put("taskRelease", getIndexInSharedStrings(sharedStrings, followUpData.taskRelease));
            rowValues.put("worklog", String.valueOf(followUpData.worklog));
            rowValues.put("wrongWorklog", String.valueOf(followUpData.wrongWorklog));
            rowValues.put("demandBallpark", String.valueOf(followUpData.demandBallpark));
            rowValues.put("taskBallpark", String.valueOf(followUpData.taskBallpark));
            rowValues.put("tshirtSize", getIndexInSharedStrings(sharedStrings, followUpData.tshirtSize));
            rowValues.put("queryType", getIndexInSharedStrings(sharedStrings, followUpData.queryType));
            rows += StrSubstitutor.replace(rowTemplate, rowValues);
            rowNumber++;
        }

        String sheetTemplate = getStringFromXML(PATH_SHEET_7_TEMPLATE);
        Map<String, String> sheetValues = new HashMap<String, String>();
        sheetValues.put("rows", rows);
        return StrSubstitutor.replace(sheetTemplate, sheetValues);
    }

    private String getStringFromXML(String pathXML) throws IOException {
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(pathXML);
        return IOUtils.toString(inputStream, "UTF-8");
    }

    private Long getIndexInSharedStrings(Map<String, Long> sharedStrings, String followUpDataAttribute) {
        Long index = sharedStrings.get(followUpDataAttribute);
        if (index != null)
            return index;

        index = Long.valueOf(sharedStrings.size());
        sharedStrings.put(followUpDataAttribute, index);
        return index;
    }

    String generateSharedStrings(Map<String, Long> sharedStrings) throws IOException {
        String siSharedStringsTemplate = getStringFromXML(PATH_SI_SHARED_STRINGS_TEMPLATE);
        List<String> sharedStringsSorted = sharedStrings.keySet().stream()
            .sorted((s1, s2) -> sharedStrings.get(s1).compareTo(sharedStrings.get(s2)))
            .collect(toList());
        String allSharedStrings = "";

        for (String sharedString : sharedStringsSorted) {
            Map<String, Object> siValues = new HashMap<String, Object>();
            siValues.put("preserve", sharedString.endsWith(" ") ? PROPERTY_XML_SPACE_PRESERVE : "");
            siValues.put("sharedString", StringEscapeUtils.escapeXml(sharedString));
            allSharedStrings += StrSubstitutor.replace(siSharedStringsTemplate, siValues);
        }

        String sharedStringsTemplate = getStringFromXML(PATH_SHARED_STRINGS_TEMPLATE);
        Map<String, Object> sharedStringsValues = new HashMap<String, Object>();
        sharedStringsValues.put("sharedStringsSize", sharedStringsSorted.size());
        sharedStringsValues.put("allSharedStrings", allSharedStrings);
        return StrSubstitutor.replace(sharedStringsTemplate, sharedStringsValues);
    }
}
