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

import objective.taskboard.followup.UpdateFollowUpService.InvalidTemplateException;
import objective.taskboard.followup.impl.DefaultUpdateFollowUpService;
import objective.taskboard.utils.XmlUtils;
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
            assertThatPlaceholderWasReplacedWithActualContent(temp);
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
            updateFollowUpService.deleteGeneratedFiles(decompressed);
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

    private void assertThatPlaceholderWasReplacedWithActualContent(Path temp) throws IOException {
        assertThat(FileUtils.readFileToString(temp.toFile(), "UTF-8"), not(containsString("${headerRow}")));
        assertThat(XmlUtils.xpath(temp.toFile(), "//sheetData/row/c").getLength(), greaterThan(0));
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
