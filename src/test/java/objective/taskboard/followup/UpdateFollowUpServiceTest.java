package objective.taskboard.followup;

import objective.taskboard.followup.UpdateFollowUpService.InvalidTemplateException;
import objective.taskboard.followup.impl.DefaultUpdateFollowUpService;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.contains;

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
        File okFollowupTemplate = new File(UpdateFollowUpServiceTest.class.getResource("OkFollowupTemplate.xlsm").toURI());
        Path decompressed = updateFollowUpService.decompressTemplate(okFollowupTemplate);
        try {
            updateFollowUpService.validateTemplate(decompressed);
        } finally {
            FileUtils.deleteDirectory(decompressed.toFile());
        }
    }

    @Test
    public void validateReceivedFile_nonBlankFileFail() throws URISyntaxException, IOException {
        File notOkFollowupTemplate = new File(UpdateFollowUpServiceTest.class.getResource("NotOkFollowupTemplate.xlsm").toURI());
        Path decompressed = updateFollowUpService.decompressTemplate(notOkFollowupTemplate);
        try {
            updateFollowUpService.validateTemplate(decompressed);
            fail("Should have thrown InvalidTemplateException");
        } catch (InvalidTemplateException e) {
            // ok
        } finally {
            FileUtils.deleteDirectory(decompressed.toFile());
        }
    }

    @Test
    public void updateFromJiraTemplate() throws IOException, URISyntaxException {
        Path temp = Files.createTempFile("sheet-template", "xml");
        File okFollowupTemplate = new File(UpdateFollowUpServiceTest.class.getResource("OkFollowupTemplate.xlsm").toURI());
        Path decompressed = updateFollowUpService.decompressTemplate(okFollowupTemplate);
        try {
            updateFollowUpService.updateFromJiraTemplate(decompressed, temp);
            assertThat(temp.toFile().length(), greaterThan(0l));
            assertThat(FileUtils.readFileToString(temp.toFile(), "UTF-8"), not(containsString("${headerRow}")));
        } finally {
            Files.delete(temp);
        }
    }
}
