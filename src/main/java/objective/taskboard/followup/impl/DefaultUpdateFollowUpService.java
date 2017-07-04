package objective.taskboard.followup.impl;

import objective.taskboard.followup.UpdateFollowUpService;
import objective.taskboard.utils.XmlUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.springframework.stereotype.Service;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Service
public class DefaultUpdateFollowUpService implements UpdateFollowUpService {

    @Override
    public Path decompressTemplate(File template) throws IOException {
        return decompressTemplate(new FileInputStream(template));
    }

    @Override
    public Path decompressTemplate(InputStream stream) throws IOException {
        Path pathFollowup = Files.createTempDirectory("Followup");

        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(stream);

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                Path entryPath = pathFollowup.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                    continue;
                } else {
                    Files.createDirectories(entryPath.getParent());
                }
                Files.copy(zipInputStream, entryPath);
            }

            return pathFollowup;
        } finally {
            if (zipInputStream != null)
                IOUtils.closeQuietly(zipInputStream);
        }
    }

    @Override
    public void validateTemplate(Path decompressed) throws InvalidTemplateException {
        Path sheetXml = searchFromJiraSheet(decompressed);
        if(!isEmpty(XmlUtils.xpath(sheetXml.toFile(), "//sheetData/row[@r>1]/c/v/text()"))) {
            throw new InvalidTemplateException();
        }
    }

    @Override
    public void updateFromJiraTemplate(Path decompressed, Path fromJiraTemplate) throws IOException {
        URL original = DefaultUpdateFollowUpService.class.getResource("/followup-template/sheet7-template-raw.xml");
        String newRowContent = getRowContent(decompressed);
        Map<String, Object> map = new HashMap<>();
        map.put("headerRow", newRowContent);
        String updatedXml = StrSubstitutor.replace(IOUtils.toString(original, "UTF-8"), map);
        FileUtils.write(fromJiraTemplate.toFile(), updatedXml, "UTF-8");
    }

    @Override
    public void updateSharedStrings(Path decompressed, Path sharedStringsTemplate) throws IOException {
        Path source = decompressed.resolve("xl/sharedStrings.xml");
        XmlUtils.format(source.toFile(), sharedStringsTemplate.toFile());
    }

    @Override
    public void deleteGeneratedFiles(Path decompressed) throws IOException {
        Files.delete(searchFromJiraSheet(decompressed));
        Files.delete(decompressed.resolve("xl/sharedStrings.xml"));
    }

    @Override
    public Path compressTemplate(Path decompressed, Path pathFollowupXLSM) throws IOException {
        ZipOutputStream zipOutputStream = null;
        try {
            zipOutputStream = new ZipOutputStream(new BufferedOutputStream(Files.newOutputStream(pathFollowupXLSM)));
            Iterable<Path> it = Files.walk(decompressed)::iterator;
            for (Path fileInFollowup : it)
                compressFile(fileInFollowup, decompressed.relativize(fileInFollowup).toString(), zipOutputStream);
            return pathFollowupXLSM;
        } finally {
            if (zipOutputStream != null)
                zipOutputStream.close();
        }
    }

    // ---

    private String getRowContent(Path decompressed) {
        Path sheetXml = searchFromJiraSheet(decompressed);
        try {
            return XmlUtils.asString(XmlUtils.xpath(sheetXml.toFile(), "//sheetData/row[@r=1]"));
        } catch (TransformerException e) {
            throw new InvalidTemplateException(e);
        }
    }

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

    private void compressFile(Path path, String zipEntryName, ZipOutputStream zipOutputStream) throws IOException {
        if (Files.isDirectory(path))
            return;

        ZipEntry entry = new ZipEntry(zipEntryName);
        zipOutputStream.putNextEntry(entry);
        Files.copy(path, zipOutputStream);
    }
}
