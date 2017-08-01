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
package objective.taskboard.followup;

import objective.taskboard.followup.FollowUpTemplateValidator.InvalidTemplateException;
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

import static java.nio.file.Files.createTempDirectory;
import static objective.taskboard.utils.ZipUtils.unzip;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.*;

public class UpdateFollowUpServiceTest {

    private static final String OK_FOLLOWUP_TEMPLATE_XLSM = "OkFollowupTemplate.xlsm";
    private static final String NOT_OK_FOLLOWUP_TEMPLATE_XLSM = "NotOkFollowupTemplate.xlsm";

    private UpdateFollowUpService updateFollowUpService;

    @Before
    public void setupService() {
        updateFollowUpService = new DefaultUpdateFollowUpService();
    }

    @Test
    public void validateReceivedFile_okFilePass() throws URISyntaxException, IOException {
        Path decompressed = decompressTemplate(OK_FOLLOWUP_TEMPLATE_XLSM);
        try {
            updateFollowUpService.validateTemplate(decompressed);
        } finally {
            FileUtils.deleteQuietly(decompressed.toFile());
        }
    }

    @Test
    public void validateReceivedFile_nonBlankFileFail() throws URISyntaxException, IOException {
        Path decompressed = decompressTemplate(NOT_OK_FOLLOWUP_TEMPLATE_XLSM);
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
        Path decompressed = decompressTemplate(OK_FOLLOWUP_TEMPLATE_XLSM);
        try {
            updateFollowUpService.updateFromJiraTemplate(decompressed, temp);
            assertThat(temp.toFile().length(), greaterThan(0L));
            assertThatPlaceholderWasReplacedWithActualContent(temp);
        } finally {
            FileUtils.deleteQuietly(temp.toFile());
            FileUtils.deleteQuietly(decompressed.toFile());
        }
    }

    @Test
    public void updateSharedStrings() throws IOException, URISyntaxException {
        Path temp = Files.createTempFile("shared-strings", ".xml");
        Path decompressed = decompressTemplate(OK_FOLLOWUP_TEMPLATE_XLSM);
        try {
            updateFollowUpService.updateSharedStringsInitial(decompressed, temp);
            assertThat(temp.toFile().length(), greaterThan(0L));
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
        Path decompressed = decompressTemplate(OK_FOLLOWUP_TEMPLATE_XLSM);
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

    // ---

    private void assertThatPlaceholderWasReplacedWithActualContent(Path temp) throws IOException {
        assertThat(FileUtils.readFileToString(temp.toFile(), "UTF-8"), not(containsString("${headerRow}")));
        assertThat(XmlUtils.xpath(temp.toFile(), "//sheetData/row/c").getLength(), greaterThan(0));
    }

    // ---

    private Path decompressTemplate(String templateFile) throws URISyntaxException, IOException {
        File followupTemplate = new File(UpdateFollowUpServiceTest.class.getResource(templateFile).toURI());
        Path pathFollowup = createTempDirectory("Followup");
        unzip(followupTemplate, pathFollowup);
        return pathFollowup;
    }

}
