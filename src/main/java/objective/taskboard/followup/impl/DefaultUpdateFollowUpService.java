package objective.taskboard.followup.impl;

import objective.taskboard.followup.UpdateFollowUpService;
import objective.taskboard.utils.XmlUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * 1. Ensure 'From Jira' tab has no data at all
 * 2. unzip the template
 * 3. run the following command:
 *   - xmllint --format xl/worksheets/sheet7.xml > sheet7-reformatted.xml
 * 4. open sheet7-reformatted.xml and copy the contents of tag <row r="1"..></row> over the same row on template src/main/resources/followup-template/sheet7-template.xml
 * 5. remove xl/worksheets/sheet7.xml and the reformatted file
 * 6. copy the contents of ./xl/sharedStrings.xml and execute the following command:
 *   - xmllint --format sharedStrings.xml > ./src/main/resources/followup-template/sharedStrings-initial.xml
 * 7. remove sharedStrings.xml
 * 8. zip the contents again into ./src/main/resources/followup-template/Followup-template.xlsm
 * 9. And you're done.
 *
 * Created by herbert on 30/06/17.
 */
public class DefaultUpdateFollowUpService implements UpdateFollowUpService {

    private static final int BUFFER = 2048;

    @Override
    public Path decompressTemplate(File template) throws IOException {
        Path pathFollowup = Files.createTempDirectory("Followup");

        ZipInputStream zipInputStream = null;
        BufferedOutputStream bufferedOutput = null;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(template));

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path entryPath = pathFollowup.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                    continue;
                } else {
                    Files.createDirectories(entryPath.getParent());
                }

                FileOutputStream fos = new FileOutputStream(entryPath.toFile());
                bufferedOutput = new BufferedOutputStream(fos, BUFFER);
                IOUtils.copy(zipInputStream, bufferedOutput);
                bufferedOutput.flush();
                bufferedOutput.close();
            }

            return pathFollowup;
        } finally {
            if (bufferedOutput != null)
                IOUtils.closeQuietly(bufferedOutput);
            if (zipInputStream != null)
                IOUtils.closeQuietly(zipInputStream);
        }
    }

    @Override
    public void validateTemplate(Path decompressed) throws InvalidTemplateException {
        Path sheetXml = getFromJiraSheet(decompressed);
        if(!isEmpty(XmlUtils.xpath(sheetXml.toFile(), "//sheetData/row[@r>1]/c/v/text()"))) {
            throw new InvalidTemplateException();
        }
    }

    @Override
    public void updateFromJiraTemplate(Path decompressed, Path fromJiraTemplate) throws IOException {
        URL original = DefaultUpdateFollowUpService.class.getResource("/followup-template/sheet7-template.xml");
        IOUtils.copy(original.openStream(), new FileOutputStream(fromJiraTemplate.toFile()));
        String newRowContent = getRowContent(decompressed);
        String updatedXml = FileUtils.readFileToString(fromJiraTemplate.toFile(), "UTF-8").replace("${headerRow}", newRowContent);
        FileUtils.write(fromJiraTemplate.toFile(), updatedXml, "UTF-8");
    }

    private String getRowContent(Path decompressed) {
        Path sheetXml = getFromJiraSheet(decompressed);
        try {
            return XmlUtils.asString(XmlUtils.xpath(sheetXml.toFile(), "//sheetData/row[@r=1]"));
        } catch (TransformerException e) {
            throw new InvalidTemplateException(e);
        }
    }

    // ---

    private static boolean isEmpty(NodeList nodeList) {
        return nodeList.getLength() == 0;
    }

    private static Path getFromJiraSheet(Path decompressed) {
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
}
