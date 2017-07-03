package objective.taskboard.followup;

import objective.taskboard.followup.UpdateFollowUpService.InvalidTemplateException;
import objective.taskboard.followup.impl.DefaultUpdateFollowUpService;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

/**
 * Created by herbert on 30/06/17.
 */
public class UpdateFollowUpServiceTest {

    private UpdateFollowUpService updateFollowUpService;

    @Before
    public void setupService() {
        updateFollowUpService = new DefaultUpdateFollowUpService();
    }

    @Test
    public void validateReceivedFile_okFilePass() throws URISyntaxException, IOException {
        Path decompressed = decompressOkTemplate();
        try {
            updateFollowUpService.validateTemplate(decompressed);
        } finally {
            FileUtils.deleteQuietly(decompressed.toFile());
        }
    }

    @Test
    public void validateReceivedFile_nonBlankFileFail() throws URISyntaxException, IOException {
        Path decompressed = decompressNotOkTemplate();
        try {
            updateFollowUpService.validateTemplate(decompressed);
            fail("Should have thrown InvalidTemplateException");
        } catch (InvalidTemplateException e) {
            // ok
        } finally {
            FileUtils.deleteQuietly(decompressed.toFile());
        }
    }

    @Test
    public void updateFromJiraTemplate() throws IOException, URISyntaxException {
        Path temp = Files.createTempFile("sheet-template", ".xml");
        Path decompressed = decompressOkTemplate();
        try {
            updateFollowUpService.updateFromJiraTemplate(decompressed, temp);
            assertThat(temp.toFile().length(), greaterThan(0l));
            assertThat(FileUtils.readFileToString(temp.toFile(), "UTF-8"), not(containsString("${headerRow}")));
        } finally {
            FileUtils.deleteQuietly(temp.toFile());
            FileUtils.deleteQuietly(decompressed.toFile());
        }
    }

    @Test
    public void updateSharedStrings() throws IOException, URISyntaxException {
        Path temp = Files.createTempFile("shared-strings", ".xml");
        Path decompressed = decompressOkTemplate();
        try {
            updateFollowUpService.updateSharedStrings(decompressed, temp);
            assertThat(temp.toFile().length(), greaterThan(0l));
            String content = FileUtils.readFileToString(temp.toFile(), "UTF-8");
            assertThat(content, containsString("project"));
            assertThat(content, containsString("Effort"));
        } finally {
            FileUtils.deleteQuietly(temp.toFile());
            FileUtils.deleteQuietly(decompressed.toFile());
        }
    }

    @Test
    public void deleteSheet7AndSharedStrings() throws IOException, URISyntaxException {
        Path decompressed = decompressOkTemplate();
        Path sheet7File = decompressed.resolve("xl/worksheets/sheet7.xml");
        Path sharedStringsFile = decompressed.resolve("xl/sharedStrings.xml");
        try {
            assertTrue(Files.exists(sheet7File));
            assertTrue(Files.exists(sharedStringsFile));
            updateFollowUpService.deleteFilesThatAreGenerated(decompressed);
            assertFalse(Files.exists(sheet7File));
            assertFalse(Files.exists(sharedStringsFile));
        }
        finally {
            FileUtils.deleteQuietly(decompressed.toFile());
        }
    }

    @Test
    public void decompressCompress() throws IOException, URISyntaxException {
        Path decompressed = decompressOkTemplate();
        Path pathFollowupXLSM = Files.createTempFile("Followup", ".xlsm");
        try {
            updateFollowUpService.compressTemplate(decompressed, pathFollowupXLSM);
            assertThat(Files.size(pathFollowupXLSM), greaterThan(0l));
        } finally {
            FileUtils.deleteQuietly(pathFollowupXLSM.toFile());
            FileUtils.deleteQuietly(decompressed.toFile());
        }
    }

    // ---

    private Path decompressOkTemplate() throws URISyntaxException, IOException {
        File okFollowupTemplate = new File(UpdateFollowUpServiceTest.class.getResource("OkFollowupTemplate.xlsm").toURI());
        return updateFollowUpService.decompressTemplate(okFollowupTemplate);
    }

    private Path decompressNotOkTemplate() throws URISyntaxException, IOException {
        File notOkFollowupTemplate = new File(UpdateFollowUpServiceTest.class.getResource("NotOkFollowupTemplate.xlsm").toURI());
        return updateFollowUpService.decompressTemplate(notOkFollowupTemplate);
    }
}
